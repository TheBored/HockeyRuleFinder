package com.teebz.hrf.activities;

import com.crashlytics.android.Crashlytics;
import com.teebz.hrf.R;
import com.teebz.hrf.entities.Rule;
import com.teebz.hrf.fragments.NavigationDrawerFragment;
import com.teebz.hrf.fragments.OfficialsListFragment;
import com.teebz.hrf.fragments.QuickReferenceFragment;
import com.teebz.hrf.fragments.RuleDetailFragment;
import com.teebz.hrf.fragments.SearchFragment;
import com.teebz.hrf.fragments.SectionListFragment;
import com.teebz.hrf.listeners.OfficialsListItemClickListener;
import com.teebz.hrf.listeners.QuickReferenceListItemClickListener;
import com.teebz.hrf.listeners.SearchListItemClickListener;
import com.teebz.hrf.listeners.SectionListItemClickListener;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.view.View;

public class MainActivity extends HRFActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks,
                   SectionListItemClickListener,
                   QuickReferenceListItemClickListener,
                   SearchListItemClickListener,
                   OfficialsListItemClickListener {

    private static final String SEARCH_LAST_TEXT = "search_last_text";
    public static final String SELECTED_MENU_OPTION = "selected_menu_option";

    private CharSequence mTitle;

    //Preserves the last search if required.
    private SearchFragment mSearchFragment;

    private NavigationDrawerFragment mNavigationDrawerFragment;

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        //Sometimes we forcefully start the MainActivity to make sure that we are in the proper
        //state when this loads. If this happens, get the menu option that we are expecting and
        //set appropriately.
        String menuOption = getMenuOption(intent);
        if (menuOption != null) {
            mNavigationDrawerFragment.setSelection(menuOption);

            //If we come here, also reset the search.
            mSearchFragment = null;
        }
    }

    private String getMenuOption(Intent intent) {
        return intent.getStringExtra(SELECTED_MENU_OPTION);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Crashlytics.start(this);


        String mSelectedOption = getString(R.string.title_section0);
        if (savedInstanceState != null) {
            String searchText = savedInstanceState.getString(SEARCH_LAST_TEXT);
            mSearchFragment = SearchFragment.newInstance(searchText);

            if (savedInstanceState.containsKey(SELECTED_MENU_OPTION)) {
                mSelectedOption = savedInstanceState.getString(SELECTED_MENU_OPTION);
            }
        }

        //If we were sent here by an intent, check that out.
        String menuOption = getMenuOption(getIntent());
        if (menuOption != null) {
            mSelectedOption = menuOption;
        }

        setContentView(R.layout.main_activity);

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getFragmentManager().findFragmentById(R.id.navigation_drawer);

        //Don't set the selection if we got it from a saved state (resuming from memory)
        if (!mNavigationDrawerFragment.isFromSavedInstanceState()) {
            mNavigationDrawerFragment.setSelection(mSelectedOption);
        }

        mTitle = getTitle();

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));
    }

    @Override
    public void onSectionListItemClick(View view, int position, int sectionId) {
        Intent newActivity = new Intent(view.getContext(), RuleListActivity.class);
        newActivity.putExtra(RuleListActivity.RULES_LIST_KEY, sectionId);
        startActivity(newActivity);
    }

    @Override
    public void onQuickRefListItemClick(View view, int position, String ruleNum) {
        //Quick ref links by rule num, can't supply an ID.
        Rule clickedRule = mRuleDataServices.getRuleByNum(ruleNum, getLeagueId());
        Intent newActivity = new Intent(view.getContext(), RuleDetailActivity.class);
        newActivity.putExtra(RuleDetailFragment.RULES_DETAIL_KEY, clickedRule);
        startActivity(newActivity);
    }

    @Override
    public void onSearchListItemClick(View view, int position, int ruleId, String highlightText) {
        Intent newActivity = new Intent(this, RuleDetailActivity.class);
        newActivity.putExtra(RuleDetailFragment.RULES_DETAIL_KEY, mRuleDataServices.getRuleById(ruleId));
        newActivity.putExtra(RuleDetailFragment.RULES_DETAIL_SEARCH_TERM, highlightText);
        startActivity(newActivity);
    }

    @Override
    public void onOfficialsListItemClick(View view, int position, String jerseyNumber) {
        Intent newActivity = new Intent(this, SingleOfficialActivity.class);
        newActivity.putExtra(SingleOfficialActivity.SINGLE_OFFICIAL_KEY, jerseyNumber);
        startActivity(newActivity);
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        FragmentManager fragmentManager = getFragmentManager();
        switch (position) {
            case 0:
                mTitle = getString(R.string.title_section0);
                fragmentManager.beginTransaction()
                        .replace(R.id.main_container, QuickReferenceFragment.newInstance())
                        .commit();
                break;
            case 1:
                mTitle = getString(R.string.title_section1);
                fragmentManager.beginTransaction()
                        .replace(R.id.main_container, SectionListFragment.newInstance())
                        .commit();
                break;
            case 2:
                mTitle = getString(R.string.title_section2);
                // If we don't have a search fragment yet, start a new one.
                if (mSearchFragment == null) {
                    mSearchFragment = SearchFragment.newInstance(null);
                }
                fragmentManager.beginTransaction()
                        .replace(R.id.main_container, mSearchFragment)
                        .commit();
                break;
            case 3:
                mTitle = getString(R.string.title_section3);
                fragmentManager.beginTransaction()
                        .replace(R.id.main_container, OfficialsListFragment.newInstance())
                        .commit();
                break;
        }
        restoreActionBar();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mSearchFragment != null) {
            outState.putString(SEARCH_LAST_TEXT, mSearchFragment.getSearchTerm());
        }
    }

    public void restoreActionBar() {
        ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Confirm Exit")
                .setMessage("Do you want to close Hockey Rule Finder?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                })
                .setNegativeButton("No", null)
                .show();
    }
}
