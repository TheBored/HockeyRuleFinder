package com.teebz.hrf.activities;

import com.teebz.hrf.entities.League;
import com.teebz.hrf.helpers.ApplicationHelper;
import com.teebz.hrf.R;
import com.teebz.hrf.helpers.PreferenceHelper;
import com.teebz.hrf.searchparsers.RuleDataServices;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteCantOpenDatabaseException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class HRFActivity extends Activity {
    protected static final String HRF_LOAD_ID = "HRF_LOAD_ID";
    private static String DB_PATH = "/data/data/com.teebz.hrf/databases/";
    private static String DB_NAME = "rules.sqlite";
    private static String TEMP_DB_NAME = "tempRules.sqlite";

    private int mLeagueId;
    protected boolean mShowMenu;
    protected UpMenuBehavior menuBehavior;
    protected RuleDataServices mRuleDataServices;

    protected enum UpMenuBehavior {
        None,
        Finish,
        NavigateUp
    }

    public HRFActivity() {
        //Default values in case they are not set.
        mShowMenu = true;
        menuBehavior = UpMenuBehavior.None;
    }

    public int getLeagueId() {
        //Return what league we are set to view.
        return mLeagueId;
    }

    public void updateSetLeague() {
        String leaguePreference = PreferenceHelper.getLeaguePreference(getBaseContext());
        League league = mRuleDataServices.getLeagueByAcronym(leaguePreference);
        mLeagueId = league.getLID();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //First time we are running anything. Check and see if we need to copy the database over.
        try {
            if (!isDatabaseCurrent()) {
                copyDatabase(DB_PATH + DB_NAME);
            }
        } catch (IOException e) {
            //Do nothing.
        }

        //DB is good, establish the rule services and get/set our league pref.
        mRuleDataServices = RuleDataServices.getRuleDataServices(getBaseContext());
        updateSetLeague();

        //Check the theme preference here. Set if need be.
        if (isDarkTheme()) {
            setTheme(R.style.HRFDarkTheme);
        } else {
            setTheme(R.style.HRFLightTheme);
        }
    }

    protected boolean isDarkTheme() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        return sp.getBoolean(SettingsActivity.PREF_THEME_OPTION, true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        if (mShowMenu){
            getMenuInflater().inflate(R.menu.main, menu);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle application wide menu options here, specific items can be overridden where needed.
        // Handle action bar item clicks here.
        switch (item.getItemId()) {
            case R.id.action_settings:
                Intent newActivity = new Intent(getBaseContext(), SettingsActivity.class);
                startActivity(newActivity);
                return true;
            case R.id.action_feedback:
                return ApplicationHelper.startFeedbackEmail(this);
            case android.R.id.home:
                if (menuBehavior.equals(UpMenuBehavior.Finish)) {
                    finish();
                    return true;
                } else if (menuBehavior.equals(UpMenuBehavior.NavigateUp)) {
                    NavUtils.navigateUpFromSameTask(this);
                    return true;
                }
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Check if the database already exist to avoid re-copying the file each time you open the application.
     * @return true if it exists, false if it doesn't
     */
    private boolean isDatabaseCurrent(){
        String localPath = DB_PATH + DB_NAME;
        String tempPath = DB_PATH + TEMP_DB_NAME;
        String version = null;
        try{
            //Before we do anything - check if we need to create the database folder.
            File dir = new File(DB_PATH);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            String localVersion = null;
            try {
                //Get the local (existing) version.
                localVersion = getDatabaseVersion(localPath);
            }
            catch (Exception e) {
                Log.d("HRFActivity", "Local database does not exist for version check.");
            }

            //Copy the APK database over so we can easily open it for comparison.
            copyDatabase(tempPath);

            //Get the APK database version
            String apkVersion = getDatabaseVersion(tempPath);

            //Now that we're done with the apk version of the database, delete it.
            File tmpDB = new File(tempPath);
            Boolean delSuccess = tmpDB.delete();

            //Do the versions match?
            return localVersion != null && localVersion.equals(apkVersion);
        }catch(Exception e){
            //Anything breaks above - copy the database.
            return false;
        }
    }

    /**
     * Copies your database from your local assets-folder to the just created empty database in the
     * system folder, from where it can be accessed and handled.
     * This is done by transfering bytestream.
     * */
    private void copyDatabase(String outputLocation) throws IOException {
        //Open your local db as the input stream
        InputStream myInput = getAssets().open(DB_NAME);

        //Open the empty db as the output stream
        OutputStream myOutput = new FileOutputStream(outputLocation);

        //transfer bytes from the inputfile to the outputfile
        byte[] buffer = new byte[1024];
        int length;
        while ((length = myInput.read(buffer))>0){
            myOutput.write(buffer, 0, length);
        }

        //Close the streams
        myOutput.flush();
        myOutput.close();
        myInput.close();
    }

    private String getDatabaseVersion(String path) {
        SQLiteDatabase localDB = SQLiteDatabase.openDatabase(path, null, SQLiteDatabase.OPEN_READONLY);

        //Hard coded as I do not plan on changing this in the future. Can revise if req'd.
        Cursor cursor = localDB.query("app_info", new String[] { "value" }, "key='VERSION'", null, null, null, null);
        cursor.moveToFirst();
        String version = cursor.getString(0);
        cursor.close();
        localDB.close();

        return version;
    }
}
