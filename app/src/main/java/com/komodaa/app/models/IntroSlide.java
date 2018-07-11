package com.komodaa.app.models;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by nevercom on 9/27/16.
 */

public class IntroSlide extends Fragment {

    private static final String ARG_LAYOUT_RES_ID = "layoutResId";
    private int layoutResId;

    public static IntroSlide newInstance(int layoutResId) {
        IntroSlide slide = new IntroSlide();

        Bundle args = new Bundle();
        args.putInt(ARG_LAYOUT_RES_ID, layoutResId);
        slide.setArguments(args);

        return slide;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null && getArguments().containsKey(ARG_LAYOUT_RES_ID)) {
            layoutResId = getArguments().getInt(ARG_LAYOUT_RES_ID);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(layoutResId, container, false);

        return view;
    }

//    @Override public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
//        super.onViewCreated(view, savedInstanceState);
//
//        //final Display display = getActivity().getWindowManager().getDefaultDisplay();
//        //int heightPixels = getActivity().getApplicationContext().getResources().getDisplayMetrics().heightPixels;
//        //Point p = new Point();
//        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
//
//        int bottom = Utils.getDisplayContentHeight(getActivity());
//        //float scHeight = heightPixels / 1920f;
//        //Log.d("jv", "height: " + heightPixels + ", y: " + bottom + " layout= " + layoutResId);
//        //Toast.makeText(getActivity(), "Height = " + bottom, Toast.LENGTH_SHORT).show();
//        switch (layoutResId) {
//            case R.layout.intro_slide_1: {
//                params.setMargins(0, (int) (bottom * 0.25), 0, 0);
//                params.addRule(RelativeLayout.CENTER_HORIZONTAL);
//                break;
//            }
//            case R.layout.intro_slide_2: {
//                params.setMargins(0, (int) (bottom * 0.20), 0, 0);
//                params.addRule(RelativeLayout.CENTER_HORIZONTAL);
//                break;
//            }
//            case R.layout.intro_slide_3: {
//                params.setMargins(0, (int) (bottom * 0.45), 0, 0);
//                params.addRule(RelativeLayout.CENTER_HORIZONTAL);
//                break;
//            }
////            case R.layout.intro_slide_4: {
////                params.setMargins(0, (int) (bottom * 0.92), 0, 0);
////                params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
////                break;
////            }
//        }
//
//        view.findViewById(R.id.imageView2).setLayoutParams(params);
//    }


}
