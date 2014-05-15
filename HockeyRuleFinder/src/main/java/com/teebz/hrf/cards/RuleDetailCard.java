package com.teebz.hrf.cards;

import com.teebz.hrf.Helpers;
import com.teebz.hrf.R;
import com.teebz.hrf.activities.SingleImageActivity;
import com.teebz.hrf.entities.Rule;
import com.teebz.hrf.searchparsers.RuleSearcher;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import it.gmariotti.cardslib.library.internal.Card;

public class RuleDetailCard extends Card {

    private Rule mRule;
    private String mHighlightText;

    public RuleDetailCard(Context context) {
        this(context, R.layout.rule_detail_card_inner_layout);
    }

    public RuleDetailCard(Context context, int innerLayout) {
        super(context, innerLayout);
        init();
    }

    public void setDetails(Rule rule, String highlightText) {
        mRule = rule;
        mHighlightText = highlightText;
    }

    private void init(){
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
        String txtContents = "";
        for (String paragraph : mRule.htmlContents) {
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
        //TextView txtRuleId = (TextView)view.findViewById(R.id.ruleNumberView);
        //txtRuleId.setText(mRule.id);

        TextView txtRuleName = (TextView)view.findViewById(R.id.ruleTitleView);
        txtRuleName.setText(mRule.id + " - " + mRule.name);

        //Last, show the rule text. We need to parse out images and show them separately, so
        //run the parse and inflate views as needed.
        LinearLayout layout = (LinearLayout)view.findViewById(R.id.ruleDetailLinearLayout);
        //The layouts can be reused, so ensure that we aren't getting any stale rule text.
        layout.removeAllViewsInLayout();

        LayoutInflater inflater = LayoutInflater.from(getContext());

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
                    Drawable img = Helpers.getDrawableFromAssets(view.getContext(),
                            Helpers.DIAGRAM_IMGS_FOLDER,
                            imgTag,
                            ".png");

                    iv.setImageDrawable(img);

                    //Adjust the image to  take up a set amount of the screen
                    int srcWidth = img.getIntrinsicWidth();
                    int srcHeight = img.getIntrinsicHeight();

                    Resources r = view.getResources();
                    float dstWidth = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 300,
                            r.getDisplayMetrics());
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

    }

    private void showLargeImage(String imgName) {
        Activity activity = (Activity)getContext();

        //If this is copied again, move it to a shared location w/the other usage.
        Intent newActivity = new Intent(activity, SingleImageActivity.class);
        newActivity.putExtra(SingleImageActivity.SINGLE_IMAGE_KEY, imgName);
        newActivity.putExtra(SingleImageActivity.SINGLE_IMAGE_TITLE, "Diagram Detail View");
        newActivity.putExtra(SingleImageActivity.SINGLE_IMAGE_FOLDER, Helpers.DIAGRAM_IMGS_FOLDER);

        activity.startActivity(newActivity);
    }
}