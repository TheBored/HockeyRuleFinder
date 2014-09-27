package com.teebz.hrf.listeners;

import android.view.View;

public interface QuickReferenceListItemClickListener {
    /**
     * This method will be invoked when an item in the ListFragment is
     * clicked
     */
    void onQuickRefListItemClick(View view, int position, String ruleNum);
}
