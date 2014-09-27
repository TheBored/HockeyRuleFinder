package com.teebz.hrf.activities;

import com.teebz.hrf.R;
import com.teebz.hrf.entities.Official;
import com.teebz.hrf.fragments.SingleOfficialFragment;
import com.teebz.hrf.searchparsers.OfficialsSearcher;

import android.os.Bundle;

public class SingleOfficialActivity extends HRFActivity {
    public static final String SINGLE_OFFICIAL_KEY = "SINGLE_OFFICIAL_KEY";
    private String mJerseyNumber;

    public SingleOfficialActivity() {
        super.mShowMenu = false;
        super.menuBehavior = UpMenuBehavior.Finish;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.single_official_activity);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            mJerseyNumber = extras.getString(SingleOfficialActivity.SINGLE_OFFICIAL_KEY);
        }

        OfficialsSearcher searcher = OfficialsSearcher.getSearcher(getAssets());

        //Set the appropriate title
        Official o = searcher.getOfficialByJerseyNumber(mJerseyNumber);
        setTitle(o.name);

        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.singleOfficialMain, SingleOfficialFragment.newInstance(mJerseyNumber))
                    .commit();
        }

        getActionBar().setDisplayHomeAsUpEnabled(true);
    }
}
