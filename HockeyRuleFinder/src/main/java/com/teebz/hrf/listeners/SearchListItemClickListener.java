package com.teebz.hrf.listeners;

import android.view.View;

public interface SearchListItemClickListener {
    void onSearchListItemClick(View view, int position, int ruleid, String highlightText);
}