package com.teebz.hrf.activities;

import com.teebz.hrf.R;
import com.teebz.hrf.entities.Rule;
import com.teebz.hrf.entities.Section;
import com.teebz.hrf.fragments.RuleListFragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.MenuItem;

import java.util.List;

public class RuleListActivity extends HRFActivity {
    public static final String RULES_LIST_KEY = "RULES_LIST_KEY";
    private static Section mSection;
    private static List<Rule> mRules;

    public RuleListActivity() {
        super.menuBehavior = UpMenuBehavior.NavigateUp;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.rule_list_main);

        Bundle extras = getIntent().getExtras();

        if (extras != null) {
            int sectionId;
            if (extras.containsKey(HRF_LOAD_ID)) { //We navigated "Up" to get here.
                sectionId = extras.getInt(HRF_LOAD_ID);
            } else { //We navigated here from the section list.
                sectionId = extras.getInt(RuleListActivity.RULES_LIST_KEY);
            }
            mSection = mRuleDataServices.getSectionById(sectionId, false);
            mRules = mRuleDataServices.getRules(null, sectionId, null, false);
        }

        if (savedInstanceState != null) {
            String sectionId = savedInstanceState.getString("SectionId");
            //mSection = mSearcher.getSectionById(sectionId);
            //mRules = mSearcher.getRulesBySectionId(sectionId);
        }

        getFragmentManager().beginTransaction()
                .add(R.id.ruleListMain, RuleListFragment.newInstance(mRules))
                .commit();

        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setTitle(mSection.getName());
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putInt("SectionId", mSection.getSID());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle up navigation here. All other option items will go to HRFActivity.
        switch (item.getItemId()) {
            case android.R.id.home:
                // Navigate up to the main view, but focused on the "Browse" menu.
                Intent upIntent = NavUtils.getParentActivityIntent(this);
                upIntent.putExtra(MainActivity.SELECTED_MENU_OPTION, getString(R.string.title_section1));

                // Make sure we navigate here, not just "back" to the previous activity.
                upIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(upIntent);
                finish();

                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}