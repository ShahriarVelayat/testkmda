package com.komodaa.app.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.chrisbanes.photoview.PhotoView;
import com.komodaa.app.R;
import com.komodaa.app.models.Media;
import com.squareup.picasso.Picasso;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;


/**
 * Created by nevercom on 11/26/16.
 */

public class ZoomableMediaFragment extends Fragment {
    @BindView(R.id.imageView) PhotoView imageView;
    private Unbinder unbinder;

    @Override public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @Nullable @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.slide_image, container, false);
        unbinder = ButterKnife.bind(this, v);
        Bundle args = getArguments();
        Media m = args.getParcelable("media");
        final Context c = getActivity();
        Picasso.with(getActivity()).load(m.getPath()).fit().centerInside().placeholder(R.drawable.loading_details_activity).into(imageView);

        return v;
    }
}
