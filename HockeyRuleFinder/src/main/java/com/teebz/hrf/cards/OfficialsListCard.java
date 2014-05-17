package com.teebz.hrf.cards;

import com.teebz.hrf.Helpers;
import com.teebz.hrf.R;
import com.teebz.hrf.entities.Official;
import com.teebz.hrf.listeners.OfficialsListItemClickListener;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import it.gmariotti.cardslib.library.internal.Card;

public class OfficialsListCard extends Card {
    private OfficialsListItemClickListener mClickListener;
    private Official mOfficial;

    public OfficialsListCard(Context context) {
        this(context, R.layout.officials_list_card_inner_layout);
    }

    public OfficialsListCard(Context context, int innerLayout) {
        super(context, innerLayout);
        setClickListeners();
    }

    public void setDetails(Official official, OfficialsListItemClickListener clickListener) {
        mOfficial = official;
        mClickListener = clickListener;
    }

    private void setClickListeners(){
        setOnClickListener(new OnCardClickListener() {
            @Override
            public void onClick(Card card, View view) {
                if (mClickListener != null) {
                    mClickListener.onOfficialsListItemClick(view, 0, mOfficial.number);
                }
            }
        });
    }

    @Override
    public void setupInnerViewElements(ViewGroup parent, View view) {
        TextView txtNumber = (TextView)view.findViewById(R.id.officialGridCardNumber);
        txtNumber.setText("#" + mOfficial.number);

        ImageView officialImg = (ImageView)view.findViewById(R.id.officialImageView);
        Drawable img = Helpers.getDrawableFromAssets(getContext(),
                Helpers.OFFICIAL_IMGS_FOLDER,
                mOfficial.number,
                ".jpg");

        officialImg.setImageDrawable(img);

        TextView txtName = (TextView)view.findViewById(R.id.officialGridCardName);
        txtName.setText(mOfficial.name);
    }


}