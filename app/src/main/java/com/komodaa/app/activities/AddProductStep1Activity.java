package com.komodaa.app.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.util.SparseArrayCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.ContentViewEvent;
import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetView;
import com.komodaa.app.App;
import com.komodaa.app.BuildConfig;
import com.komodaa.app.R;
import com.komodaa.app.interfaces.NetworkCallback;
import com.komodaa.app.models.Error;
import com.komodaa.app.utils.ApiClient;
import com.komodaa.app.utils.ImageUtils;
import com.komodaa.app.utils.UserUtils;
import com.komodaa.app.utils.Utils;
import com.rey.material.widget.Button;
import com.rey.material.widget.RadioButton;
import com.soundcloud.android.crop.Crop;

import net.darkion.AchievementUnlockedLib.AchievementUnlocked;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.Locale;
import java.util.Random;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class AddProductStep1Activity extends AppCompatActivity {

    private static final int CAMERA_REQUEST = 1;
    private static final String TAG = "Komodaa";

    boolean isSquare;
    SparseArrayCompat<Uri> uris = new SparseArrayCompat<>();
    boolean isNew = false;
    private boolean isHomemade;

    @BindView(R.id.imgMainPhoto) ImageView imgMainPhoto;
    @BindView(R.id.imgPhoto2) ImageView imgPhoto2;
    @BindView(R.id.imgPhoto1) ImageView imgPhoto1;
    @BindView(R.id.llImages) LinearLayout llImages;
    @BindView(R.id.btnNext) Button btnNext;
    @BindView(R.id.rbNew) RadioButton rbNew;
    @BindView(R.id.rbUsed) RadioButton rbUsed;
    @BindView(R.id.rlRgNew) LinearLayout rlRgNew;
    @BindView(R.id.rlRgUsed) LinearLayout rlRgUsed;
    @BindView(R.id.rlLimitWrap) RelativeLayout rlLimitWrap;
    @BindView(R.id.tvHowToShoot) TextView tvHowToShoot;
    @BindView(R.id.webView) WebView webView;
    @BindView(R.id.rbHomemade) RadioButton rbHomemade;
    @BindView(R.id.rlRgHomemade) LinearLayout rlRgHomemade;

    ImageView targetView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_product_step1);
        ButterKnife.bind(this);
        getSupportActionBar().setTitle(Utils.generateTypeFacedString(this, getTitle().toString()));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (Utils.isActivityVisitedForFirstTime(this)) {
            rlLimitWrap.setVisibility(View.VISIBLE);
        } else {
            rlLimitWrap.setVisibility(View.GONE);

        }
        // In case 'config' is not retrieved yet, make another request. we need this data in the next step
        if (App.getPrefs().getString("config", null) == null) {
            JSONObject data = new JSONObject();
            try {
                data.put("app_version", BuildConfig.VERSION_CODE);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            ApiClient.makeRequest(this, "GET", "/all", data, new NetworkCallback() {
                @Override
                public void onSuccess(int status, JSONObject data) {
                    Log.d(TAG, "onSuccess: ");
                    App.getPrefs().edit().putString("config", data.toString()).apply();

                }

                @Override
                public void onFailure(int status, Error error) {
                    Log.d(TAG, "onFailure: ");
                }
            });
        }
        initializeWebView();
        try {
            String vUserName = UserUtils.getUser().getFirstName() + " " + UserUtils.getUser()
                                                                                   .getLastName();
            if (TextUtils.isEmpty(vUserName.trim())) {
                vUserName = "نا شناس";
            }
            Answers.getInstance().logContentView(new ContentViewEvent()
                    .putContentName("ثبت محصول")
                    .putContentType("Add Item")
                    .putCustomAttribute("user-id", UserUtils.getUser().getId() + "")
                    .putCustomAttribute("Viewer", vUserName)
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
        displayTapTargetIfNecessary();
    }

    private void displayTapTargetIfNecessary() {
        SharedPreferences prefs = App.getPrefs();

        if (prefs.getBoolean("is_first_time_homemade_shown", true)) {
            prefs.edit().putBoolean("is_first_time_homemade_shown", false).apply();
            final Activity c = this;
            rlRgHomemade.postDelayed(() -> TapTargetView.showFor(c, TapTarget
                    .forView(rlRgHomemade, Utils
                            .generateTypeFacedString(c, "کاردستی یعنی چیزمیزایی که دست ساز و جزو هنرهای خودته."))
                    .id(1)), 1000);
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void initializeWebView() {
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setScrollBarStyle(View.SCROLLBARS_OUTSIDE_OVERLAY);
        final WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webView.setVisibility(View.GONE);
        webView.getSettings().setAppCacheEnabled(false);

        webView.setWebViewClient(new WebViewClient() {


            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                view.setVisibility(View.GONE);
                super.onReceivedError(view, request, error);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                // do your stuff here
                try {
                    String title = view.getTitle();
                    if (title != null && title.equalsIgnoreCase("nodisplay")) {
                        view.setVisibility(View.GONE);
                    } else {
                        view.setVisibility(View.VISIBLE);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);
                return true;
            }

        });

        if (Utils.isNetworkAvailable(this)) {
            final Random rnd = new Random();
            final int irnd = rnd.nextInt();

            webView.loadUrl("http://komodaa.com/banner/?user_id="
                    + UserUtils.getUser().getId()
                    + "&app_ver=" + BuildConfig.VERSION_CODE
                    + "&android_ver=" + Utils.getAndroidVersion()
                    + "&rnd=" + irnd);
            //webView.setVisibility(View.VISIBLE);
        } else {
            webView.setVisibility(View.GONE);
        }
    }

    @OnClick({
            R.id.imgMainPhoto, R.id.imgPhoto2, R.id.imgPhoto1, R.id.btnNext, R.id.rlRgNew,
            R.id.rlRgUsed, R.id.rbNew, R.id.rbUsed, R.id.mbtn_close, R.id.tvHowToShoot,
            R.id.rbHomemade, R.id.rlRgHomemade
    })
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.imgMainPhoto:
                targetView = imgMainPhoto;
                isSquare = true;
                displayPicker();
                break;
            case R.id.imgPhoto2:
                targetView = imgPhoto2;
                isSquare = false;
                displayPicker();

                break;
            case R.id.imgPhoto1:
                targetView = imgPhoto1;
                isSquare = false;
                displayPicker();

                break;
            case R.id.rlRgNew:
            case R.id.rbNew:
                rbNew.setChecked(true);
                rbUsed.setChecked(false);
                rbHomemade.setChecked(false);
                isNew = true;
                isHomemade = false;
                break;
            case R.id.rlRgUsed:
            case R.id.rbUsed:
                rbNew.setChecked(false);
                rbHomemade.setChecked(false);
                rbUsed.setChecked(true);
                isNew = false;
                isHomemade = false;
                break;
            case R.id.rlRgHomemade:
            case R.id.rbHomemade:
                rbNew.setChecked(false);
                rbUsed.setChecked(false);
                rbHomemade.setChecked(true);
                isNew = false;
                isHomemade = true;
                break;
            case R.id.btnNext:
                if (uris == null || uris.size() < 1) {
                    new AchievementUnlocked(this)
                            .setTitle("خطا !")
                            .setIcon(Utils.getDrawableFromRes(this, R.drawable.no))
                            .setTitleColor(Color.WHITE)
                            .setSubtitleColor(Color.WHITE)
                            .setTypeface(ResourcesCompat.getFont(this, R.font.iran_sans))
                            .setDuration(1500)
                            .setSubTitle("لطفاً برای آیتم تصویری انتخاب کنید")
                            .setBackgroundColor(Utils
                                    .getColorFromResource(this, R.color.notification_error))
                            .isLarge(false)
                            .build().show();
                    return;
                }
                if (!rbNew.isChecked() && !rbUsed.isChecked() && !rbHomemade.isChecked()) {
                    new AchievementUnlocked(this)
                            .setTitle("خطا !")
                            .setIcon(Utils.getDrawableFromRes(this, R.drawable.no))
                            .setTitleColor(Color.WHITE)
                            .setSubtitleColor(Color.WHITE)
                            .setTypeface(ResourcesCompat.getFont(this, R.font.iran_sans))
                            .setDuration(1500)
                            .setSubTitle("لطفاً وضعیت آیتم را انتخاب کنید")
                            .setBackgroundColor(Utils
                                    .getColorFromResource(this, R.color.notification_error))
                            .isLarge(false)
                            .build().show();
                    return;
                }
                Intent intent = new Intent(this, AddProductStep2Activity.class);

                intent.putExtra("count", uris.size());
                for (int i = 0; i < uris.size(); i++) {

                    intent.putExtra("uri" + i, uris.get(uris.keyAt(i)));
                }
                intent.putExtra("status", isNew);
                intent.putExtra("is_homemade", isHomemade);
                startActivity(intent);
                break;
            case R.id.mbtn_close:
                rlLimitWrap.setVisibility(View.GONE);
                break;

            case R.id.tvHowToShoot:
                //startActivity(new Intent(this, HowToShootActivity.class));
                startActivity(new Intent(this, HelpActivity.class)
                        .putExtra("url", "http://komodaa.com/help/how-to-shoot")
                        .putExtra("title", "چطور عکس کمدایی بگیریم"));

                break;
        }
    }

    void displayPicker() {
        final Activity a = this;
        DialogInterface.OnClickListener actionListener = (dialog, which) -> {
            switch (which) {
                case 0: // Delete
                    pickCamera();
                    break;
                case 1: // Copy

                    Crop.pickImage(a);
                    break;
                default:
                    break;
            }
        };
        AlertDialog.Builder builder = new AlertDialog.Builder(a);
        builder.setTitle("انتخاب کنید");

        String[] options = new String[]{"دوربین", "انتخاب از گالری"};
        builder.setItems(options, actionListener);
        builder.create().show();
    }

    void pickCamera() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri
                .fromFile(getTempFile(CAMERA_REQUEST, targetView)));
        startActivityForResult(cameraIntent, CAMERA_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent result) {
        //Log.d("", "onActivityResult() called with: requestCode = [" + requestCode + "], resultCode = [" + resultCode + "], result = [" + result + "]");
        if (resultCode != RESULT_OK) {
            return;
        }
        if (requestCode == Crop.REQUEST_PICK) {
            beginCrop(result.getData());
        } else if (requestCode == Crop.REQUEST_CROP) {
            handleCrop(resultCode, result);
        } else if (requestCode == CAMERA_REQUEST) {

            beginCrop(Uri.fromFile(getTempFile(requestCode, targetView)));

        }
    }

    private void beginCrop(Uri source) {
        //Log.d(TAG, "beginCrop() called with: source = [" + source + "]");
        Uri destination = Uri.fromFile(getTempFile(Crop.REQUEST_CROP, targetView));
        String sourcePath = Utils.getPath(this, source);
        Crop of = Crop.of(Uri.fromFile(new File(sourcePath)), destination);
        if (isSquare) {
            of.asSquare();
        }
        of.start(this);
    }

    private void handleCrop(int resultCode, Intent result) {
        Log.d(TAG, "handleCrop() called with: resultCode = [" + resultCode + "], result = [" + result + "]");
        if (resultCode == RESULT_OK) {
            Uri uri = Crop.getOutput(result);
            ImageUtils.normalizeImageForUri(this, uri);
            Log.d(TAG, "handleCrop: " + uri.getPath());
            uris.put(targetView.getId(), uri);
            targetView.setScaleType(ImageView.ScaleType.FIT_CENTER);
            targetView.setImageBitmap(Utils
                    .decodeSampledBitmapFromUri(this, uri, targetView.getWidth(), targetView
                            .getHeight()));
            if (targetView == imgMainPhoto) {
                llImages.setVisibility(View.VISIBLE);
                imgPhoto1.setVisibility(View.VISIBLE);
            } else if (targetView == imgPhoto1) {
                imgPhoto2.setVisibility(View.VISIBLE);
            }
        } else if (resultCode == Crop.RESULT_ERROR) {
            Toast.makeText(this, Crop.getError(result).getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    File getTempFile(int requestCode, ImageView view) {
        File f = new File(getExternalCacheDir(), String
                .format(Locale.US, "_%d_%d.jpg", requestCode, view.getId()));
        f.getParentFile().mkdirs();
        return f;
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
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
