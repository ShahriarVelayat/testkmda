package com.komodaa.app.activities;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.komodaa.app.R;
import com.komodaa.app.utils.Utils;
import com.rey.material.widget.Button;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class CallbackActivity extends AppCompatActivity {

    @BindView(R.id.tvMessage) TextView tvMessage;
    @BindView(R.id.imgStatus) ImageView imgStatus;
    @BindView(R.id.komoLooloo) ImageView imgKomoLooloo;
    @BindView(R.id.tvStatus) TextView tvStatus;
    @BindView(R.id.btnBack) Button btnBack;
    private boolean hasError;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_callback);
        ButterKnife.bind(this);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(Utils.generateTypeFacedString(this, getTitle().toString()));
        final Uri data = getIntent().getData();
        if (data == null) {
            return;
        }
        Log.i("URI", data.toString());
        /* @formatter:off */
        final String tmpState = data.getQueryParameter("status");
        final String message = data.getQueryParameter("message");
        tvMessage.setText(message);

        hasError = !TextUtils.isEmpty(tmpState) && Integer.parseInt(tmpState) < 0;
        if (hasError) {
            tvStatus.setVisibility(View.GONE);
            imgKomoLooloo.setVisibility(View.VISIBLE);
            imgStatus.setImageResource(R.drawable.icon_close);
            btnBack.setBackgroundColor(Utils
                    .getColorFromResource(this, R.color.notification_error_dark));
            getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Utils
                    .getColorFromResource(this, R.color.notification_error)));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                Window window = getWindow();
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                window.setStatusBarColor(Utils
                        .getColorFromResource(this, R.color.notification_error_dark));
            }
        } else {
            imgKomoLooloo.setVisibility(View.GONE);
            tvStatus.setVisibility(View.VISIBLE);
            btnBack.setBackgroundColor(Utils.getColorFromResource(this, R.color.colorPrimary));
            imgStatus.setImageResource(R.drawable.icon_tick);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case android.R.id.home:
                // This is called when the Home (Up) button is pressed
                // in the Action Bar.
                finishMe();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override public void onBackPressed() {
        finishMe();
    }

    public void finishMe() {
        startActivity(new Intent(this, HomeActivity.class)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
        finish();
    }

    @OnClick({R.id.btnBack, R.id.btnHelp})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnBack:
                finishMe();
                break;
            case R.id.btnHelp:
                Intent intent = new Intent(this, HelpActivity.class);
                intent.putExtra("title", "اطلاعات بیشتر");
                intent.putExtra("url", hasError ? "https://komodaa.com/help/fail"
                                                : "https://komodaa.com/help/delivery");
                startActivity(intent);
                break;

        }
    }
}
