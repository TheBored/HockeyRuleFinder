package com.teebz.hrf.searchparsers;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.widget.Toast;

import com.teebz.hrf.entities.League;
import com.teebz.hrf.entities.Rule;
import com.teebz.hrf.entities.SearchResult;
import com.teebz.hrf.entities.Section;

import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RuleDataServices {
    public static final int SEARCH_BUFFER_SPACE = 45; //Number of characters to buffer around search text
    private static RuleDataServices sRuleDataServices;
    private DBHelper mDbHelper;
    private SQLiteDatabase mDatabase;

    //Track the number of "open" requests. Only close database when we
    //call close and no other methods need the connection.
    private int mDatabaseRequests;

    public static synchronized RuleDataServices getRuleDataServices(Context context) {
        if (sRuleDataServices == null) {
            sRuleDataServices = new RuleDataServices(context);
        }
        return sRuleDataServices;
    }

    private RuleDataServices(Context context) {
        //Establish the database connection and load the rules into memory.
        mDbHelper = new DBHelper(context);
        mDatabaseRequests = 0;
    }

    //region Public methods
    public ArrayList<League> getAllLeagues() {
        ArrayList<League> leagues = new ArrayList<League>();

        dbOpen();
        Cursor cursor = mDatabase.query(DBHelper.TABLE_LEAGUE,
                DBHelper.LEAGUE_COLUMNS, null, null, null, null, null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            League league = cursorToLeague(cursor);
            leagues.add(league);
            cursor.moveToNext();
        }
        cursor.close();
        dbClose();

        return leagues;
    }

    public League getLeagueByAcronym(String acronym) {
        dbOpen();
        Cursor cursor = mDatabase.query(DBHelper.TABLE_LEAGUE,
                DBHelper.LEAGUE_COLUMNS, "acronym='"+acronym+"'", null, null, null, null);

        cursor.moveToFirst();
        League league = cursorToLeague(cursor);
        cursor.close();
        dbClose();

        return league;
    }

    public ArrayList<Section> getAllSections(int leagueId) {
        ArrayList<Section> sections = new ArrayList<Section>();

        //File file = new File("/data/data/com.teebz.hrf/databases/rules.sqlite");
        //boolean test = file.exists();

        dbOpen();
        Cursor cursor = mDatabase.query(DBHelper.TABLE_SECTION,
                DBHelper.SECTION_COLUMNS, "league_id="+leagueId, null, null, null, null);

        //Create the (empty) section elements here
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            Section section = cursorToSection(cursor);
            sections.add(section);
            cursor.moveToNext();
        }
        cursor.close();
        dbClose();

        //After the sections have been created, fill them with rules.
        for (int i = 0; i < sections.size(); i++){
            Section s = sections.get(i);
            ArrayList<Rule> rules = getRulesBySectionId(s.getSID());
            Rule[] ruleArr = new Rule[rules.size()];
            ruleArr = rules.toArray(ruleArr);
            s.setRules(ruleArr);
            sections.set(i, s);
        }

        return sections;
    }

    public Section getSectionById (int sectionId) {
        dbOpen();
        Cursor cursor = mDatabase.query(DBHelper.TABLE_SECTION,
                DBHelper.SECTION_COLUMNS, "section_id="+sectionId, null, null, null, null);

        //There should only be one section in the cursor.
        cursor.moveToFirst();
        Section s = cursorToSection(cursor);
        cursor.close();

        //Have the section, get the corresponding rules under it.
        ArrayList<Rule> rules = getRulesBySectionId(s.getSID());
        Rule[] ruleArr = new Rule[rules.size()];
        ruleArr = rules.toArray(ruleArr);
        s.setRules(ruleArr);

        dbClose();
        return s;
    }

    public ArrayList<Rule> getRulesBySectionId(int sectionId) {
        ArrayList<Rule> rules = new ArrayList<Rule>();

        dbOpen();
        Cursor cursor = mDatabase.query(DBHelper.TABLE_RULE,
                DBHelper.RULE_COLUMNS, "section_id="+sectionId, null, null, null, null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            Rule rule = cursorToRule(cursor);
            rules.add(rule);
            cursor.moveToNext();
        }
        cursor.close();
        dbClose();

        ArrayList<Rule> ruleTree = makeTree(rules);

        return ruleTree;
    }

    public Rule getRuleById(int ruleId) {
        ArrayList<Rule> rules = new ArrayList<Rule>();

        dbOpen();
        //Get this rule and any children
        Cursor cursor = mDatabase.query(DBHelper.TABLE_RULE,
                DBHelper.RULE_COLUMNS, "rule_id="+ruleId+" OR parent_rule_id="+ruleId, null, null, null, null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            Rule rule = cursorToRule(cursor);
            rules.add(rule);
            cursor.moveToNext();
        }
        cursor.close();
        dbClose();

        //Normal case, resume building the tree.
        ArrayList<Rule> ruleTree = makeTree(rules);

        if (ruleTree.size() == 0) {
            //We only got child rules in return so it isn't a valid tree. Return the first (only) child rule instead.
            return rules.get(0);
        } else {
            //Rule tree will have 1 element, the parent rule. Return that individual element.
            return ruleTree.get(0);
        }
    }

    public Rule getRuleByNum(String ruleNum, int leagueId) {
        ArrayList<Rule> rules = new ArrayList<Rule>();

        dbOpen();
        //Get this rule, can't get children w/o proper id.
        Cursor cursor = mDatabase.query(DBHelper.TABLE_RULE,
                DBHelper.RULE_COLUMNS, "rule_num="+ruleNum+" AND league_id="+leagueId, null, null, null, null);

        cursor.moveToFirst();
        Rule rule = cursorToRule(cursor);
        cursor.close();
        dbClose();

        //Let the real method build the rule & children.
        return getRuleById(rule.getRID());
    }

    public Rule getParentRuleByNum(String ruleNum, int leagueId) {
        ArrayList<Rule> rules = new ArrayList<Rule>();

        dbOpen();
        //Get this rule, can't get children w/o proper id.
        Cursor cursor = mDatabase.query(DBHelper.TABLE_RULE,
                DBHelper.RULE_COLUMNS, "rule_num="+ruleNum+" AND league_id="+leagueId, null, null, null, null);

        cursor.moveToFirst();
        Rule rule = cursorToRule(cursor);
        cursor.close();
        dbClose();

        //Let the real method build the rule & children.
        if (rule.getParent_RID() == null) {
            return getRuleById(rule.getRID());
        } else {
            return getRuleById(rule.getParent_RID());
        }
    }

    public Rule getParentRuleForId(int ruleId) {
        dbOpen();
        //Get the rule indicated
        Cursor cursor = mDatabase.query(DBHelper.TABLE_RULE,
                DBHelper.RULE_COLUMNS, "rule_id="+ruleId, null, null, null, null);

        cursor.moveToFirst();
        Rule rule = cursorToRule(cursor);
        cursor.close();
        dbClose();

        //Return the entire parent rule.
        return getRuleById(rule.getParent_RID());
    }

    public List<SearchResult> searchRules(String text, int leagueId) {
        //TODO: Database search? Is that possible?
        List<SearchResult> results = new ArrayList<SearchResult>();

        ArrayList<Section> sections = getAllSections(leagueId);

        if (text.isEmpty()) {
            return results; //No search text = no results.
        }


        for (Section s : sections) {
            for (Rule parent : s.getRules()) {
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
    //endregion

    //region Private Methods
    private void dbOpen() {
        mDatabaseRequests += 1;
        if (mDatabase == null || !mDatabase.isOpen()) {
            mDatabase = mDbHelper.getReadableDatabase();
        }
    }

    private void dbClose() {
        mDatabaseRequests -= 1;
        if (mDatabaseRequests == 0) {
            mDatabase.close();
        }
    }

    private League cursorToLeague(Cursor cursor) {
        League l = new League();
        l.setLID(cursor.getInt(0));
        l.setName(cursor.getString(1));
        l.setAcronym(cursor.getString(2));
        return l;
    }

    private Section cursorToSection(Cursor cursor) {
        Section s = new Section();
        s.setSID(cursor.getInt(0));
        s.setLID(cursor.getInt(1));
        s.setNum(cursor.getString(2));
        s.setName(cursor.getString(3));
        return s;
    }

    private Rule cursorToRule(Cursor cursor) {
        Rule r = new Rule();
        r.setRID(cursor.getInt(0));
        r.setSID(cursor.getInt(1));
        if (!cursor.isNull(2)) {
            r.setParent_RID(cursor.getInt(2));
        }
        r.setNum(cursor.getString(3));
        r.setName(cursor.getString(4));
        r.setPureContents(Arrays.asList(cursor.getString(5).split("\n")));
        return r;
    }

    private ArrayList<Rule> makeTree(ArrayList<Rule> flatRules) {
        //TODO: Performance sucks, improve here.
        ArrayList<Rule> tree = new ArrayList<Rule>();

        //Get all of the rules that don't have parent ids ("top level" rules)
        //Iterate in reverse, remove as we find things.
        for (int i = flatRules.size() - 1; i >= 0; i--) {
            if (flatRules.get(i).getParent_RID() == null) {
                tree.add(flatRules.get(i));
                flatRules.remove(i);
            }
        }
        //Reverse to obtain the original order.
        Collections.reverse(tree);

        //Now iterate over the top level rules and find our sub rules.
        for (int k = 0; k < tree.size(); k++) {
            Rule parent = tree.get(k);
            ArrayList<Rule> subRules = new ArrayList<Rule>();
            //Same as above.
            for (int i = flatRules.size() - 1; i >= 0; i--) {
                if (flatRules.get(i).getParent_RID().equals(parent.getRID())) {
                    subRules.add(flatRules.get(i));
                    flatRules.remove(i);
                }
            }
            Collections.reverse(subRules);
            //Take the rules we found and assign them to the parent.
            parent.setSubRules(subRules);
            //Fix the original list.
            tree.set(k, parent);
        }

        return tree;
    }

    private SearchResult getResult (Rule r, String searchText, ArrayList<String> textFound) {
        String highlightText = "";
        Section section = getSectionById(r.getSID());
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
    //endregion


}
