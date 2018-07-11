package com.komodaa.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.github.chrisbanes.photoview.PhotoView;
import com.komodaa.app.R;
import com.komodaa.app.models.Media;
import com.squareup.picasso.Picasso;

import butterknife.BindView;
import butterknife.ButterKnife;

public class PhotoViewerActivity extends AppCompatActivity {


    @BindView(R.id.photo) PhotoView photo;
    @BindView(R.id.toolbar) Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_viewer);
        ButterKnife.bind(this);

        Intent i = getIntent();
        if (i == null || !i.hasExtra("media")) {
            finish();
        }

        Media m = i.getParcelableExtra("media");
        if (m == null) {
            finish();
        }
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(" ");
        ViewCompat.setTransitionName(photo, "image");
        Picasso.with(this).load(m.getPath()).into(photo);
    }

    @Override public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
//        if (id == R.id.action_open_source) {
//            new LibsBuilder()
//                    //provide a style (optional) (LIGHT, DARK, LIGHT_DARK_TOOLBAR)
//                    .withActivityStyle(Libs.ActivityStyle.LIGHT_DARK_TOOLBAR)
//                    //start the activity
//                    .start(this);
//            return true;
//
//        } else
        if (id == android.R.id.home) {
            finishWithTransition();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override public void onBackPressed() {
        finishWithTransition();
    }

    public void finishWithTransition() {ActivityCompat.finishAfterTransition(this);}

}
