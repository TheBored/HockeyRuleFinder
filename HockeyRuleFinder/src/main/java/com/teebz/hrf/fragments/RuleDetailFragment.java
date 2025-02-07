package com.teebz.hrf.fragments;

import com.teebz.hrf.R;
import com.teebz.hrf.cards.RuleDetailCard;
import com.teebz.hrf.entities.Rule;
import com.teebz.hrf.searchparsers.RuleDataServices;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.CardArrayAdapter;
import it.gmariotti.cardslib.library.view.CardListView;

public class RuleDetailFragment extends android.app.Fragment {
    public static final String RULES_DETAIL_KEY = "RULES_DETAIL_KEY";
    public static final String RULES_DETAIL_SEARCH_TERM = "RULES_DETAIL_SEARCH_TERM";

    private Rule mRule;
    private CardListView mDetailList;
    private ArrayList<Card> mCardList;
    private String mHighlightText;
    private int mRuleTarget;

    public static RuleDetailFragment newInstance(Rule rule, String highlightText, int ruleTarget) {
        RuleDetailFragment fragment = new RuleDetailFragment();
        fragment.setData(rule, highlightText, ruleTarget);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //If we are coming back with a valid instance state, take that.
        if (savedInstanceState != null) {
            mHighlightText = savedInstanceState.getString("HighlightText");
            mRuleTarget = savedInstanceState.getInt("RuleTarget");
            int ruleId = savedInstanceState.getInt("RuleId");

            RuleDataServices ruleDataServices = RuleDataServices.getRuleDataServices(getActivity().getBaseContext());
            mRule = ruleDataServices.getRule(null, ruleId, null);
        }
        // retain this fragment
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.rule_detail_fragment, container, false);

        mDetailList = (CardListView)rootView.findViewById(R.id.ruleDetailCardList);
        populateListView(rootView);

        //If we have a rule target, we want the list to auto scroll to that rule specifically.
        if (mRuleTarget != -1) {
            for (int i = 0; i < mRule.getSubRules().size(); i++) {
                if (mRule.getSubRules().get(i).getRID().equals(mRuleTarget)) {
                    mDetailList.setSelection(i);
                }
            }
        }

        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putString("HighlightText", mHighlightText);
        savedInstanceState.putInt("RuleTarget", mRuleTarget);
        savedInstanceState.putInt("RuleId", mRule.getRID());
    }

    public void setData(Rule rule, String highlightText, int ruleTarget) {
        this.mRule = rule;
        this.mHighlightText = highlightText;
        this.mRuleTarget = ruleTarget;
    }

    private void populateListView(View theView) {
        buildCards(theView.getContext());
        CardArrayAdapter mCardArrayAdapter = new CardArrayAdapter(theView.getContext(), mCardList);

        if (mDetailList!=null){
            mDetailList.setAdapter(mCardArrayAdapter);
        }
    }

    private void buildCards(Context context) {
        if (context != null) {
            mCardList = new ArrayList<Card>();

            for(Rule r : mRule.getSubRules()) {
                RuleDetailCard card = new RuleDetailCard(context);
                card.setDetails(r, mHighlightText);
                mCardList.add(card);
            }
        }
    }
}
