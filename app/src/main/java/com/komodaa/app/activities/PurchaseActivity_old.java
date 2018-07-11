package com.komodaa.app.activities;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
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
import com.rey.material.widget.Button;
import com.rey.material.widget.RadioButton;

import net.darkion.AchievementUnlockedLib.AchievementUnlocked;

import org.json.JSONException;
import org.json.JSONObject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.pedant.SweetAlert.SweetAlertDialog;

public class PurchaseActivity_old extends AppCompatActivity {

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
    private int deliveryMethod;
    private SharedPreferences prefs;
    private double discountAmount;
    private int discountOn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_purchase_old);
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


    @OnClick({R.id.rbMigMigi, R.id.rbSnail, R.id.btnSubmit, R.id.rlMigMigMode, R.id.rlSnailMode})
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
                final Handler h = new Handler(getMainLooper());
                final Context c = this;

                if (!UserUtils.isLoggedIn()) {
                    Utils.displayLoginErrorDialog(c);
                    return;
                }
                final SweetAlertDialog pDialog = new SweetAlertDialog(c, SweetAlertDialog.NORMAL_TYPE);
                pDialog.setTitleText("خرید این آیتم");
                pDialog.setContentText(String.format(" بعد از این که درخواست ات ثبت شد، مبلغ %s تومن رو به شماره کارت %s به نام ثنا خالصی بریز، بعدش برو قسمت \"ثبت رسید پرداخت\" و اطلاعات لازم رو برامون بفرست.", Utils.formatNumber(priceSum), CARD_NUM));
                pDialog.setConfirmText("ثبت");
                pDialog.setCancelText("بعداً");
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
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    ApiClient.makeRequest(c, "POST", "/purchase/" + p.getId(), json, new NetworkCallback() {
                        @Override public void onSuccess(int status, JSONObject data) {
                            h.post(() -> {
                                resetDiscount();
                                pDialog.setTitleText("درخواست خرید ثبت شد !")
                                        .setContentText("24 ساعت وقت داری که مبلغ رو بریزی به سامانک و اطلاعات رسید رو توی کُمُدا ثبت کنی. وگرنه کُمُلولو درخواست ات رو قورت میده و کنسل میشه. مرسی که توی کمدایی.")
                                        .setConfirmText("چشم ممنونم")
                                        .setConfirmClickListener(sweetAlertDialog12 -> {
                                            pDialog.dismiss();
                                            startActivity(new Intent(c, PurchasedProductsActivity.class));
                                            finish();
                                        })
                                        .showCancelButton(false)
                                        .changeAlertType(SweetAlertDialog.SUCCESS_TYPE);
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

                break;
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
