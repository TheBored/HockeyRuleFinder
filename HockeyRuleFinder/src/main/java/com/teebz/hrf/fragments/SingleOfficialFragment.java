package com.teebz.hrf.fragments;

import com.teebz.hrf.Helpers;
import com.teebz.hrf.R;
import com.teebz.hrf.entities.Official;
import com.teebz.hrf.searchparsers.OfficialsSearcher;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class SingleOfficialFragment extends android.app.Fragment {
    private String mJerseyNumber;

    public static SingleOfficialFragment newInstance(String jerseyNumber) {
        SingleOfficialFragment fragment = new SingleOfficialFragment();
        fragment.setData(jerseyNumber);
        return fragment;
    }

    public SingleOfficialFragment() {}

    public void setData(String jerseyNumber) {
        this.mJerseyNumber = jerseyNumber;
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
        //Inflate the fragment view
        View fragmentView = inflater.inflate(R.layout.single_official_fragment, container, false);

        OfficialsSearcher searcher = OfficialsSearcher.getSearcher(getActivity().getAssets());
        Official official = searcher.getOfficialByJerseyNumber(mJerseyNumber);

        //If we have official number 35 or 85, show the unique text and stop processing.
        if (official.number.equals("35") || official.number.equals("85")) {
            RelativeLayout relativeLayout = (RelativeLayout)fragmentView.findViewById(R.id.officialContainer);
            relativeLayout.setVisibility(View.GONE);

            TextView officialPlaceholder = (TextView)fragmentView.findViewById(R.id.officialPlaceholderText);
            officialPlaceholder.setVisibility(View.VISIBLE);

            return fragmentView;
        }

        ImageView imgView = (ImageView)fragmentView.findViewById(R.id.officialImageView);
        Drawable img = Helpers.getDrawableFromAssets(container.getContext(),
                Helpers.OFFICIAL_IMGS_FOLDER,
                official.number,
                ".jpg");

        imgView.setImageDrawable(img);

        TextView nameTextView = (TextView)fragmentView.findViewById(R.id.officialNameTxt);
        nameTextView.setText(official.name);

        TextView jerseyTextView = (TextView)fragmentView.findViewById(R.id.txtJersey);
        jerseyTextView.setText(official.number);

        TextView txtLeague = (TextView)fragmentView.findViewById(R.id.txtLeague);
        txtLeague.setText(sanitizeData(official.league));

        TextView txtMemberSince = (TextView)fragmentView.findViewById(R.id.txtMemberSince);
        txtMemberSince.setText(sanitizeData(official.memberSince));

        TextView txtRegSeasonCount = (TextView)fragmentView.findViewById(R.id.txtRegularSeasonGames);
        txtRegSeasonCount.setText(sanitizeData(official.regSeasonCount));

        TextView txtFirstRegSeason = (TextView)fragmentView.findViewById(R.id.txtFirstRegularSeason);
        txtFirstRegSeason.setText(sanitizeData(official.firstRegSeason));

        TextView txtFirstRegGame = (TextView)fragmentView.findViewById(R.id.txtFirstRegularSeasonGame);
        txtFirstRegGame.setText(sanitizeData(official.firstRegGame));

        TextView txtPlayoffGames = (TextView)fragmentView.findViewById(R.id.txtPlayoffGames);
        txtPlayoffGames.setText(sanitizeData(official.playoffCount));

        TextView txtFirstPlayoffSeason = (TextView)fragmentView.findViewById(R.id.txtFirstPlayoffSeason);
        txtFirstPlayoffSeason.setText(sanitizeData(official.firstPlayoffSeason));

        TextView txtFirstPlayoffGame = (TextView)fragmentView.findViewById(R.id.txtFirstPlayoffGame);
        txtFirstPlayoffGame.setText(sanitizeData(official.firstPlayoffGame));

        return fragmentView;
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
