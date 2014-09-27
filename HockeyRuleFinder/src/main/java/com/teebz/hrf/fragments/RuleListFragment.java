package com.teebz.hrf.fragments;

import com.teebz.hrf.R;
import com.teebz.hrf.activities.QuickReferenceActivity;
import com.teebz.hrf.activities.RuleDetailActivity;
import com.teebz.hrf.entities.Rule;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class RuleListFragment extends android.app.Fragment {
    public List<Rule> mRules = null;

    public static RuleListFragment newInstance(List<Rule> rules) {
        RuleListFragment fragment = new RuleListFragment();
        fragment.setRules(rules);
        return fragment;
    }

    public RuleListFragment() {
        mRules = new ArrayList<Rule>();
    }

    public void setRules(List<Rule> rules) {
        this.mRules = rules;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.rule_list_fragment, container, false);

        populateListView(rootView);
        registerClickCallback(rootView);

        return rootView;
    }

    private void registerClickCallback(View theView) {
        ListView list = (ListView) theView.findViewById(R.id.ruleListView);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                Rule current = mRules.get(position);

                //TODO: Potentially not correct for other leagues.
                //If this is rule 29 (Signals) then we want to send the user to the quick reference page
                if (current.getNum().equals("29")) {
                    Intent newActivity = new Intent(getActivity(), QuickReferenceActivity.class);
                    startActivity(newActivity);
                }
                else {
                    Intent newActivity = new Intent(getActivity(), RuleDetailActivity.class);
                    newActivity.putExtra(RuleDetailFragment.RULES_DETAIL_KEY, current);
                    startActivity(newActivity);
                }
            }
        });
    }

    private void populateListView(View theView) {
        if (mRules != null) {
            ArrayAdapter<Rule> adapter = new RuleListAdapter();
            ListView list = (ListView) theView.findViewById(R.id.ruleListView);
            list.setAdapter(adapter);
        }
    }

    private class RuleListAdapter extends ArrayAdapter<Rule> {
        public RuleListAdapter() {
            super(getActivity(), R.layout.rule_row, mRules);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View itemView = convertView;
            if (itemView == null) {
                itemView = getActivity().getLayoutInflater().inflate(R.layout.rule_row, parent, false);
            }

            //Find the car to work with
            Rule current = mRules.get(position);

            TextView txtSectionName = (TextView)itemView.findViewById(R.id.txtRuleName);
            txtSectionName.setText(current.getName());

            TextView txtSectionId = (TextView)itemView.findViewById(R.id.txtRuleId);
            if (current.getNum().equals("PREFIX")) {
                txtSectionId.setText(current.getNum());
            } else {
                txtSectionId.setText("Rule " + current.getNum());
            }

            return itemView;
        }
    }
}