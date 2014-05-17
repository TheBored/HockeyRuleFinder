package com.teebz.hrf.fragments;

import com.teebz.hrf.R;
import com.teebz.hrf.cards.OfficialsListCard;
import com.teebz.hrf.entities.Official;
import com.teebz.hrf.listeners.OfficialsListItemClickListener;
import com.teebz.hrf.searchparsers.OfficialsSearcher;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import java.util.ArrayList;

import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.CardGridArrayAdapter;
import it.gmariotti.cardslib.library.view.CardGridView;

public class OfficialsListFragment extends Fragment {
    private ArrayList<Official> mOfficials = null;
    private ArrayList<Card> mCardList = null;

    private OfficialsListItemClickListener mItemClickListener;

    public static OfficialsListFragment newInstance() {
        return new OfficialsListFragment();
    }

    public OfficialsListFragment() { }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // retain this fragment
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.officials_list_fragment, container, false);

        OfficialsSearcher searcher = OfficialsSearcher.getSearcher(rootView.getContext().getAssets());
        mOfficials = searcher.getAllOfficials();

        populateListView(rootView);

        //Hide the keyboard if it happens to be up
        InputMethodManager keyboard = (InputMethodManager) container.getContext()
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        keyboard.hideSoftInputFromWindow(container.getWindowToken(), 0);

        return rootView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mItemClickListener = (OfficialsListItemClickListener)activity;
        }
        catch (Exception e) {
            Toast.makeText(activity.getBaseContext(), "Click listener failed", Toast.LENGTH_LONG).show();
        }
    }

    private void populateListView(View theView) {
        buildCards(theView.getContext());
        CardGridArrayAdapter mCardArrayAdapter = new CardGridArrayAdapter(theView.getContext(), mCardList);

        CardGridView gridView = (CardGridView) theView.findViewById(R.id.officialsGrid);
        if (gridView!=null){
            gridView.setAdapter(mCardArrayAdapter);
        }
    }

    private void buildCards(Context context) {
        if (context != null) {
            mCardList = new ArrayList<Card>();

            for (Official o : mOfficials) {
                OfficialsListCard card = new OfficialsListCard(context);
                card.setDetails(o, mItemClickListener);
                mCardList.add(card);
            }
        }
    }
}
