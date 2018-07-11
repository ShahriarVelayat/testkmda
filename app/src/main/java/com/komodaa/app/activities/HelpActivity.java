package com.komodaa.app.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.RelativeLayout;

import com.komodaa.app.R;
import com.komodaa.app.utils.Utils;
import com.rey.material.widget.ProgressView;

import butterknife.BindView;
import butterknife.ButterKnife;

public class HelpActivity extends AppCompatActivity {


    @BindView(R.id.webView) WebView webView;
    @BindView(R.id.progress) ProgressView progress;
    @BindView(R.id.rlProgressBg) RelativeLayout rlProgressBg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);
        Intent intent = getIntent();
        if (intent == null || !intent.hasExtra("title") || !intent.hasExtra("url")) {
            finish();
        }
        String title = intent.getStringExtra("title");
        String url = intent.getStringExtra("url");

        getSupportActionBar().setTitle(Utils.generateTypeFacedString(this, TextUtils.isEmpty(title) ? "راهنما" : title));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ButterKnife.bind(this);

        webView.setWebViewClient(new WebViewClient() {
            @Override public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                rlProgressBg.setVisibility(View.VISIBLE);
                progress.setVisibility(View.VISIBLE);
            }

            @Override public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                rlProgressBg.setVisibility(View.GONE);
                progress.setVisibility(View.GONE);
            }

            @Override public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }
        });
        webView.getSettings().setJavaScriptEnabled(true);
        webView.loadUrl(url);

    }


    @Override public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
