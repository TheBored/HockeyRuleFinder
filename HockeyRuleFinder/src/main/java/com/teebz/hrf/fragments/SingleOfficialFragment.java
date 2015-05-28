package com.teebz.hrf.fragments;

import com.teebz.hrf.R;
import com.teebz.hrf.cards.SimpleTextCard;
import com.teebz.hrf.cards.SingleOfficialCard;
import com.teebz.hrf.entities.Official;
import com.teebz.hrf.searchparsers.OfficialsSearcher;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import it.gmariotti.cardslib.library.view.CardView;

public class SingleOfficialFragment extends android.app.Fragment {
    private String mJerseyNumber;
    private static String OFFICIALS_JERSEY_KEY;

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

        //Coming from saved state.
        if (mJerseyNumber == null && savedInstanceState != null) {
            //Something happened that killed our state. Reload!
            mJerseyNumber = savedInstanceState.getString(SingleOfficialFragment.OFFICIALS_JERSEY_KEY);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(SingleOfficialFragment.OFFICIALS_JERSEY_KEY, mJerseyNumber);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //Inflate the fragment view
        View fragmentView = inflater.inflate(R.layout.single_official_fragment, container, false);

        OfficialsSearcher searcher = OfficialsSearcher.getSearcher(getActivity().getAssets());
        Official official = searcher.getOfficialByJerseyNumber(mJerseyNumber);

        CardView officialPlaceholder = (CardView)fragmentView.findViewById(R.id.officialCard);

        //If we have official number 35 or 85, show the unique text and stop processing.
        if (official.number.equals("35") || official.number.equals("85")) {
            SimpleTextCard simpleTextCard = new SimpleTextCard(getActivity());
            simpleTextCard.setDetails("Reserved Jersey", getResources().getString(R.string.officialNum3585));
            officialPlaceholder.setCard(simpleTextCard);
        } else {
            SingleOfficialCard singleOfficialCard = new SingleOfficialCard(getActivity());
            singleOfficialCard.setDetails(official);
            officialPlaceholder.setCard(singleOfficialCard);
        }

        return fragmentView;
    }
}
