package com.teebz.hrf.activities;

import com.teebz.hrf.R;
import com.teebz.hrf.fragments.SingleImageFragment;

import android.os.Bundle;

public class SingleImageActivity extends HRFActivity {
    public static final String SINGLE_IMAGE_KEY = "SINGLE_IMAGE_KEY";
    public static final String SINGLE_IMAGE_TITLE = "SINGLE_IMAGE_TITLE";
    public static final String SINGLE_IMAGE_FOLDER = "SINGLE_IMAGE_FOLDER";

    public SingleImageActivity() {
        super.menuBehavior = UpMenuBehavior.Finish;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.single_image_activity);

        Bundle extras = getIntent().getExtras();
        String imageName = extras.getString(SingleImageActivity.SINGLE_IMAGE_KEY);
        String title = extras.getString(SingleImageActivity.SINGLE_IMAGE_TITLE);
        String folder = extras.getString(SingleImageActivity.SINGLE_IMAGE_FOLDER);

        setTitle(title);

        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.singleImageMain, SingleImageFragment.newInstance(imageName, folder))
                    .commit();
        }

        getActionBar().setDisplayHomeAsUpEnabled(true);
    }
}
