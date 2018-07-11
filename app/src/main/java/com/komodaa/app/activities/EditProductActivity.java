package com.komodaa.app.activities;

import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.InputType;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.komodaa.app.R;
import com.komodaa.app.interfaces.NetworkCallback;
import com.komodaa.app.models.Attribute;
import com.komodaa.app.models.Error;
import com.komodaa.app.models.Product;
import com.komodaa.app.utils.ApiClient;
import com.komodaa.app.utils.NumberTextWatcherForThousand;
import com.komodaa.app.utils.UserUtils;
import com.komodaa.app.utils.Utils;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.rey.material.widget.Button;

import net.darkion.AchievementUnlockedLib.AchievementUnlocked;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.pedant.SweetAlert.SweetAlertDialog;

public class EditProductActivity extends AppCompatActivity {

    @BindView(R.id.metPrice) MaterialEditText metPrice;
    @BindView(R.id.llAttribsContainer) LinearLayout llAttribsContainer;
    @BindView(R.id.metDescription) MaterialEditText metDescription;
    @BindView(R.id.btnNext) Button btnNext;
    List<MaterialEditText> views = new ArrayList<>();
    @BindView(R.id.scrollView) ScrollView scrollView;
    @BindView(R.id.tvPercent) TextView tvPercent;
    @BindView(R.id.btnFeatured) Button btnFeatured;
    private Product p;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_product);
        ButterKnife.bind(this);
        getSupportActionBar().setTitle(Utils.generateTypeFacedString(this, getTitle().toString()));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Intent intent = getIntent();
        if (intent == null || !intent.hasExtra("item")) {
            finish();
        }

        p = intent.getParcelableExtra("item");
        metPrice.addTextChangedListener(new NumberTextWatcherForThousand(metPrice));
        metPrice.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

                int percent = 0;
                try {
                    double price = Double.parseDouble(NumberTextWatcherForThousand.trimCommaOfString(s.toString()));
                    double l = p.getPrice() - price;
                    double a = 5500 / 15000;
                    Log.d("Percent", "afterTextChanged: " + a);
                    double l1 = l / price;
                    Log.d("Percent", String.format("Original: %d, Price: %f, diff: %f, div: %f", p.getPrice(), price, l, l1));
                    percent = (int) (l1 * 100);
                    //Log.d("Percent", String.format("Original: %d, Price: %d, percent: %d",p.getPrice(),price, percent));

                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }

                tvPercent.setText(percent > 0 ? String.format(Locale.US, "%d درصد تخفیف", percent) : "");
            }
        });
        metPrice.setText(p.getPrice() + "");
        metDescription.setText(p.getDescription());

        updateAttributesList(p.getAttributes());


    }

    void updateAttributesList(List<Attribute> attribs) {
        if (attribs == null || attribs.size() < 1) {
            return;
        }
        llAttribsContainer.removeAllViews();
        views = new ArrayList<>();
        for (Attribute a : attribs) {
            llAttribsContainer.addView(createView(a));
        }
    }

    LinearLayout createView(Attribute a) {

        LinearLayout l = (LinearLayout) getLayoutInflater().inflate(R.layout.template_attribute, llAttribsContainer, false);
        MaterialEditText m = l.findViewById(R.id.metAttributeValue);
        TextView t = l.findViewById(R.id.tvAttributeName);

        t.setText(a.getName());

        m.setHint(a.getName());
        m.setId(a.getId());
        int inputType = InputType.TYPE_CLASS_NUMBER;
        if (!TextUtils.isEmpty(a.getInputType())) {
            switch (a.getInputType()) {
                case "numeric":
                    inputType = InputType.TYPE_CLASS_NUMBER;
                    break;
                case "text":
                    inputType = InputType.TYPE_TEXT_VARIATION_NORMAL;
                    break;
                case "email":
                    inputType = InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS;
                    break;
                case "password_text":
                    inputType = InputType.TYPE_TEXT_VARIATION_PASSWORD;
                    break;
                case "password_number":
                    inputType = InputType.TYPE_NUMBER_VARIATION_PASSWORD;
                    break;
                default:
                    inputType = InputType.TYPE_TEXT_VARIATION_NORMAL;
            }

        }
        //m.setInputType(inputType);
        m.setText(a.getValue());
        //m.setFocusable(true);
        //m.setFocusableInTouchMode(true);
//        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
//        m.setLayoutParams(params);
        m.setEnabled(true);
        m.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(v, InputMethodManager.SHOW_IMPLICIT);
            }
        });
        views.add(m);
        return l;
    }

    @OnClick(R.id.btnNext)
    public void onViewClicked() {
        if (!UserUtils.isLoggedIn()) {
            Utils.displayLoginErrorDialog(this);
            return;
        }

        String price = NumberTextWatcherForThousand.trimCommaOfString(metPrice.getText().toString());
        if (TextUtils.isEmpty(price) || Integer.parseInt(price) < 1000) {
            metPrice.setError("مبلغ باید عددی بالاتر از ۱۰۰۰ تومان باشد");
            return;
        }
        for (final MaterialEditText e : views) {
            if (TextUtils.isEmpty(e.getText())) {
                e.setError("مقداری برای این قسمت وارد کنید");
                scrollView.post(() -> {
                    scrollView.scrollTo(0, e.getBottom() + e.getHeight() + Utils.dpToPx(this, 16));
                    e.requestFocus();
                });
                return;
            }
        }
        if (TextUtils.isEmpty(metDescription.getText().toString())) {
            metDescription.setError("توضیحات محصول را وارد کنید");
            return;
        }

        final SweetAlertDialog pDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
        pDialog.getProgressHelper().setBarColor(Utils.getColorFromResource(this, R.color.colorPrimary));
        pDialog.setTitleText("در حال ویرایش آیتم");
        pDialog.setContentText("داریم آیتم رو ویرایش می کنیم");
        pDialog.setCancelable(false);
        pDialog.show();


        JSONObject data = null;
        try {
            JSONArray attribs = new JSONArray();
            for (MaterialEditText e : views) {
                JSONObject obj = new JSONObject();
                obj.put(e.getId() + "", e.getText().toString());
                attribs.put(obj);
            }

            data = new JSONObject();
            data.put("price", price);
            data.put("attribs", attribs);
            data.put("description", metDescription.getText().toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        final Handler h = new Handler(this.getMainLooper());
        ApiClient.makeRequest(this, "POST", "/product/" + p.getId(), data, new NetworkCallback() {
            @Override
            public void onSuccess(int status, JSONObject data) {

                h.post(() -> pDialog.setTitleText("آیتم ویرایش شد")
                        .setContentText("تغییرات مدنظرت روی آیتم اعمال شد.")
                        .setConfirmText("باشه، مرسی")
                        .setConfirmClickListener(sweetAlertDialog -> {
                            sweetAlertDialog.dismiss();
                            setResult(RESULT_OK);
                            finish();


                        })
                        .changeAlertType(SweetAlertDialog.SUCCESS_TYPE));
            }

            @Override
            public void onFailure(int status, final Error error) {
                h.post(() -> pDialog.setTitleText("اوه اوه ! مثل اینکه خطایی رخ داده")
                        .setContentText(error.getMessage())
                        .setConfirmText("بیخیالش شو !")
                        .showCancelButton(false)
                        .setConfirmClickListener(Dialog::dismiss)
                        .setCancelClickListener(Dialog::dismiss)
                        .changeAlertType(SweetAlertDialog.ERROR_TYPE));
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

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

    @OnClick(R.id.btnFeatured)
    public void onFeaturedClicked() {
        promote();
    }

    public void promote() {

        final Handler h = new Handler(getMainLooper());
        final Context c = this;

        if (!UserUtils.isLoggedIn()) {
            Utils.displayLoginErrorDialog(c);
            return;
        }
        final SweetAlertDialog pDialog = new SweetAlertDialog(c, SweetAlertDialog.NORMAL_TYPE);
        pDialog.setTitleText("ویژه کردن آیتم");
        String title = "لطفاً صفحه کلید خودتون رو روی انگلیسی قرار بدید تا در درگاه پرداخت اعداد به صورت لاتین وارد بشه و مشکلی پیش نیاد.";
        Spannable sp = new SpannableString(title
                + "\n"
                + "برای پرداخت وجه به سمت درگاه پرداخت هدایت می شی. اگر مطمئن هستی دکمه ثبت رو لمس کن.");
        sp.setSpan(new ForegroundColorSpan(Color.RED), 0, title.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        pDialog.setContentText(sp);
        pDialog.setConfirmText("ثبت");
        pDialog.setCancelText("بعداً");
        pDialog.setConfirmClickListener(sweetAlertDialog -> {
            pDialog.setTitleText("در حال ویژه کردن آیتم")
                    .setContentText(" داریم توی سِرور ثبتش می کنیم")
                    .showCancelButton(false)
                    .changeAlertType(SweetAlertDialog.PROGRESS_TYPE);

            ApiClient.makeRequest(c, "POST", "/product/promote/" + p.getId(), null, new NetworkCallback() {
                @Override
                public void onSuccess(int status, final JSONObject data) {
                    h.post(() -> {

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

                        } catch (JSONException ignored) {
                        }

                        pDialog.setTitleText("مثل اینکه یه مشکلی پیش اومده")
                                .setContentText("")
                                .setConfirmText("بیخیالش شو !")
                                .showCancelButton(false)
                                .setConfirmClickListener(Dialog::dismiss)
                                .changeAlertType(SweetAlertDialog.ERROR_TYPE);
                    });

                }

                @Override
                public void onFailure(int status, final Error error) {
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
}
