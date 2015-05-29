package com.teebz.hrf.activities;

import com.crashlytics.android.Crashlytics;
import com.teebz.hrf.R;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;

public class SplashScreenActivity extends HRFActivity {
    private static int SPLASH_TIME_OUT = 1500;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Crashlytics.start(this);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.splash_main);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() { // This method will be executed once the timer is over
                // Start Main
                Intent i = new Intent(SplashScreenActivity.this, MainActivity.class);
                startActivity(i);

                // Close this out.
                finish();

                //Stop the standard "fly in" animation, switch to simple fade.
                overridePendingTransition(R.anim.fadein, R.anim.fadeout);
            }
        }, SPLASH_TIME_OUT);
    }
}
