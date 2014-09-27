package com.teebz.hrf.activities;

import com.teebz.hrf.R;
import com.teebz.hrf.entities.Rule;
import com.teebz.hrf.entities.Section;
import com.teebz.hrf.fragments.RuleDetailFragment;

import android.app.ActionBar;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.MenuItem;

public class RuleDetailActivity extends HRFActivity {
    private Rule mRule;
    private String mHighlightText;

    public RuleDetailActivity() { }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.rule_detail_main);

        //We may have come from another action, we may have come from a link. Figure out which.
        Bundle extras = getIntent().getExtras();
        Uri data = getIntent().getData();

        mHighlightText = null;
        int ruleTarget = -1;
        if (data != null) { //We came from a link
            String[] uriComponents = data.toString().split("//");
            String ruleNum = uriComponents[uriComponents.length - 1];

            //We don't know if this rule is a "parent" rule or not. The rule detail fragment shows
            //parent rules only (always shows children) so we need to find out which one we have.
            //Get the exact rule
            mRule = ruleDataServices.getRuleByNum(ruleNum, getLeagueId());
            ruleTarget = mRule.getRID();//Mark this rule as our target - makes the resulting view auto scroll to the link.
            if (mRule.getSubRules() == null || mRule.getSubRules().size() == 0){ //We don't have children, go get our parent.
                mRule = ruleDataServices.getParentRuleByNum(ruleNum, getLeagueId());
            }
        } else if (extras != null) { //We came from an action
            mRule = (Rule)extras.getSerializable(RuleDetailFragment.RULES_DETAIL_KEY);
            mHighlightText = extras.getString(RuleDetailFragment.RULES_DETAIL_SEARCH_TERM);
        } else if (savedInstanceState != null) {
            //Something happened that killed our state. Reload!
            mRule = (Rule)savedInstanceState.getSerializable(RuleDetailFragment.RULES_DETAIL_KEY);
            mHighlightText = savedInstanceState.getString(RuleDetailFragment.RULES_DETAIL_SEARCH_TERM);
        }
        // else: Should not be valid, where did we come from?

        //Have our info, reload the fragment.
        getFragmentManager().beginTransaction()
                .add(R.id.ruleDetailMain, RuleDetailFragment.newInstance(mRule, mHighlightText, ruleTarget))
                .commit();

        ActionBar bar = getActionBar();
        if (bar != null) {
            getActionBar().setDisplayHomeAsUpEnabled(true);
            getActionBar().setTitle(mRule.getName());
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(RuleDetailFragment.RULES_DETAIL_KEY, mRule);
        outState.putString(RuleDetailFragment.RULES_DETAIL_SEARCH_TERM, mHighlightText);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle up navigation here. All other option items will go to HRFActivity.
        switch (item.getItemId()) {
            case android.R.id.home:
                // Navigate up to the parent section. We may or may not have arrived from here.
                Intent upIntent = NavUtils.getParentActivityIntent(this);
                upIntent.putExtra(HRF_LOAD_ID, mRule.getSID());
                // Make sure we navigate here, not just "back" to the previous activity.
                upIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(upIntent);
                finish();

                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
