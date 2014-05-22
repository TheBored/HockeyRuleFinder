package com.teebz.hrf.cards;

import com.teebz.hrf.Helpers;
import com.teebz.hrf.R;
import com.teebz.hrf.entities.Official;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import it.gmariotti.cardslib.library.internal.Card;

public class SingleOfficialCard extends Card {

    private Context mContext;
    private Official mOfficial;

    public SingleOfficialCard(Context context) {
        this(context, R.layout.single_official_card_inner_layout);
    }

    public SingleOfficialCard(Context context, int innerLayout) {
        super(context, innerLayout);
        mContext = context;
    }

    public void setDetails(Official official) {
        mOfficial = official;
    }

    @Override
    public void setupInnerViewElements(ViewGroup parent, View view) {
        ImageView imgView = (ImageView)view.findViewById(R.id.officialImageView);
        Drawable img = Helpers.getDrawableFromAssets(mContext,
                Helpers.OFFICIAL_IMGS_FOLDER,
                mOfficial.number,
                ".jpg");

        imgView.setImageDrawable(img);

        TextView nameTextView = (TextView)view.findViewById(R.id.officialNameTxt);
        nameTextView.setText(mOfficial.name);

        TextView jerseyTextView = (TextView)view.findViewById(R.id.txtJersey);
        jerseyTextView.setText(mOfficial.number);

        TextView txtLeague = (TextView)view.findViewById(R.id.txtLeague);
        txtLeague.setText(sanitizeData(mOfficial.league));

        TextView txtMemberSince = (TextView)view.findViewById(R.id.txtMemberSince);
        txtMemberSince.setText(sanitizeData(mOfficial.memberSince));

        TextView txtRegSeasonCount = (TextView)view.findViewById(R.id.txtRegularSeasonGames);
        txtRegSeasonCount.setText(sanitizeData(mOfficial.regSeasonCount));

        TextView txtFirstRegSeason = (TextView)view.findViewById(R.id.txtFirstRegularSeason);
        txtFirstRegSeason.setText(sanitizeData(mOfficial.firstRegSeason));

        TextView txtFirstRegGame = (TextView)view.findViewById(R.id.txtFirstRegularSeasonGame);
        txtFirstRegGame.setText(sanitizeData(mOfficial.firstRegGame));

        TextView txtPlayoffGames = (TextView)view.findViewById(R.id.txtPlayoffGames);
        txtPlayoffGames.setText(sanitizeData(mOfficial.playoffCount));

        TextView txtFirstPlayoffSeason = (TextView)view.findViewById(R.id.txtFirstPlayoffSeason);
        txtFirstPlayoffSeason.setText(sanitizeData(mOfficial.firstPlayoffSeason));

        TextView txtFirstPlayoffGame = (TextView)view.findViewById(R.id.txtFirstPlayoffGame);
        txtFirstPlayoffGame.setText(sanitizeData(mOfficial.firstPlayoffGame));
    }

    private String sanitizeData(String input) {
        if (input == null || input.isEmpty()) {
            return "No Data";
        } else if (input.trim().equals("@  -")) {
            return "No Data";
        } else {
            return input;
        }
    }
}