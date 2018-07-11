package com.komodaa.app.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.komodaa.app.R;
import com.komodaa.app.activities.IntroActivity;
import com.komodaa.app.activities.LoginActivity;
import com.komodaa.app.activities.SignupActivity;
import com.rey.material.widget.Button;

/**
 * Created by nevercom on 10/21/16.
 */

public class SlideSignupFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View   v   = inflater.inflate(R.layout.intro_slide_4, container, false);
        Button btn = v.findViewById(R.id.btnSubmit);
        btn.setOnClickListener(v1 -> {
            startActivity(new Intent(getActivity(), SignupActivity.class));
            ((IntroActivity) getActivity()).onDonePressed(SlideSignupFragment.this);
        });
        v.findViewById(R.id.btnLogin).setOnClickListener(b -> {
            startActivity(new Intent(getActivity(), LoginActivity.class));
            ((IntroActivity) getActivity()).onDonePressed(SlideSignupFragment.this);

        });
//        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
//
//        int bottom = Utils.getDisplayContentHeight(getActivity());
//        params.setMargins(0, (int) (bottom * 0.38), 0, 0);
//        params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
//        v.findViewById(R.id.imageView2).setLayoutParams(params);


        return v;
    }
}
