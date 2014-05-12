package com.teebz.hrf.fragments;

import com.teebz.hrf.Helpers;
import com.teebz.hrf.R;
import com.teebz.hrf.activities.SingleImageActivity;
import com.teebz.hrf.entities.Call;
import com.teebz.hrf.listeners.QuickReferenceListItemClickListener;
import com.teebz.hrf.searchparsers.CallSearcher;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class QuickReferenceFragment extends android.app.Fragment {
    private ArrayList<Call> mCalls = null;
    private CallSearcher mCallSearcher;
    private QuickReferenceListItemClickListener mItemClickListener;

    public static QuickReferenceFragment newInstance() {
        return new QuickReferenceFragment();
    }

    public QuickReferenceFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.quick_reference_fragment, container, false);

        mCallSearcher = CallSearcher.getSearcher(rootView.getContext().getAssets());
        mCalls = mCallSearcher.getCalls();

        registerClickCallback(rootView);
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
            mItemClickListener = (QuickReferenceListItemClickListener)activity;
        }
        catch (Exception e) {
            Toast.makeText(activity.getBaseContext(), "Click listener failed", Toast.LENGTH_LONG).show();
        }
    }

    private void populateListView(View theView) {
        if (mCalls != null) {
            ArrayAdapter<Call> adapter = new QuickRefListAdapter();
            ListView list = (ListView) theView.findViewById(R.id.quickRefListView);
            list.setAdapter(adapter);
        }
    }

    private void registerClickCallback(View theView) {
        ListView list = (ListView) theView.findViewById(R.id.quickRefListView);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                //A rule was clicked, get the corresponding rules to pass to the parent.
                Call c = mCalls.get(position);

                //Alert our parent that a click happened.
                mItemClickListener.onQuickRefListItemClick(view, position, c.assocRuleId);
            }
        });
    }

    private void showLargeImage(String smallName) {
        //If this is copied again, move it to a shared location w/the other usage.
        Call c = mCallSearcher.getCallByCallId(smallName);

        Intent newActivity = new Intent(getActivity(), SingleImageActivity.class);
        newActivity.putExtra(SingleImageActivity.SINGLE_IMAGE_KEY, c.imgName);
        newActivity.putExtra(SingleImageActivity.SINGLE_IMAGE_TITLE, c.name);
        newActivity.putExtra(SingleImageActivity.SINGLE_IMAGE_FOLDER, Helpers.CALL_IMGS_FOLDER);

        startActivity(newActivity);
    }

    private class QuickRefListAdapter extends ArrayAdapter<Call> {
        public QuickRefListAdapter() {
            super(getActivity(), R.layout.quick_reference_row, mCalls);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View itemView = convertView;
            if (itemView == null) {
                itemView = getActivity().getLayoutInflater().inflate(R.layout.quick_reference_row, parent, false);
            }

            Call current = mCalls.get(position);

            //Set the header text
            TextView nameText = (TextView)itemView.findViewById(R.id.qrNameTextView);
            nameText.setText(current.name);

            //Set the desc text
            TextView descText = (TextView)itemView.findViewById(R.id.qrDescTextView);
            descText.setText(current.desc);

            //Set the image
            String imageName = current.imgName.replace("img_", "img_sm_");
            ImageButton quickRefImageBtn = (ImageButton)itemView.findViewById(R.id.qrImageBtn);

            //If this call does not have an image, handle it differently.
            if (!imageName.equals("NO SIGNAL")) {
                Drawable d = Helpers.getDrawableFromAssets(getContext(),
                                                           Helpers.CALL_IMGS_FOLDER,
                                                           imageName,
                                                           ".png");
                quickRefImageBtn.setImageDrawable(d);
            }
            else {
                quickRefImageBtn.setImageResource(android.R.color.white);
            }

            quickRefImageBtn.setTag(current.id);
            quickRefImageBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showLargeImage(view.getTag().toString());
                }
            });

            //Last, determine how tall the row should be.
            int height = 0;

            ViewGroup.LayoutParams layoutParams = itemView.getLayoutParams();
            layoutParams.height = convertDpToPixels(height, itemView.getContext());
            itemView.setLayoutParams(layoutParams);

            return itemView;
        }
    }

    public static int convertDpToPixels(float dp, Context context){
        Resources resources = context.getResources();
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dp,
                resources.getDisplayMetrics()
        );
    }
}
