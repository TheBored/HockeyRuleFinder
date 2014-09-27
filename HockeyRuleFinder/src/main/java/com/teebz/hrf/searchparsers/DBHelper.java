package com.teebz.hrf.searchparsers;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.teebz.hrf.entities.Section;

import java.util.ArrayList;

public class DBHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "rules.sqlite";
    private static final int DATABASE_VERSION = 1;

    public static final String TABLE_LEAGUE = "league";
    public static final String[] LEAGUE_COLUMNS = { "league_id", "name", "acronym" };

    public static final String TABLE_SECTION = "section";
    public static final String[] SECTION_COLUMNS = { "section_id", "league_id", "section_num", "section_name", "section_order" };

    public static final String TABLE_RULE = "rule";
    public static final String[] RULE_COLUMNS = { "rule_id", "section_id", "parent_rule_id", "rule_num", "rule_name", "text", "rule_order" };

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        //Hope this is never called.
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //This should never be called either.
    }
}
