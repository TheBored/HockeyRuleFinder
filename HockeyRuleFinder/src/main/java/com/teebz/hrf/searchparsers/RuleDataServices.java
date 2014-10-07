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

    public Section getSectionById (int sectionId, Boolean includeRules) {
        dbOpen();
        Cursor cursor = mDatabase.query(DBHelper.TABLE_SECTION,
                DBHelper.SECTION_COLUMNS, "section_id="+sectionId, null, null, null, null);

        //There should only be one section in the cursor.
        cursor.moveToFirst();
        Section s = cursorToSection(cursor);
        cursor.close();

        if (includeRules) {
            //Have the section, get the corresponding rules under it.
            ArrayList<Rule> rules = getRules(null, s.getSID(), null, false);
            Rule[] ruleArr = new Rule[rules.size()];
            ruleArr = rules.toArray(ruleArr);
            s.setRules(ruleArr);
        }

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

    public int[] findRulesWithText(Integer leagueId, String text) {
        String[] justIdCol = { "rule_id", "parent_rule_id" };
        String whereClause = String.format("league_id=%s AND (text LIKE '%%%s%%' OR rule_name LIKE '%%%s%%')",
                leagueId != null ? leagueId.toString() : "league_id",
                text,
                text);

        dbOpen();
        Cursor cursor = mDatabase.query(true, DBHelper.TABLE_RULE,
                justIdCol, whereClause, null, null, null, null, null);
        cursor.moveToFirst();

        int[] ids = new int[cursor.getCount()];
        int i = 0;
        while (!cursor.isAfterLast()) {
            ids[i++] = cursor.isNull(1) ? cursor.getInt(0) : cursor.getInt(1); //Is parent id null? If so, use rule id.
            cursor.moveToNext();
        }
        cursor.close();
        dbClose();

        return ids;
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
    //endregion


}
