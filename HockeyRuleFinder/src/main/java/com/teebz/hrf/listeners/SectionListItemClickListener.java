package com.teebz.hrf.listeners;

import android.view.View;

public interface SectionListItemClickListener {
    /**
     * This method will be invoked when an item in the ListFragment is
     * clicked
     */
    void onSectionListItemClick(View view, int position, String sectionId);
}