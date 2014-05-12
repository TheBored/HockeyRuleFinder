package com.teebz.hrf.fragments;

import com.teebz.hrf.R;
import com.teebz.hrf.entities.Official;
import com.teebz.hrf.listeners.OfficialsListItemClickListener;
import com.teebz.hrf.searchparsers.OfficialsSearcher;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class OfficialsListFragment extends Fragment {
    private ArrayList<Official> mOfficials = null;
    private OfficialsListItemClickListener mItemClickListener;

    public static OfficialsListFragment newInstance() {
        return new OfficialsListFragment();
    }

    public OfficialsListFragment() { }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.officials_list_fragment, container, false);

        OfficialsSearcher searcher = OfficialsSearcher.getSearcher(rootView.getContext().getAssets());
        mOfficials = searcher.getAllOfficials();

        registerClickCallback(rootView);
        populateListView(rootView);

        //Hide the keyboard if it happens to be up
        InputMethodManager keyboard = (InputMethodManager) container.getContext()
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        keyboard.hideSoftInputFromWindow(container.getWindowToken(), 0);

        return rootView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mItemClickListener = (OfficialsListItemClickListener)activity;
        }
        catch (Exception e) {
            Toast.makeText(activity.getBaseContext(), "Click listener failed", Toast.LENGTH_LONG).show();
        }
    }

    private void registerClickCallback(View theView) {
        ListView list = (ListView) theView.findViewById(R.id.officialsListView);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                Official o = mOfficials.get(position);
                mItemClickListener.onOfficialsListItemClick(view, position, o.number);
            }
        });
    }

    private void populateListView(View theView) {
        if (mOfficials != null) {
            ArrayAdapter<Official> adapter = new OfficialsListAdapter();
            ListView list = (ListView) theView.findViewById(R.id.officialsListView);
            list.setAdapter(adapter);
        }
    }

    private class OfficialsListAdapter extends ArrayAdapter<Official> {
        public OfficialsListAdapter() {
            super(getActivity(), R.layout.officials_row, mOfficials);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View itemView = convertView;
            if (itemView == null) {
                itemView = getActivity().getLayoutInflater().inflate(R.layout.officials_row, parent, false);
            }

            Official current = mOfficials.get(position);

            //Set the desc text
            TextView jerseyNumText = (TextView)itemView.findViewById(R.id.officialsJerseyNumber);
            jerseyNumText.setText(current.number);

            //Set the header text
            TextView nameText = (TextView)itemView.findViewById(R.id.officialsName);
            nameText.setText(current.name);

            return itemView;
        }
    }

}
