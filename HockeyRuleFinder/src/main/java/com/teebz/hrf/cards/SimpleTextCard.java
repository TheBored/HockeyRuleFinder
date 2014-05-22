package com.teebz.hrf.cards;

import com.teebz.hrf.R;

import android.content.Context;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import it.gmariotti.cardslib.library.internal.Card;

public class SimpleTextCard extends Card {

    private Context mContext;
    private String mTitle;
    private String mText;

    public SimpleTextCard(Context context) {
        this(context, R.layout.simple_hrf_card_inner_layout);
    }

    public SimpleTextCard(Context context, int innerLayout) {
        super(context, innerLayout);
        mContext = context;
    }

    public void setDetails(String title, String text) {
        mTitle = title;
        mText = text;
    }

    @Override
    public void setupInnerViewElements(ViewGroup parent, View view) {
        TextView txtRuleName = (TextView)view.findViewById(R.id.ruleTitleView);
        txtRuleName.setText(mTitle);

        //Last, show the rule text. We need to parse out images and show them separately, so
        //run the parse and inflate views as needed.
        LinearLayout layout = (LinearLayout)view.findViewById(R.id.ruleDetailLinearLayout);
        //The layouts can be reused, so ensure that we aren't getting any stale rule text.
        layout.removeAllViewsInLayout();

        LayoutInflater inflater = LayoutInflater.from(getContext());


        TextView tv = (TextView)inflater.inflate(R.layout.rule_detail_text, parent, false);
        //Convert it to display the various HTML tags.
        tv.setText(Html.fromHtml(mText));
        tv.setMovementMethod(LinkMovementMethod.getInstance());
        layout.addView(tv);
    }


}