package com.teebz.hrf.activities;

import com.teebz.hrf.Helpers;
import com.teebz.hrf.R;
import com.teebz.hrf.searchparsers.RuleDataServices;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuItem;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class HRFActivity extends Activity {
    protected static final String HRF_LOAD_ID = "HRF_LOAD_ID";
    private static String DB_PATH = "/data/data/com.teebz.hrf/databases/";
    private static String DB_NAME = "rules.sqlite";

    protected boolean showMenu;
    protected UpMenuBehavior menuBehavior;
    protected RuleDataServices ruleDataServices;

    protected enum UpMenuBehavior {
        None,
        Finish,
        NavigateUp
    }

    public HRFActivity() {
        //Default values in case they are not set.
        showMenu = true;
        menuBehavior = UpMenuBehavior.None;
    }

    protected int getLeagueId() {
        //Return what league we are set to view.
        return 1; //TODO: Don't hardcode league id.
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ruleDataServices = RuleDataServices.getRuleDataServices(getBaseContext());

        //TODO: Check database size, if different then do this.
        //First time we are running anything. Check and see if we need to copy the database over.
        try {
            if (!checkDatabase()) {
                copyDatabase();
            }
        } catch (IOException e) {
            //Do nothing.
        }

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
        if (showMenu){
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
                return Helpers.startFeedbackEmail(this);
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
    private boolean checkDatabase(){
        SQLiteDatabase checkDB = null;

        try{
            String myPath = DB_PATH + DB_NAME;
            checkDB = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);
        }catch(SQLiteException e){
            //database does't exist yet.
        }

        if(checkDB != null){
            checkDB.close();
        }
        return checkDB != null;
    }

    /**
     * Copies your database from your local assets-folder to the just created empty database in the
     * system folder, from where it can be accessed and handled.
     * This is done by transfering bytestream.
     * */
    private void copyDatabase() throws IOException {
        //Open your local db as the input stream
        InputStream myInput = getAssets().open(DB_NAME);

        // Path to the just created empty db
        String outFileName = DB_PATH + DB_NAME;

        //Open the empty db as the output stream
        OutputStream myOutput = new FileOutputStream(outFileName);

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
}
