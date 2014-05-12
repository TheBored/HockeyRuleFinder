package com.teebz.hrf.fragments;

import com.teebz.hrf.Helpers;
import com.teebz.hrf.R;
import com.teebz.hrf.TouchImageView;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class SingleImageFragment extends android.app.Fragment {
    private String mImageName;
    private String mFolder;

    public static SingleImageFragment newInstance(String imgName, String folder) {
        SingleImageFragment fragment = new SingleImageFragment();
        fragment.setData(imgName, folder);
        return fragment;
    }

    private SingleImageFragment() {}

    public void setData(String imgName, String folder) {
        this.mImageName = imgName;
        this.mFolder = folder;
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
        View fragmentView = inflater.inflate(R.layout.single_image_fragment, container, false);

        TouchImageView imgView = (TouchImageView)fragmentView;
        Drawable img = Helpers.getDrawableFromAssets(container.getContext(),
                                                     mFolder,
                                                     mImageName,
                                                     ".png");
        imgView.setImageDrawable(img);
        imgView.setBackgroundColor(Color.WHITE);

        return fragmentView;
    }
}
