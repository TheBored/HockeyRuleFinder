package com.teebz.hrf.listeners;

import android.view.View;

public interface OfficialsListItemClickListener {
    /**
     * This method will be invoked when an item in the ListFragment is
     * clicked
     */
    void onOfficialsListItemClick(View view, int position, String jerseyNumber);
}
