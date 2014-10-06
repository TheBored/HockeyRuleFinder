package com.teebz.hrf.searchparsers;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.widget.Toast;

import com.teebz.hrf.entities.League;
import com.teebz.hrf.entities.Rule;
import com.teebz.hrf.entities.SearchResult;
import com.teebz.hrf.entities.Section;

import java.io.File;
import java.lang.reflect.Array;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
            cursor.moveToNext();

            //We got the section, now run another query to get the rule range.
            String sql = String.format("SELECT * FROM (SELECT rule_num " +
                    "                                  FROM rule " +
                    "                                  WHERE section_id = %d AND parent_rule_id IS NULL " +
                    "                                  ORDER BY rule_id ASC LIMIT 1) " +
                    "                   UNION ALL " +
                    "                   SELECT * FROM (SELECT rule_num " +
                    "                                  FROM rule " +
                    "                                  WHERE section_id = %d AND parent_rule_id IS NULL " +
                    "                                  ORDER BY rule_id DESC LIMIT 1)"
                    , section.getSID(), section.getSID());

            Cursor subCursor = mDatabase.rawQuery(sql, null);
            subCursor.moveToFirst();
            String first = subCursor.getString(0);
            subCursor.moveToNext();
            String second = subCursor.getString(0);
            subCursor.close();
            if (first.equals("PREFIX")) {
                section.setRuleRange(String.format("%s - Rule %s", first, second));
            }
            else {
                section.setRuleRange(String.format("Rules %s - %s", first, second));
            }

            sections.add(section);
        }
        cursor.close();
        dbClose();

        //We have the section
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
        ArrayList<Rule> rules = getRules(null, s.getSID(), null, false);
        Rule[] ruleArr = new Rule[rules.size()];
        ruleArr = rules.toArray(ruleArr);
        s.setRules(ruleArr);

        dbClose();
        return s;
    }

    public ArrayList<Rule> getRules(Integer leagueId, Integer sectionId, int[] ruleIds, Boolean returnFlat) {
        //Make a where clause that effectively makes each column optional in the search.
        String whereClause = String.format("league_id=%s AND section_id=%s",
                leagueId != null ? leagueId.toString() : "league_id",
                sectionId != null ? sectionId.toString() : "section_id");

        //If we are also searching with specific ruleIds, add onto the query.
        if (ruleIds != null && ruleIds.length != 0) {
            //Holy crap Java doesn't have a String.join method. Ooookay.
            //Going bush league here because it isn't critical code.
            String ids = "";
            for (int i = 0; i < ruleIds.length; i++) {
                ids += Integer.toString(ruleIds[i]);
                if (i + 1 != ruleIds.length) { //If we aren't at the end
                    ids += ",";
                }
            }
            whereClause += String.format(" AND (rule_id IN (%s) OR parent_rule_id IN (%s))", ids, ids);
        }


        dbOpen();
        Cursor cursor = mDatabase.query(DBHelper.TABLE_RULE,
                DBHelper.RULE_COLUMNS, whereClause, null, null, null, null);

        ArrayList<Rule> rules = new ArrayList<Rule>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            Rule rule = cursorToRule(cursor);
            rules.add(rule);
            cursor.moveToNext();
        }
        cursor.close();
        dbClose();

        if (returnFlat) {
            return rules;
        }
        else {
            return makeTree(rules);
        }
    }

    public Rule getRule(Integer leagueId, Integer ruleId, String ruleNum) {
        String whereClause = null;
        //There are essentially two search modes.
        if (leagueId != null) { //1: Search by league/number.
            whereClause = String.format("league_id=%s AND rule_num=%s",
                    leagueId != null ? leagueId.toString() : "league_id",
                    ruleNum != null ? ruleNum : "rule_num");
        }
        else { //2: Search by ID.
            whereClause = String.format("(rule_id=%s AND parent_rule_id IS NULL) OR parent_rule_id=%s",
                    //Search for both columns, to give us a rule tree when possible.
                    ruleId != null ? ruleId.toString() : "rule_id",
                    ruleId != null ? ruleId.toString() : "parent_rule_id");
        }
        //Technically there is a third if no params are set, this should yield all rows.

        dbOpen();

        Cursor cursor = mDatabase.query(DBHelper.TABLE_RULE,
                DBHelper.RULE_COLUMNS, whereClause, null, null, null, null);

        ArrayList<Rule> rules = new ArrayList<Rule>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            Rule rule = cursorToRule(cursor);
            rules.add(rule);
            cursor.moveToNext();
        }
        cursor.close();
        dbClose();

        //If we only finished with one rule, we need to run again by ID so we can get the entire tree.
        if (rules.size() == 1) {
            Rule loneRule = rules.get(0);
            //It might be a parent rule or a child rule.
            if (loneRule.getParent_RID() != null) { //Child
                return getRule(null, loneRule.getParent_RID(), null);
            }
            else { //Parent
                return getRule(null, loneRule.getRID(), null);
            }
        }

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

    private ArrayList<Rule> findRulesWithText(Integer leagueId, String text) {
        String whereClause = String.format("league_id=%s AND (text LIKE '%%%s%%' OR rule_name LIKE '%%%s%%')",
                leagueId != null ? leagueId.toString() : "league_id",
                text,
                text);

        dbOpen();
        Cursor cursor = mDatabase.query(DBHelper.TABLE_RULE,
                DBHelper.RULE_COLUMNS, whereClause, null, null, null, null);

        ArrayList<Rule> rules = new ArrayList<Rule>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            Rule rule = cursorToRule(cursor);
            rules.add(rule);
            cursor.moveToNext();
        }
        cursor.close();
        dbClose();

        //Always return flat result.
        return rules;
    }

    public List<SearchResult> searchRules(String text, int leagueId) {
        List<SearchResult> results = new ArrayList<SearchResult>();
        if (text.isEmpty()) {
            return results; //No search text = no results.
        }

        //First up, find any rules that might have the text we are interested in.
        ArrayList<Rule> directReferences = findRulesWithText(leagueId, text);

        //Next thing, get all of the parent RuleIds that we are interested in.
        int[] ruleIds = new int[directReferences.size()];
        for (int i = 0; i < directReferences.size(); i++) {
            Rule r = directReferences.get(i);
            ruleIds[i] = r.getParent_RID() == null ? r.getRID() : r.getParent_RID();
        }

        //With these Ids, go back to the database and get all of those rules again with any parents
        //that aren't already in the list. Also puts them into a tree.
        ArrayList<Rule> possibleReferences = getRules(leagueId, null, ruleIds, false);

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

        //Iterate through all of the rules once and sort them into a HashMap
        Map<Integer, ArrayList<Rule>> ruleMap = new HashMap<Integer, ArrayList<Rule>>();
        for (Rule r : flatRules) {
            //Insert by parent id, 0 if no parent.
            int insertIndex = r.getParent_RID() != null ? r.getParent_RID() : 0;
            if (!ruleMap.containsKey(insertIndex)) {
                ruleMap.put(insertIndex, new ArrayList<Rule>());
            }
            ruleMap.get(insertIndex).add(r);
        }

        //Get the parents, iterate through them and collect the children.
        ArrayList<Rule> tree = ruleMap.get(0);
        for (int i = 0; i < tree.size(); i++) {
            Rule parent = tree.get(i);
            ArrayList<Rule> children = ruleMap.get(parent.getRID());
            parent.setSubRules(children);
            tree.set(i, parent);
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
