package com.teebz.hrf.activities;

import com.teebz.hrf.R;
import com.teebz.hrf.entities.League;
import com.teebz.hrf.helpers.PreferenceHelper;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import java.util.ArrayList;

public class SettingsActivity extends HRFActivity {
    public final static String PREF_THEME_OPTION = "pref_theme_option";
    public final static String PREF_LEAGUE_OPTION = "pref_league_option";

    private SharedPreferences preferenceManager = null;

    private Switch mDarkThemeSwitch = null;
    private Spinner mLeagueSpinner = null;
    private ArrayList<League> mLeagues = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_main);

        preferenceManager = PreferenceManager.getDefaultSharedPreferences(getBaseContext());

        mDarkThemeSwitch = (Switch) findViewById(R.id.themeSwitch);
        mLeagueSpinner = (Spinner)findViewById(R.id.leagueSpinner);
        mLeagues = mRuleDataServices.getAllLeagues();

        //Set the content of the league spinner.
        //Build a simple array of league acronyms.
        String[] leagues = new String[mLeagues.size()];
        for (int i = 0; i < mLeagues.size(); i++) {
            leagues[i] = mLeagues.get(i).getAcronym();
        }
        //Set the adapter.
        ArrayAdapter spinnerArrayAdapter = new ArrayAdapter(this,
                android.R.layout.simple_spinner_dropdown_item,
                leagues);
        mLeagueSpinner.setAdapter(spinnerArrayAdapter);

        loadCurrentValues();
        setListeners();

        getActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void loadCurrentValues() {
        mDarkThemeSwitch.setChecked(isDarkTheme());

        //Do we have a preference saved yet?
        String currentValue = PreferenceHelper.getLeaguePreference(getBaseContext());

        //At a minimum, we got back a league that existed at one point. Check to make sure it still does.
        Integer leagueIndex = getLeagueIndex(currentValue);
        if (leagueIndex != null) {
            mLeagueSpinner.setSelection(leagueIndex);
        }
        else {
            //The league saved in prefs is no good for some reason. Save NHL and set that.
            PreferenceHelper.setLeaguePreference(getBaseContext(), "NHL");

            //Set the spinner value
            Integer nhlIndex = getLeagueIndex("NHL");
            mLeagueSpinner.setSelection(nhlIndex);

            //Not sure how we'd even get here with an invalid league, but set the activity regardless.
            ((HRFActivity)getParent()).updateSetLeague();
        }
    }

    private Integer getLeagueIndex(String acronym) {
        for (int i = 0; i < mLeagues.size(); i++) {
            if (acronym.equals(mLeagues.get(i).getAcronym())) {
                return i;
            }
        }
        return null;
    }

    private void setListeners () {
        //On/off switch
        mDarkThemeSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                preferenceManager.edit().putBoolean(PREF_THEME_OPTION, isChecked).apply();
            }
        });

        //League spinner
        mLeagueSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                //Something changed, get the league.
                League newLeague = mLeagues.get(position);

                //Set our new pref.
                PreferenceHelper.setLeaguePreference(getBaseContext(), newLeague.getAcronym());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // Don't do anything.
            }

        });
    }
}
