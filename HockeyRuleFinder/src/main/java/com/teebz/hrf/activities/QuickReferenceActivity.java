package com.teebz.hrf.activities;

import com.teebz.hrf.R;
import com.teebz.hrf.fragments.QuickReferenceFragment;
import com.teebz.hrf.listeners.QuickReferenceListItemClickListener;

import android.os.Bundle;
import android.view.View;

public class QuickReferenceActivity extends HRFActivity
                                    implements QuickReferenceListItemClickListener {
    public static final String QUICK_REF_KEY = "QUICK_REF_KEY";

    @Override
    public void onQuickRefListItemClick(View view, int position, String ruleId) {
        //Something was clicked in the reference list and is sending us a list of rules to
        //display on the right pane. Forward the rules onto the fragment & refresh.
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.quick_reference_main);

        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.quickReferenceContainer, QuickReferenceFragment.newInstance())
                    .commit();
        }

        //We want to set the title here, even though it is normally set by the fragment.
        //In this case, we know that the only reason to use this activity is because we are looking
        //at the "Signals" rule from the "Browse" area.
        setTitle("Signals");
    }
}
