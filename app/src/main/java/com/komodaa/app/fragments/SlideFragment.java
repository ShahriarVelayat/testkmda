package com.komodaa.app.fragments;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v4.util.Pair;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.komodaa.app.R;
import com.komodaa.app.activities.PhotoViewerActivity;
import com.komodaa.app.models.Media;
import com.komodaa.app.utils.Blur;
import com.komodaa.app.utils.Utils;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Created by nevercom on 10/11/16.
 */

public class SlideFragment extends Fragment {

    @BindView(R.id.imageView) ImageView imageView;
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
        final Media m = args.getParcelable("media");
        v.setOnClickListener(v1 -> {
            Intent intent = new Intent(getActivity(), PhotoViewerActivity.class).putExtra("media", m);
            Pair<View, String> pair = Pair.create(imageView, "image");
            ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(getActivity(),
                    pair);
            ActivityCompat.startActivity(getActivity(), intent, options.toBundle());
        });
        final Context c = getActivity();
        Picasso.with(getActivity()).load(m.getPath()).fit().centerInside().placeholder(R.drawable.loading_details_activity).into(imageView, new Callback() {
            @TargetApi(Build.VERSION_CODES.JELLY_BEAN) @Override public void onSuccess() {
                if (imageView == null) {
                    return;
                }
                Bitmap resource = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
                int intrinsicWidth = resource.getWidth();
                int width = imageView.getWidth();
                int intrinsicHeight = resource.getHeight();
                int height = imageView.getHeight();
                if ((intrinsicWidth < width) || intrinsicHeight < height) {
                    Log.d("Image Loaded", "onResourceReady: r.w = " + intrinsicWidth + ", i.w = " + width);
                    imageView.setBackground(new BitmapDrawable(getResources(), Blur.fastBlur(c, Utils.scaleImage(c, resource, 50), 25)));
                }
            }

            @Override public void onError() {

            }
        });

        return v;
    }
}
