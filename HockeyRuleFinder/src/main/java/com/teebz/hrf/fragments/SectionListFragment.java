package com.teebz.hrf.fragments;

import com.teebz.hrf.R;
import com.teebz.hrf.activities.HRFActivity;
import com.teebz.hrf.entities.Rule;
import com.teebz.hrf.entities.Section;
import com.teebz.hrf.listeners.SectionListItemClickListener;
import com.teebz.hrf.searchparsers.RuleDataServices;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.widget.ListViewAutoScrollHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SectionListFragment extends android.app.Fragment {
    private List<Section> mSections;
    private SectionListItemClickListener mItemClickListener;

    public static SectionListFragment newInstance() {
        return new SectionListFragment();
    }

    public SectionListFragment() { }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mItemClickListener = (SectionListItemClickListener)activity;
        }
        catch (Exception e) {
            Toast.makeText(activity.getBaseContext(), "Click listener failed", Toast.LENGTH_LONG).show();
        }

        RuleDataServices rds = RuleDataServices.getRuleDataServices(activity.getBaseContext());
        HRFActivity parentActivity = (HRFActivity)activity;
        mSections = rds.getAllSections(parentActivity.getLeagueId());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //Inflate the fragment view
        View fragmentView = inflater.inflate(R.layout.section_list_fragment, container, false);

        //Add items to the list & click events.
        populateListView(fragmentView);
        registerClickCallback(fragmentView);

        //Hide the keyboard if it happens to be up
        InputMethodManager keyboard = (InputMethodManager) container.getContext()
            .getSystemService(Context.INPUT_METHOD_SERVICE);
        keyboard.hideSoftInputFromWindow(container.getWindowToken(), 0);

        return fragmentView;
    }

    private void populateListView(View theView) {
        //Now that we have the sections, go ahead and set the list adapter.
        if (mSections != null) {
            ArrayAdapter<Section> adapter = new SectionListAdapter();
            ListView list = (ListView) theView.findViewById(R.id.listViewMain);
            list.setAdapter(adapter);
        }
    }

    private void registerClickCallback(View theView) {
        ListView list = (ListView) theView.findViewById(R.id.listViewMain);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                //A section was clicked, get the corresponding section to pass to the parent.
                Section s = mSections.get(position);

                //Alert our parent that a click happened.
                mItemClickListener.onSectionListItemClick(view, position, s.getSID());
            }
        });

    }

    private class SectionListAdapter extends ArrayAdapter<Section> {
        public SectionListAdapter() {
            super(getActivity(), R.layout.section_row, mSections);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View itemView = convertView;
            if (itemView == null) {
                itemView = getActivity().getLayoutInflater().inflate(R.layout.section_row, parent, false);
            }

            Section current = mSections.get(position);

            TextView txtSectionName = (TextView)itemView.findViewById(R.id.txtSectionName);
            txtSectionName.setText(current.getName());

            //Get the range of rules within this one.
            Rule first = current.getRules()[0];
            Rule last = current.getRules()[current.getRules().length - 1];
            String range = "";

            TextView txtSectionRange = (TextView)itemView.findViewById(R.id.txtSectionRange);
            if (first.getNum().equals("PREFIX")) {
                range = first.getNum() + " - Rule " + last.getNum();
            } else {
                range = "Rules " + first.getNum() + " - " + last.getNum();
            }
            txtSectionRange.setText(range);

            return itemView;
        }
    }
}
