package com.komodaa.app.activities;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.FrameLayout;

import com.komodaa.app.R;
import com.komodaa.app.fragments.FollowingsFragment;
import com.komodaa.app.utils.Utils;

import butterknife.BindView;
import butterknife.ButterKnife;

public class FollowingsActivity extends AppCompatActivity {

    @BindView(R.id.frame_container) FrameLayout frameContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_followings);
        ButterKnife.bind(this);

        getSupportActionBar().setTitle(Utils.generateTypeFacedString(this, getTitle().toString()));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.frame_container, new FollowingsFragment());
        //transaction.addToBackStack(null);
        transaction.commit();
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
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
