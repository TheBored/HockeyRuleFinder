package com.teebz.hrf.activities;

import com.teebz.hrf.R;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.CompoundButton;
import android.widget.Switch;

public class SettingsActivity extends HRFActivity {
    public final static String PREF_THEME_OPTION = "pref_theme_option";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_main);

        Switch onOffSwitch = (Switch)  findViewById(R.id.themeSwitch);
        onOffSwitch.setChecked(isDarkTheme());
        onOffSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences sp = PreferenceManager
                        .getDefaultSharedPreferences(getBaseContext());
                sp.edit().putBoolean(PREF_THEME_OPTION, isChecked).apply();
            }
        });

        getActionBar().setDisplayHomeAsUpEnabled(true);
    }
}
