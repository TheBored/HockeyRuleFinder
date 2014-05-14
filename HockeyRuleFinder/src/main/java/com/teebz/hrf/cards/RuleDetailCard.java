package com.teebz.hrf.cards;

import com.teebz.hrf.R;
import com.teebz.hrf.entities.Rule;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import it.gmariotti.cardslib.library.internal.Card;

public class RuleDetailCard extends Card {

    private Rule mRule;

    public RuleDetailCard(Context context, Rule rule) {
        this(context, R.layout.rule_detail_card_inner_layout, rule);
    }

    public RuleDetailCard(Context context, int innerLayout, Rule rule) {
        super(context, innerLayout);
        mRule = rule;
        init();
    }

    private void init(){

        //No Header

        //Set a OnClickListener listener
        setOnClickListener(new OnCardClickListener() {
            @Override
            public void onClick(Card card, View view) {
                Toast.makeText(getContext(), "Click Listener card=", Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void setupInnerViewElements(ViewGroup parent, View view) {


        TextView mTitle = (TextView) parent.findViewById(R.id.ruleNumberView);
        mTitle.setText(mRule.id);

        //mSecondaryTitle = (TextView) parent.findViewById(R.id.carddemo_myapps_main_inner_secondaryTitle);
        //mRatingBar = (RatingBar) parent.findViewById(R.id.carddemo_myapps_main_inner_ratingBar);




    }
}