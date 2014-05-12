package com.teebz.hrf.fragments;

import com.teebz.hrf.R;
import com.teebz.hrf.entities.SearchResult;
import com.teebz.hrf.listeners.SearchListItemClickListener;
import com.teebz.hrf.searchparsers.RuleSearcher;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.Html;
import android.text.Spanned;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

public class SearchFragment extends android.app.Fragment {
    private static final String PREVIOUS_SEARCH_TERM = "previous_search_term";

    private List<SearchResult> mResults;
    private RuleSearcher mRuleSearcher;
    private SearchListItemClickListener mItemClickListener;
    private EditText mEditTextBox;
    private ListView mListView;
    private Handler mSearchResultHandler;
    private String mPreviousSearch = null;

    public static SearchFragment newInstance(String previousSearch) {
        SearchFragment fragment = new SearchFragment();
        fragment.setSearchTerm(previousSearch);
        return fragment;
    }

    public String getSearchTerm() {
        if (mEditTextBox != null) {
            return mEditTextBox.getText().toString();
        } else {
            return null;
        }
    }

    public void setSearchTerm(String previousSearch) {
        this.mPreviousSearch = previousSearch;
    }

    public SearchFragment() { }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null){
            mPreviousSearch = savedInstanceState.getString(PREVIOUS_SEARCH_TERM);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mItemClickListener = (SearchListItemClickListener)activity;
        }
        catch (Exception e) {
            Toast.makeText(activity.getBaseContext(), "Click listener failed", Toast.LENGTH_LONG).show();
        }
        populateListView(activity.findViewById(android.R.id.content).getRootView());
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putString(PREVIOUS_SEARCH_TERM, getSearchTerm());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //Inflate the fragment view
        View fragmentView = inflater.inflate(R.layout.search_fragment, container, false);

        mRuleSearcher = RuleSearcher.getSearcher(fragmentView.getContext().getAssets());
        mEditTextBox = (EditText)fragmentView.findViewById(R.id.txtSearch);
        mListView = (ListView)fragmentView.findViewById(R.id.searchListView);

        //Make sure the edit box gets focus when we load this framgent.
        mEditTextBox.requestFocus();
        mEditTextBox.postDelayed(new Runnable() {
            @Override
            public void run() {
                InputMethodManager keyboard = (InputMethodManager) mEditTextBox.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                keyboard.showSoftInput(mEditTextBox, InputMethodManager.SHOW_IMPLICIT);
            }
        }, 200);

        setHandlers(fragmentView);

        if (mPreviousSearch != null) {
            mEditTextBox.setText(mPreviousSearch);
            updateSearchResults();
        }

        return fragmentView;
    }

    private void setHandlers(View parentView) {
        mSearchResultHandler = new Handler();

        ListView list = (ListView) parentView.findViewById(R.id.searchListView);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                //A section was clicked, get the corresponding rules to pass to the parent.
                SearchResult sr = mResults.get(position);

                //Alert our parent that a click happened.
                mItemClickListener.onSearchListItemClick(view, position, sr.ruleMatch.id, getSearchTerm());
            }
        });

        mEditTextBox.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                mSearchResultHandler.removeCallbacksAndMessages(null);
                int duration = 500;
                if (getSearchTerm().isEmpty()) {
                    duration = 0; //Empty search goes instant.
                }

                mSearchResultHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        updateSearchResults();
                    }
                }, duration);
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });
    }

    private void populateListView(View parentView) {
        Activity act = getActivity();
        if (mResults != null && act != null) {
            ArrayAdapter<SearchResult> adapter = new SearchListAdapter();
            mListView.setAdapter(adapter);

            View errorText = parentView.findViewById(R.id.searchErrorText);
            if (mResults.size() == 0) { //If we don't have results, show the error text.
                errorText.setVisibility(View.VISIBLE);
            }
            else {
                errorText.setVisibility(View.GONE);
            }
        }
    }

    private void updateSearchResults() {
        mResults = mRuleSearcher.searchRules(getSearchTerm());
        View rootView = mEditTextBox.getRootView();
        populateListView(rootView);
    }

    private class SearchListAdapter extends ArrayAdapter<SearchResult> {
        public SearchListAdapter() {
            super(getActivity(), R.layout.search_row, mResults);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View itemView = convertView;
            if (itemView == null) {
                itemView = getActivity().getLayoutInflater().inflate(R.layout.search_row, parent, false);
            }

            SearchResult current = mResults.get(position);

            TextView txtRuleContents = (TextView)itemView.findViewById(R.id.ruleTextView);
            //Convert it to display the various HTML tags.
            Spanned htmlContents = Html.fromHtml(current.highlightText);
            txtRuleContents.setText(htmlContents);

            TextView txtRuleId = (TextView)itemView.findViewById(R.id.ruleNumberView);
            txtRuleId.setText(current.ruleMatch.id);

            TextView txtSectionName = (TextView)itemView.findViewById(R.id.ruleSectionView);
            txtSectionName.setText(current.sectionMatch.name);

            TextView txtRuleName = (TextView)itemView.findViewById(R.id.ruleTitleView);
            txtRuleName.setText(current.ruleMatch.name);

            return itemView;
        }
    }
}
