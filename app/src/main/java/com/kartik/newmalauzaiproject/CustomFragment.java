package com.kartik.newmalauzaiproject;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

/**
 * Created by Kartik on 2/22/2017.
 */
public class CustomFragment extends Fragment {
    public static final String fragmentText = "text";
    public static final String fragmentImage = "image";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        String text = getArguments().getString(fragmentText);
        String image = getArguments().getString(fragmentImage);
        View view = inflater.inflate(R.layout.fragment, container, false);
        TextView textView = (TextView) view.findViewById(R.id.fragment_textView);
        ImageView imageView = (ImageView) view.findViewById(R.id.fragment_imageView);
        textView.setText(text);

        //used Picasso library to load images into the imageView
        Picasso.with(this.getContext()).load(image).into(imageView);
        return view;
    }
    public static final CustomFragment newInstance(String text, String image){
        CustomFragment myFragment = new CustomFragment();
        Bundle bundle = new Bundle();
        bundle.putString(fragmentText, text);
        bundle.putString(fragmentImage, image);
        myFragment.setArguments(bundle);
        return myFragment;
    }

}
