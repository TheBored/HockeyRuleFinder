package com.teebz.hrf.activities;

import com.crashlytics.android.Crashlytics;
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
import android.view.Window;

public class RuleDetailActivity extends HRFActivity {
    private Rule mRule;
    private String mHighlightText;

    public RuleDetailActivity() { }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Sometimes we don't get the actionbar that we expect. Add in a request to ensure it's there.
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);

        setContentView(R.layout.rule_detail_main);

        //We may have come from another action, we may have come from a link. Figure out which.
        Bundle extras = getIntent().getExtras();
        Uri data = getIntent().getData();

        //Set defaults
        mHighlightText = null;
        int ruleTarget = -1;

        /*
         * Possible entry points: Link, Action, reload from memory. Check each one if the previous was
         * not successful.
         */
        //Coming from link
        if (data != null) {
            String[] uriComponents = data.toString().split("//");
            String ruleNum = uriComponents[uriComponents.length - 1];

            //We don't know if this rule is a "parent" rule or not. The rule detail fragment shows
            //parent rules only (always shows children) so we need to find out which one we have.
            //Get the rule and its children.
            mRule = mRuleDataServices.getRule(getLeagueId(), null, ruleNum);

            if (!mRule.getNum().equals(ruleNum)) {
                //If the rule we got back doesn't match the target (we got a parent back)
                //then look through the children to find our target.
                for (Rule child : mRule.getSubRules()) {
                    if (child.getNum().equals(ruleNum)) {
                        ruleTarget = child.getRID();
                    }
                }
            }
            else {
                ruleTarget = mRule.getRID();
            }
        }

        //Coming from action.
        if (mRule == null && extras != null) {
            mRule = (Rule)extras.getSerializable(RuleDetailFragment.RULES_DETAIL_KEY);
            mHighlightText = extras.getString(RuleDetailFragment.RULES_DETAIL_SEARCH_TERM);
        }

        //Coming from saved state.
        if (mRule == null && savedInstanceState != null) {
            //Something happened that killed our state. Reload!
            mRule = (Rule)savedInstanceState.getSerializable(RuleDetailFragment.RULES_DETAIL_KEY);
            mHighlightText = savedInstanceState.getString(RuleDetailFragment.RULES_DETAIL_SEARCH_TERM);
        }

        if (mRule != null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.ruleDetailMain, RuleDetailFragment.newInstance(mRule, mHighlightText, ruleTarget))
                    .commit();

            getActionBar().setDisplayHomeAsUpEnabled(true);
            getActionBar().setTitle(mRule.getName());
        }
        else { //If mRule is still null, we have a problem.
            //Save some info so we can figure out whats going on.
            Crashlytics.setString("HighlightText", mHighlightText);
            Crashlytics.setInt("RuleTarget", ruleTarget);
            Crashlytics.setString("IntentData", data != null ? data.toString() : "No Intent Data");
            Crashlytics.setString("BundleExtras", extras != null ? "Exists" : "Does not exist");
            Crashlytics.logException(new Exception("mRule is null during RuleDetailActivity load."));

            //App was going to crash, restart back at the main menu. Exception was logged and sent to me though.
            Intent i = getBaseContext().getPackageManager()
                    .getLaunchIntentForPackage( getBaseContext().getPackageName() );
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(i);
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
