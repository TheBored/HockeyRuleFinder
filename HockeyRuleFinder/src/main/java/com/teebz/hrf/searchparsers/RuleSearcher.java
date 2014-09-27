package com.teebz.hrf.searchparsers;

import com.teebz.hrf.entities.Rule;
import com.teebz.hrf.entities.SearchResult;
import com.teebz.hrf.entities.Section;

import android.content.res.AssetManager;
import android.util.Log;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
public class RuleSearcher {
    public static final int SEARCH_BUFFER_SPACE = 45; //Number of characters to buffer around search text
    private static RuleSearcher searcher;

    private ArrayList<Section> sections;
    private ArrayList<Rule> rules;

    //Implement a singleton to ensure that parsing only happens once.
    public static synchronized RuleSearcher getSearcher(AssetManager manager) {
        if (searcher == null) {
            searcher = new RuleSearcher(manager);
        }
        return searcher;
    }

    //Don't let anyone call the constructor outside this class.
    private RuleSearcher(AssetManager manager) {
        try { //Get all of the sections.
            InputStream s = manager.open("rules.xml");
            RuleXMLParser parse = new RuleXMLParser(s);
            sections = parse.getSections();
            s.close();
        } catch (Exception e) {
            Log.d("HockeyRuleFinder", "RuleXMLParser failed parsing.");
        }

        //With all of the sections, build a flat list of rules.
        //No need to build it recursively, we know how deep the rules go.
        rules = new ArrayList<Rule>();
        for (Section s : sections) {
            for (Rule parent : s.rules) {
                rules.add(parent);
                for (Rule child : parent.subRules) {
                    rules.add(child);
                }
            }
        }
    }

    public ArrayList<Section> getSections() {
        return sections;
    }

    public Section getSectionById (String sectionId) {
        for (Section s : sections){
            if (s.id.equals(sectionId)) {
                return s;
            }
        }
        return null;
    }

    public Rule getRuleById(String ruleId) {
        for (Section s : sections) {
            for (Rule parent : s.rules) {
                if (parent.id.equals(ruleId)) {
                    return parent;
                }
                for (Rule r : parent.subRules) {
                    if (r.id.equals(ruleId)) {
                        return  r;
                    }
                }
            }
        }
        return null;
    }

    public ArrayList<Rule> getRulesBySectionId (String sectionId) {

        for (Section s : sections){
            if (s.id.equals(sectionId)) {
                ArrayList<Rule> ret = new ArrayList<Rule>();
                Collections.addAll(ret, s.rules);
                return ret;
            }
        }
        return null;
    }

    public ArrayList<Rule> getRulesWithImages() {
        ArrayList<Rule> response = new ArrayList<Rule>();
        for (Rule r : rules) {
            if (r.imgName != null) {
                response.add(r);
            }
        }

        //These rules are sorted alphabetically by name.
        Collections.sort(response, new Comparator<Rule>() {
            public int compare(Rule r1, Rule r2) {
                return r1.name.compareTo(r2.name);
            }
        });

        return response;
    }

    public Rule getParentRuleForId(String id) {
        for (Section s : sections) {
            for (Rule parent : s.rules) {
                for (Rule child : parent.subRules) {
                    if (child.id.equals(id)){
                        return parent;
                    }
                }
            }
        }
        return null;
    }

    public Section getSectionForParentId(String id) {
        for (Section s : sections) {
            for (Rule parent : s.rules) {
                if (parent.id.equals(id)) {
                    return s;
                }
            }
        }
        return null;
    }

    public List<SearchResult> searchRules(String text) {
        List<SearchResult> results = new ArrayList<SearchResult>();

        if (text.isEmpty()) {
            return results; //No search text = no results.
        }

        for (Section s : sections) {
            for (Rule parent : s.rules) {
                boolean isTitleMatch = false;
                ArrayList<String> contentFound = new ArrayList<String>();
                if (parent.name.toLowerCase().contains(text.toLowerCase())) {
                    isTitleMatch = true;
                }
                for (Rule child : parent.subRules) {
                    if (text.trim().toLowerCase().equals(parent.id) ||
                        text.trim().toLowerCase().equals(child.id)) {
                        isTitleMatch = true;
                    }
                    for (String par : child.searchContents) {
                        if (par.toLowerCase().contains(text.toLowerCase())) {
                            contentFound.add(par);
                        }
                    }
                }

                if (isTitleMatch || contentFound.size() > 0) {
                    results.add(getResult(parent, text, contentFound));
                }
            }
        }

        //Lastly, sort ze list.
        Collections.sort(results, new Comparator<SearchResult>(){
            public int compare(SearchResult sr1, SearchResult sr2){
                if(sr1.countFound == sr2.countFound)
                    return 0;
                return sr1.countFound < sr2.countFound ? 1 : -1;
            }
        });
        return results;
    }

    public List<Rule> getAssociatedRules(String ruleId) {
        //We have an id. If that rule is a "parent" rule, return its subrules.
        //If that rule is a "child" rule, return the sibling rules.
        for (Section s : sections) {
            for (Rule parent : s.rules) {
                for (Rule child : parent.subRules) {
                    if (parent.id.equals(ruleId) || child.id.equals(ruleId)) {
                        return parent.subRules;
                    }
                }
            }
        }
        return null;
    }

    private SearchResult getResult (Rule r, String searchText, ArrayList<String> textFound) {
        String highlightText = "";
        Section section = getSectionForParentId(r.id);
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
}
*/