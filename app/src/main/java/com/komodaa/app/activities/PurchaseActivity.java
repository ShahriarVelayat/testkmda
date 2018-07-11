package com.komodaa.app.activities;

import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.komodaa.app.App;
import com.komodaa.app.R;
import com.komodaa.app.interfaces.NetworkCallback;
import com.komodaa.app.models.Error;
import com.komodaa.app.models.Product;
import com.komodaa.app.utils.ApiClient;
import com.komodaa.app.utils.UserUtils;
import com.komodaa.app.utils.Utils;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.rey.material.widget.Button;
import com.rey.material.widget.RadioButton;

import net.darkion.AchievementUnlockedLib.AchievementUnlocked;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.pedant.SweetAlert.SweetAlertDialog;

public class PurchaseActivity extends AppCompatActivity {

    private static final String CARD_NUM = "4996-2896-8610-6219"; // "6219-8610-2896-4996"
    private static final int TYPE_SNAIL = 2;
    private static final int TYPE_MIGMIG = 1;
    @BindView(R.id.rbMigMigi) RadioButton rbMigMigi;
    @BindView(R.id.rbSnail) RadioButton rbSnail;
    @BindView(R.id.tvProductprice) TextView tvProductPrice;
    @BindView(R.id.tvShipmentPrice) TextView tvShipmentPrice;
    @BindView(R.id.tvSum) TextView tvSum;
    @BindView(R.id.separator) View separator;
    @BindView(R.id.btnSubmit) Button btnSubmit;
    Product p;
    int snail, migmig, priceSum;
    @BindView(R.id.rlMigMigMode) RelativeLayout rlMigMigMode;
    @BindView(R.id.rlSnailMode) RelativeLayout rlSnailMode;
    @BindView(R.id.metAddress) MaterialEditText metAddress;
    @BindView(R.id.ivBadge) ImageView ivBadge;
    private int deliveryMethod;
    private SharedPreferences prefs;
    private double discountAmount;
    private int discountOn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_purchase);
        ButterKnife.bind(this);

        Intent intent = getIntent();
        if (intent == null || !intent.hasExtra("item")) {
            finish();
            return;
        }
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(Utils.generateTypeFacedString(this, getTitle().toString()));
        p = intent.getParcelableExtra("item");

        tvProductPrice.setText(String.format("%s تومان", Utils.formatNumber(p.getPrice())));

        prefs = App.getPrefs();
        String json = prefs.getString("config", null);
        try {
            JSONObject config = new JSONObject(json);
            JSONObject delivery = config.getJSONObject("delivery");
            snail = delivery.getInt("snail");
            migmig = delivery.getInt("migmig");


            rbMigMigi.setText(String.format("%s تومان", Utils.formatNumber(migmig)));
            rbSnail.setText(String.format("%s تومان", Utils.formatNumber(snail)));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String confs = prefs.getString("user_meta", null);
        if (!TextUtils.isEmpty(confs)) {
            try {
                JSONObject j = new JSONObject(confs);
                // Set address field based on user meta
                if (j.has("meta")) {
                    JSONArray meta = j.getJSONArray("meta");
                    for (int i = 0; i < meta.length(); i++) {
                        JSONObject m = meta.getJSONObject(i);
                        if (m.getString("meta").equals("address")) {
                            metAddress.setText(m.getString("value"));
                            break;
                        }
                    }
                }
                if (j.has("discount_amount")) {
                    discountAmount = j.getDouble("discount_amount");
                    int amount = snail;
                    discountOn = TYPE_SNAIL;
                    RadioButton radio = rbSnail;
                    if (j.has("discount_on")) {
                        discountOn = j.getInt("discount_on");
                    }
                    if (discountOn == TYPE_MIGMIG) {
                        radio = rbMigMigi;
                        migmig = (int) (migmig * discountAmount);
                        amount = migmig;
                    } else if (discountOn == TYPE_SNAIL) {
                        radio = rbSnail;
                        snail = (int) (snail * discountAmount);
                        amount = snail;
                    }
                    radio.setText(String.format("%s تومان", Utils.formatNumber(amount)));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
//        rbMigMigi.setChecked(true);
//        calculateSum(migmig);
    }

    void calculateSum(int shipmentPrice) {

        tvShipmentPrice.setText(String.format("%s تومان", Utils.formatNumber(shipmentPrice)));
        priceSum = p.getPrice() + shipmentPrice;
        tvSum.setText(String.format("جمع فاکتور: %s", Utils.formatNumber(priceSum)));
    }


    @OnClick({R.id.rbMigMigi, R.id.rbSnail, R.id.btnSubmit, R.id.rlMigMigMode, R.id.rlSnailMode, R.id.ivBadge, R.id.btnHelp})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.rlMigMigMode:
            case R.id.rbMigMigi:
                rbSnail.setChecked(false);
                rbMigMigi.setChecked(true);
                calculateSum(migmig);
                deliveryMethod = TYPE_MIGMIG;
                break;
            case R.id.rlSnailMode:
            case R.id.rbSnail:
                rbMigMigi.setChecked(false);
                rbSnail.setChecked(true);
                calculateSum(snail);
                deliveryMethod = TYPE_SNAIL;
                break;
            case R.id.btnSubmit:
                submit();

                break;
            case R.id.ivBadge:
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.zarinpal.com/webservice/verifyWebsite/komodaa.com")));
                break;
            case R.id.btnHelp:
                Intent intent = new Intent(this, HelpActivity.class);
                intent.putExtra("title", "آموزش خرید از کمدا");
                intent.putExtra("url", "https://komodaa.com/help/purchase");
                startActivity(intent);
                break;
        }
    }

    public void submit() {
        if (!rbSnail.isChecked() && !rbMigMigi.isChecked()) {
            new AchievementUnlocked(this)
                    .setTitle("خطا !")
                    .setIcon(Utils.getDrawableFromRes(this, R.drawable.no))
                    .setTitleColor(Color.WHITE)
                    .setSubtitleColor(Color.WHITE)
                    .setTypeface(ResourcesCompat.getFont(this, R.font.iran_sans))
                    .setDuration(3500)
                    .setSubTitle("روش ارسال رو هم انتخاب کن لطفاً")
                    .setBackgroundColor(Utils.getColorFromResource(this, R.color.notification_error))
                    .isLarge(false)
                    .build().show();
            return;
        }
        if (TextUtils.isEmpty(metAddress.getText().toString())) {
            metAddress.setError("آدرس رو وارد کن !");
            return;
        }
        final Handler h = new Handler(getMainLooper());
        final Context c = this;

        if (!UserUtils.isLoggedIn()) {
            Utils.displayLoginErrorDialog(c);
            return;
        }
        final SweetAlertDialog pDialog = new SweetAlertDialog(c, SweetAlertDialog.NORMAL_TYPE);
        pDialog.setTitleText("خرید این آیتم");
        String title = "لطفا از اینجا به بعد صفحه کلیدت رو روی انگلیسی بذار تا با هر کارت بانکی که داری بری به درگاه امن پرداخت.";
        Spannable sp = new SpannableString(title);
        sp.setSpan(new ForegroundColorSpan(Color.RED), 0, title.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        pDialog.setContentText(sp);
        pDialog.setConfirmText("ثبت");
        pDialog.setCancelText("لغو");
        pDialog.setConfirmClickListener(sweetAlertDialog -> {
            pDialog.setTitleText("در حال ثبت آیتم")
                    .setContentText(" داریم توی سِرور ثبتش می کنیم")
                    .showCancelButton(false)
                    .changeAlertType(SweetAlertDialog.PROGRESS_TYPE);
            JSONObject json = null;
            try {
                json = new JSONObject();
                json.put("delivery_method", deliveryMethod);
                json.put("total_cost", priceSum);
                json.put("delivery_discount", discountAmount);
                json.put("delivery_discount_on", discountOn);
                json.put("address", metAddress.getText().toString());
            } catch (Exception e) {
                e.printStackTrace();
            }
            ApiClient.makeRequest(c, "POST", "/pay/" + p.getId(), json, new NetworkCallback() {
                @Override public void onSuccess(int status, final JSONObject data) {
                    h.post(() -> {
                        resetDiscount();
//                                        pDialog.setTitleText("درخواست خرید ثبت شد !")
//                                                .setContentText("24 ساعت وقت داری که مبلغ رو بریزی به سامانک و اطلاعات رسید رو توی کُمُدا ثبت کنی. وگرنه کُمُلولو درخواست ات رو قورت میده و کنسل میشه. مرسی که توی کمدایی.")
//                                                .setConfirmText("چشم ممنونم")
//                                                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
//                                                    @Override
//                                                    public void onClick(SweetAlertDialog sweetAlertDialog) {
//                                                        pDialog.dismiss();
//                                                        startActivity(new Intent(c, PurchasedProductsActivity.class));
//                                                        finish();
//                                                    }
//                                                })
//                                                .showCancelButton(false)
//                                                .changeAlertType(SweetAlertDialog.SUCCESS_TYPE);
                        try {
                            if (data.has("url") && !TextUtils.isEmpty(data.getString("url"))) {

                                pDialog.dismissWithAnimation();


                                // First, try to open url in Chrome Browser
                                Uri uri = Uri.parse(data.getString("url"));
                                final Intent chromeIntent = new Intent(Intent.ACTION_VIEW, uri);
                                chromeIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                chromeIntent.setPackage("com.android.chrome");


                                //final Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(data.getString("url")));
                                try {
                                    startActivity(chromeIntent);
                                    return;
                                } catch (Exception e) {
                                    // If Chrome is not Available, Try Default browser
                                    final Intent defaultIntent = new Intent(Intent.ACTION_VIEW, uri);
                                    defaultIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                                    defaultIntent.setComponent(new ComponentName("com.android.browser",
                                            "com.android.browser.BrowserActivity"));

                                    try {
                                        startActivity(defaultIntent);
                                        return;
                                    } catch (Exception e1) {
                                        // If both Chrome Browser and Default Browser were unavailable
                                        // let the user choose the browser
                                        final Intent i = new Intent(Intent.ACTION_VIEW, uri);
                                        startActivity(i);
                                        return;
                                    }

                                }

                            }

                        } catch (JSONException e) {
                        }

                        pDialog.setTitleText("مثل اینکه یه مشکلی پیش اومده")
                                .setContentText("")
                                .setConfirmText("بیخیالش شو !")
                                .showCancelButton(false)
                                .setConfirmClickListener(Dialog::dismiss)
                                .changeAlertType(SweetAlertDialog.ERROR_TYPE);
                    });

                }

                @Override public void onFailure(int status, final Error error) {
                    h.post(() -> pDialog.setTitleText("مثل اینکه یه مشکلی پیش اومده")
                            .setContentText(error.getMessage())
                            .setConfirmText("بیخیالش شو !")
                            .showCancelButton(false)
                            .setConfirmClickListener(Dialog::dismiss)
                            .changeAlertType(SweetAlertDialog.ERROR_TYPE));

                }
            });
        });

        pDialog.setCustomImage(R.drawable.logo_komodaa);
        pDialog.show();
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

    void resetDiscount() {
        String confs = prefs.getString("user_meta", null);
        if (!TextUtils.isEmpty(confs)) {
            try {
                JSONObject j = new JSONObject(confs);
                if (j.has("discount_amount")) {

                    j.remove("discount_amount");

                }
                if (j.has("discount_on")) {
                    j.remove("discount_on");
                }
                prefs.edit().putString("user_meta", j.toString()).apply();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
