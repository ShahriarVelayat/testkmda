package com.komodaa.app.activities;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.komodaa.app.BuildConfig;
import com.komodaa.app.R;
import com.komodaa.app.utils.Utils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class AboutActivity extends AppCompatActivity {

    @BindView(R.id.tvAppName) TextView tvAppName;
    @BindView(R.id.tvDesc) TextView tvDesc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        getSupportActionBar().setTitle(Utils.generateTypeFacedString(this, getTitle().toString()));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ButterKnife.bind(this);
        tvAppName.setText(String.format("%s - نسخه %s", getString(R.string.app_name), BuildConfig.VERSION_NAME));

        tvDesc.setText(Html.fromHtml(Utils.readText(this, "about.html")));

    }

    @Override public boolean onCreateOptionsMenu(Menu menu) {
        //getMenuInflater().inflate(R.menu.about, menu);
        return true;
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


    @OnClick({R.id.imgInstagram, R.id.imgTelegram, R.id.imgTwitter, R.id.btnContact})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.imgInstagram:
                Uri uri = Uri.parse("https://www.instagram.com/_u/komodaa/");
                Intent likeIng = new Intent(Intent.ACTION_VIEW, uri);

                likeIng.setPackage("com.instagram.android");

                try {
                    startActivity(likeIng);
                } catch (ActivityNotFoundException e) {
                    startActivity(new Intent(Intent.ACTION_VIEW,
                            Uri.parse("https://www.instagram.com/komodaa/")));
                }
                break;
            case R.id.imgTelegram:
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://telegram.me/komodaa")));
                break;
            case R.id.imgTwitter:
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://twitter.com/komodaa")));
                break;
            case R.id.btnContact:
                startActivity(new Intent(this, ContactActivity.class));
                break;
        }
    }
}
