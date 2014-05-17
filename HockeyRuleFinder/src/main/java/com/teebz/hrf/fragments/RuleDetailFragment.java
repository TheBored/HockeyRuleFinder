package com.teebz.hrf.fragments;

import com.teebz.hrf.R;
import com.teebz.hrf.cards.RuleDetailCard;
import com.teebz.hrf.entities.Rule;

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
    private String mRuleTarget;

    public static RuleDetailFragment newInstance(Rule rule, String highlightText, String ruleTarget) {
        RuleDetailFragment fragment = new RuleDetailFragment();
        fragment.setData(rule, highlightText, ruleTarget);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
        if (mRuleTarget != null) {
            for (int i = 0; i < mRule.subRules.size(); i++) {
                if (mRule.subRules.get(i).id.equals(mRuleTarget)) {
                    mDetailList.setSelection(i);
                }
            }
        }

        return rootView;
    }

    public void setData(Rule rule, String highlightText, String ruleTarget) {
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

            for(Rule r : mRule.subRules) {
                RuleDetailCard card = new RuleDetailCard(context);
                card.setDetails(r, mHighlightText);
                mCardList.add(card);
            }
        }
    }
}
