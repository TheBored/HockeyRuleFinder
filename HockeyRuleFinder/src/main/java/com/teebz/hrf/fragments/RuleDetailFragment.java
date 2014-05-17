package com.teebz.hrf.fragments;

import com.teebz.hrf.Helpers;
import com.teebz.hrf.R;
import com.teebz.hrf.activities.SingleImageActivity;
import com.teebz.hrf.cards.RuleDetailCard;
import com.teebz.hrf.entities.Rule;
import com.teebz.hrf.searchparsers.RuleSearcher;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

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

    public void setData(Rule rule, String highlightText, String ruleTarget) {
        this.mRule = rule;
        this.mHighlightText = highlightText;
        this.mRuleTarget = ruleTarget;
    }

    private void populateListView() {
        buildCards();
        CardArrayAdapter mCardArrayAdapter = new CardArrayAdapter(getActivity(), mCardList);

        if (mDetailList!=null){
            mDetailList.setAdapter(mCardArrayAdapter);
        }
    }

    private void buildCards() {
        Activity activity = getActivity();
        if (activity != null) {
            Context context = activity.getBaseContext();
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
        populateListView();

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

    private class RuleDetailAdapter extends ArrayAdapter<Rule> {
        public RuleDetailAdapter() {
            super(getActivity(), R.layout.rule_detail_row, mRule.subRules);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View itemView = convertView;
            if (itemView == null) {
                itemView = getActivity().getLayoutInflater().inflate(R.layout.rule_detail_row, parent, false);
            }

            Rule current = mRule.subRules.get(position);


            String txtContents = "";
            for (String paragraph : current.htmlContents) {
                if (!txtContents.isEmpty()) {
                    txtContents += "<br /><br />";
                }

                String p = paragraph;
                if (mHighlightText != null && !mHighlightText.isEmpty()){
                    p = RuleSearcher.getHighlightedText(paragraph, mHighlightText, false);
                }

                txtContents += p;
            }
            txtContents += "<br />"; //After we put in all the text, add one more newline for spacing.

            //With the links prepared, set the ID and Name fields
            TextView txtRuleId = (TextView)itemView.findViewById(R.id.ruleNumberView);
            txtRuleId.setText(current.id);

            TextView txtRuleName = (TextView)itemView.findViewById(R.id.ruleTitleView);
            txtRuleName.setText(current.name);

            //Last, show the rule text. We need to parse out images and show them separately, so
            //run the parse and inflate views as needed.
            LinearLayout layout = (LinearLayout)itemView.findViewById(R.id.ruleDetailLinearLayout);
            //The layouts can be reused, so ensure that we aren't getting any stale rule text.
            layout.removeAllViewsInLayout();

            LayoutInflater inflater = getActivity().getLayoutInflater();

            if (!txtContents.contains("[image]")) {
                TextView tv = (TextView)inflater.inflate(R.layout.rule_detail_text, parent, false);
                //Convert it to display the various HTML tags.
                tv.setText(Html.fromHtml(txtContents));
                tv.setMovementMethod(LinkMovementMethod.getInstance());
                layout.addView(tv);
            } else {
                while (!txtContents.isEmpty()) {
                    int imageIndex = txtContents.indexOf("[image]");
                    int endIndex = txtContents.indexOf("[/image]");

                    TextView tv = (TextView)inflater.inflate(R.layout.rule_detail_text, parent, false);
                    //Convert it to display the various HTML tags.
                    String justText = imageIndex == -1 ? txtContents : txtContents.substring(0, imageIndex);
                    tv.setText(Html.fromHtml(justText + "<br />"));
                    tv.setMovementMethod(LinkMovementMethod.getInstance());
                    layout.addView(tv);

                    if (imageIndex != -1) {
                        ImageView iv = (ImageView)inflater.inflate(R.layout.rule_detail_image, parent, false);
                        String imgTag = txtContents.substring(imageIndex + 7,endIndex);
                        Drawable img = Helpers.getDrawableFromAssets(itemView.getContext(),
                                Helpers.DIAGRAM_IMGS_FOLDER,
                                imgTag,
                                ".png");

                        iv.setImageDrawable(img);

                        //Adjust the image to  take up a set amount of the screen
                        int srcWidth = img.getIntrinsicWidth();
                        int srcHeight = img.getIntrinsicHeight();

                        Resources r = getResources();
                        float dstWidth = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 300, r.getDisplayMetrics());
                        double ratio = dstWidth / srcWidth;

                        int pxEndHeight = (int)(srcHeight * ratio);
                        iv.setLayoutParams(new FrameLayout.LayoutParams((int)dstWidth, pxEndHeight));
                        iv.setTag(imgTag);
                        iv.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                showLargeImage(view.getTag().toString());
                            }
                        });
                        layout.addView(iv);
                    }

                    if (imageIndex == -1) {
                        //We are done, empty out txtContents
                        txtContents = "";
                    } else {
                        txtContents = txtContents.substring(endIndex + 8);
                    }
                }
            }
            return itemView;
        }

        private void showLargeImage(String imgName) {
            //If this is copied again, move it to a shared location w/the other usage.
            Intent newActivity = new Intent(getActivity(), SingleImageActivity.class);
            newActivity.putExtra(SingleImageActivity.SINGLE_IMAGE_KEY, imgName);
            newActivity.putExtra(SingleImageActivity.SINGLE_IMAGE_TITLE, "Diagram Detail View");
            newActivity.putExtra(SingleImageActivity.SINGLE_IMAGE_FOLDER, Helpers.DIAGRAM_IMGS_FOLDER);

            startActivity(newActivity);
        }
    }
}
