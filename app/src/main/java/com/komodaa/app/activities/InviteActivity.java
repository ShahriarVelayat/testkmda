package com.komodaa.app.activities;

import android.os.Bundle;
import android.support.v4.app.ShareCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;

import com.komodaa.app.R;
import com.komodaa.app.utils.UserUtils;
import com.komodaa.app.utils.Utils;
import com.rey.material.widget.Button;

import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class InviteActivity extends AppCompatActivity {

    @BindView(R.id.btnSubmit) Button btnSubmit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invite);
        ButterKnife.bind(this);


        getSupportActionBar().setTitle(Utils.generateTypeFacedString(this, getTitle().toString()));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @OnClick(R.id.btnSubmit) public void onViewClicked() {
        long userId = UserUtils.getUser().getId();

        String text = String
                .format(Locale.US, "کُمُدا یه کمدِ لباسِ بی انتهاست! منم چسبیدم همونجا. اگه دوس داری بدونی چی به چیه، پاشو بیا.\nhttps://komodaa.com/web/invite/?%s",
                        userId > 0 ? userId + "" : "");
        ShareCompat.IntentBuilder
                .from(this)
                .setType("text/plain")
                .setText(text)
                .setChooserTitle("برنامه رو انتخاب کن")
                .startChooser();
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
