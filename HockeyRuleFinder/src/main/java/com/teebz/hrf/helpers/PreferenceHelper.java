package com.teebz.hrf.helpers;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.teebz.hrf.activities.SettingsActivity;

public class PreferenceHelper {
    public static String getLeaguePreference(Context c) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(c);

        //Do we have a preference saved yet?
        String currentValue = sp.getString(SettingsActivity.PREF_LEAGUE_OPTION, null);

        //If nothing is saved, set it to the NHL.
        if (currentValue != null && !currentValue.isEmpty()) {
            return currentValue;
        }
        else {
            setLeaguePreference(c, "NHL");
            return "NHL";
        }
    }

    public static void setLeaguePreference(Context c, String acronym) {
        PreferenceManager.getDefaultSharedPreferences(c)
                .edit()
                .putString(SettingsActivity.PREF_LEAGUE_OPTION, acronym)
                .apply();
    }
}
