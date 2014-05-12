package com.teebz.hrf.activities;

import com.teebz.hrf.Helpers;
import com.teebz.hrf.R;
import com.teebz.hrf.searchparsers.RuleSearcher;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuItem;

public class HRFActivity extends Activity {
    protected static final String HRF_LOAD_ID = "HRF_LOAD_ID";

    protected boolean showMenu;
    protected UpMenuBehavior menuBehavior;
    protected RuleSearcher searcher;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        searcher = RuleSearcher.getSearcher(getAssets());

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
}
