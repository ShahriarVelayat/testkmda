package com.komodaa.app.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ImageView;

import com.komodaa.app.App;
import com.komodaa.app.R;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class PopupActivity extends AppCompatActivity {

    @BindView(R.id.photo) ImageView photo;
    private String action;
    private String title;
    private boolean persistent;
    private SharedPreferences prefs;
    private int id = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_popup);
        ButterKnife.bind(this);

        Intent intent = getIntent();
        if (intent == null || !intent.hasExtra("json")) {
            finish();
            return;
        }
        prefs = App.getPrefs();
        action = null;
        try {
            JSONObject j = new JSONObject(intent.getStringExtra("json"));
            if (!j.has("url") || !j.has("id")) {
                finish();
                return;
            }
            id = j.getInt("id");
            if (id <= prefs.getLong(App.MESSAGES_LAST_SEEN, 0)) {
                finish();
                return;
            }
            String validity = j.has("expires") ? j.getString("expires") : null;
            if (!TextUtils.isEmpty(validity)) {
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
                Date date = format.parse(validity);
                Log.i("date", date.toString());
                if (date.before(new Date())) {
                    Log.i("notif", "notif expired");
                    finish();
                    return;
                }
            }
            String url = j.getString("url");
            action = j.has("action") ? j.getString("action") : null;
            title = j.has("title") ? j.getString("title") : null;
            persistent = j.has("persistent") && j.getBoolean("persistent");
            if (!persistent) {
                prefs.edit().putLong(App.MESSAGES_LAST_SEEN, id).apply();
            }
            Picasso.with(this).load(url).into(photo);
        } catch (JSONException | ParseException e) {
            e.printStackTrace();

            finish();
        }

    }

    @OnClick(R.id.photo) public void onClick() {

        prefs.edit().putLong(App.MESSAGES_LAST_SEEN, id).apply();
        if (action == null) {
            return;
        }
        startActivity(new Intent(this, HelpActivity.class).putExtra("url", action).putExtra("title", title == null ? "کمدا" : title));
        finish();
    }
}
