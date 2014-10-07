package com.teebz.hrf.searchparsers;

import com.teebz.hrf.entities.Rule;
import com.teebz.hrf.entities.SearchResult;
import com.teebz.hrf.entities.Section;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RuleSearcher {
    public static final int SEARCH_BUFFER_SPACE = 45; //Number of characters to buffer around search text

    private RuleDataServices mRuleDataServices;
    public RuleSearcher(RuleDataServices rds) {
        mRuleDataServices = rds;
    }

    public List<SearchResult> searchRules(String text, int leagueId) {
        List<SearchResult> results = new ArrayList<SearchResult>();
        if (text.isEmpty()) {
            return results; //No search text = no results.
        }

        //First up, find any rules that might have the text we are interested in.
        int[] directReferences = mRuleDataServices.findRulesWithText(leagueId, text);

        //With these Ids, go back to the database and get all of those rules again with any parents
        //that aren't already in the list. Also puts them into a tree.
        ArrayList<Rule> possibleReferences = mRuleDataServices.getRules(leagueId, null, directReferences, false);

        for (Rule parent : possibleReferences) {
            boolean isTitleMatch = false;
            ArrayList<String> contentFound = new ArrayList<String>();
            if (parent.getName().toLowerCase().contains(text.toLowerCase())) {
                isTitleMatch = true;
            }
            for (Rule child : parent.getSubRules()) {
                if (text.trim().toLowerCase().equals(parent.getNum()) ||
                        text.trim().toLowerCase().equals(child.getNum())) {
                    isTitleMatch = true;
                }
                for (String par : child.getSearchContents()) {
                    if (par.toLowerCase().contains(text.toLowerCase())) {
                        contentFound.add(par);
                    }
                }
            }

            if (isTitleMatch || contentFound.size() > 0) {
                results.add(getResult(parent, text, contentFound));
            }
        }

        //Lastly, sort ze list.
        Collections.sort(results, new Comparator<SearchResult>() {
            public int compare(SearchResult sr1, SearchResult sr2) {
                if (sr1.countFound == sr2.countFound)
                    return 0;
                return sr1.countFound < sr2.countFound ? 1 : -1;
            }
        });

        return results;
    }

    public static String getHighlightedText(String input, String highlightText, boolean cutDownToSize) {
        if (highlightText == null || highlightText.isEmpty())
            return input; //Nothing to highlight, just return.

        String response = input;

        if (cutDownToSize) {
            //If we need to cut from the start.
            int start = response.toLowerCase().indexOf(highlightText.toLowerCase());
            if (start > SEARCH_BUFFER_SPACE) {
                String sub = response.substring(start - SEARCH_BUFFER_SPACE);
                response = "..." + sub;
            }
            //If we need to cut from the end
            int end = start + highlightText.length();
            if (response.length() - end > SEARCH_BUFFER_SPACE + 3) { //3 periods before text
                String sub = response.substring(0, end + SEARCH_BUFFER_SPACE);
                response = sub + "...";
            }
        }

        Pattern p = Pattern.compile("(?i)" + highlightText);
        Matcher m = p.matcher(response);
        while(m.find()){
            response = response.replaceFirst(m.group(), "<b><font color=\"Red\">" + m.group() + "</font></b>");
        }
        return response;
    }

    private SearchResult getResult (Rule r, String searchText, ArrayList<String> textFound) {
        String highlightText = "";
        Section section = mRuleDataServices.getSectionById(r.getSID(), false);
        if (textFound.size() == 0) {
            highlightText = "No contents";
        }
        else {
            //We need to find what to highlight. Find the first time the text is used in a paragraph.
            for (String par : textFound) {
                String temp = getHighlightedText(par, searchText, true);
                highlightText = highlightText + temp + "<br />";
            }
        }

        return new SearchResult(r, section, textFound.size(), highlightText);
    }
}
