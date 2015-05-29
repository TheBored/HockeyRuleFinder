package com.teebz.hrf.searchparsers;

import com.crashlytics.android.Crashlytics;
import com.teebz.hrf.entities.Rule;
import com.teebz.hrf.entities.SearchResult;
import com.teebz.hrf.entities.Section;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RuleSearcher {
    public static final int SEARCH_BUFFER_SPACE = 45; //Number of characters to buffer around search text

    private RuleDataServices mRuleDataServices;
    private int mLeagueId;
    private HashMap<Integer, Section> mSections;

    // Begin at start of line. Find the first occurence of the search term.
    // Attempt to match %d characters before the search term. Any additional
    // characters past 45 will be replaced with "...".
    private static String REGEX_FIRST_TERM = "^(.*?)(.{0,%d})(%s)(.*)$";

    //Match any lines that don't contain the term at all.
    private static String REGEX_HAS_TERM = "^.*(%s).*$";

    //Match any gaps between terms with a length greater than defined limit.
    private static String REGEX_AFTER_TERM = "(%s)((?!%s).{%d,}?)(%s|$)";

    public RuleSearcher(RuleDataServices rds, int leagueId) {
        mRuleDataServices = rds;
        setSectionCache(mLeagueId);
    }

    //Section data is used with every search result. Cache it here for later use.
    //This will be invalidated should the leagueId change.
    private void setSectionCache(int leagueId) {
        //Keep track of what league this cache is for.
        mLeagueId = leagueId;

        mSections = new HashMap<Integer, Section>();

        ArrayList<Section> sections = mRuleDataServices.getAllSections(mLeagueId);
        for (Section s : sections) {
            mSections.put(s.getSID(), s);
        }
    }

    public List<SearchResult> searchRules(String text, int leagueId) {
        //If the league ID has changed, reset the section cache.
        if (leagueId != mLeagueId) {
            setSectionCache(leagueId);
        }

        List<SearchResult> results = new ArrayList<SearchResult>();

        //Before anything starts, trim just in case.
        text = text.trim();

        //No search text = no results.
        if (text.isEmpty()) {
            return results;
        }

        //First up, find any rules that might have the text we are interested in.
        int[] directReferences = mRuleDataServices.findRulesWithText(leagueId, text);

        //Their search returns no direct references -- nothing to see here.
        if (directReferences.length == 0) {
            return results;
        }

        //With these Ids, go back to the database and get all of those rules again with any parents
        //that aren't already in the list. Also puts them into a tree.
        ArrayList<Rule> possibleReferences = mRuleDataServices.getRules(leagueId, null, directReferences, false);

        for (Rule parent : possibleReferences) {
            results.add(getResult(parent, text));
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

    public static String getHighlightedText(String input, String highlightText) {
        if (highlightText == null || highlightText.isEmpty())
            return input; //Nothing to highlight, just return.

        String response = input;
        try {
            Pattern p = Pattern.compile(highlightText, Pattern.CASE_INSENSITIVE);
            response = input.replaceAll("(?i)" + highlightText, "<b><font color=\"Red\">$0</font></b>");
        }
        catch (Exception e) {
            Crashlytics.setString("Text", input);
            Crashlytics.setString("Highlight", highlightText);
            Crashlytics.logException(e);
        }
        return response;
    }

    private static String getCondensedVersion(String input, String highlightText) {
        String response = "";

        //Remove all lines that don't contain the pattern at all.
        Pattern emptyPattern = Pattern.compile(String.format(REGEX_HAS_TERM, highlightText),
                                               Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
        Matcher emptyMatch = emptyPattern.matcher(input);
        StringBuffer sbEmpty = new StringBuffer();
        while (emptyMatch.find()) { //If we found our term in this line, append.
            sbEmpty.append(emptyMatch.group() + "\n");
        }
        response = sbEmpty.toString();


        Pattern startPattern = Pattern.compile(String.format(REGEX_FIRST_TERM,
                                                             SEARCH_BUFFER_SPACE,
                                                             highlightText),
                                               Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
        Matcher startMatch = startPattern.matcher(response);
        StringBuffer sbStart = new StringBuffer();
        while (startMatch.find()) {
            //If we found enough text in group 2, replace out group 1.
            startMatch.appendReplacement(sbStart, shortenStartText(startMatch));
        }
        startMatch.appendTail(sbStart);
        response = sbStart.toString();

        Pattern afterPattern = Pattern.compile(String.format(REGEX_AFTER_TERM,
                                                             highlightText,
                                                             highlightText,
                                                             SEARCH_BUFFER_SPACE * 2, //Any gap longer than buffer * 2
                                                             highlightText),
                                               Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
        Matcher afterMatch = afterPattern.matcher(response);
        StringBuffer sbAfter = new StringBuffer();
        while (afterMatch.find()) {
            afterMatch.appendReplacement(sbAfter, shortenAfterText(afterMatch));
        }
        afterMatch.appendTail(sbAfter);
        response = sbAfter.toString();

        return response;
    }

    private static String shortenStartText(Matcher matcher) {
        //If we found enough text in group 2, replace out group 1.
        String groupOne = matcher.group(1);
        if (matcher.group(2).length() == SEARCH_BUFFER_SPACE) {
            groupOne = "...";
        }

        String result = groupOne + matcher.group(2) + matcher.group(3) + matcher.group(4);
        return escapeForReplacement(result); //We must escape SOME regex characters as this will be consumed by regex later.
    }

    private static String shortenAfterText(Matcher matcher) {
        //Check the length of the middle group. If it is too long, shorten it up.
        String gap = matcher.group(2);
        if (gap.length() > SEARCH_BUFFER_SPACE * 2) {
            String beg = gap.substring(0, SEARCH_BUFFER_SPACE);
            String end = gap.substring(gap.length() - SEARCH_BUFFER_SPACE);
            gap = beg + "..." + end;
        }
        String result = matcher.group(1) + gap + matcher.group(3);
        return escapeForReplacement(result); //We must escape SOME regex characters as this will be consumed by regex later.
    }

    private static String escapeForReplacement(String input) {
        //Sometimes we have a string that must be escaped before performing a regexp replace.
        //An example is "Blah blah $5000". In this case, the replacement treats the $5 as "group 5"
        //for the replacement. If there are not five groups in the matcher, things go derp.
        return input.replace("$", "\\$");
    }

    private SearchResult getResult (Rule r, String searchText) {
        //We're about to do a lot of RegExp so escape out the search text.
        searchText = Pattern.quote(searchText);

        //Concat all of our child rule text into one big blob.
        String allContents = "";
        for (Rule child : r.getSubRules()) {
            allContents += child.getSearchContents() + "\n";
        }

        //Remove all excess text.
        String shortVersion = getCondensedVersion(allContents, searchText);
        String highlightText = "";

        if (shortVersion.length() == 0) {
            highlightText = "No contents";
        }
        else {
            highlightText = getHighlightedText(shortVersion, searchText);
            highlightText = highlightText.trim().replace("\n", "<br /><br />");
        }

        //TODO: GET REAL COUNT BELOW
        return new SearchResult(r, mSections.get(r.getSID()), 0, highlightText);
    }
}
