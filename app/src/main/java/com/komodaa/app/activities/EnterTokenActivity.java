package com.komodaa.app.activities;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.MenuItem;

import com.komodaa.app.R;
import com.komodaa.app.interfaces.NetworkCallback;
import com.komodaa.app.models.Error;
import com.komodaa.app.utils.ApiClient;
import com.komodaa.app.utils.Utils;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.rey.material.widget.Button;

import org.json.JSONException;
import org.json.JSONObject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.pedant.SweetAlert.SweetAlertDialog;

public class EnterTokenActivity extends AppCompatActivity {

    @BindView(R.id.metVerificationCode) MaterialEditText metVerificationCode;
    @BindView(R.id.btnSend) Button btnSend;
    private String phoneNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enter_token);
        ButterKnife.bind(this);

        Intent intent = getIntent();
        if (intent == null || !intent.hasExtra("phone_number")) {
            finish();
        }
        getSupportActionBar().setTitle(Utils.generateTypeFacedString(this, getTitle().toString()));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        phoneNumber = intent.getStringExtra("phone_number");
    }

    @OnClick(R.id.btnSend) public void onClick() {
        String token = metVerificationCode.getText().toString();
        if (TextUtils.isEmpty(token) || token.length() < 6) {
            metVerificationCode.setError("کد تایید معتبر نیست");
            return;
        }

        final SweetAlertDialog pDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
        pDialog.getProgressHelper().setBarColor(Utils.getColorFromResource(this, R.color.colorPrimary));
        pDialog.setTitleText("بازیابی کلمه عبور");
        pDialog.setContentText(getString(R.string.loadin_please_wait));
        pDialog.setCancelable(false);
        pDialog.show();

        JSONObject data = null;
        try {
            data = new JSONObject();
            data.put("phone_number", phoneNumber);
            data.put("token", token);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        ApiClient.makeRequest(this, "POST", "/forget_password/validate", data, new NetworkCallback() {
            @Override public void onSuccess(int status, JSONObject data) {
                if (data.has("verification_token")) {
                    final String token;
                    try {
                        token = data.getString("verification_token");
                        pDialog.changeAlertType(SweetAlertDialog.SUCCESS_TYPE);
                        pDialog.showCancelButton(false).setConfirmText("باشه");
                        new Handler(getMainLooper()).postDelayed(() -> {
                            pDialog.dismissWithAnimation();
                            startActivity(new Intent(EnterTokenActivity.this, ChangePasswordActivity.class).putExtra("verification_token", token));
                            finish();
                        }, 600);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    return;


                }
                pDialog.setContentText("اطلاعات وارد شده معتبر نیست")
                        .setConfirmText("بیخیالش شو !")
                        .showCancelButton(false)
                        .setConfirmClickListener(Dialog::dismiss)
                        .changeAlertType(SweetAlertDialog.ERROR_TYPE);


            }

            @Override public void onFailure(int status, final Error error) {
                pDialog.setContentText(error.getMessage())
                        .setConfirmText("بیخیالش شو !")
                        .showCancelButton(false)
                        .setConfirmClickListener(Dialog::dismiss)
                        .changeAlertType(SweetAlertDialog.ERROR_TYPE);
            }
        });
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
            finishMe();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override public void onBackPressed() {
        finishMe();
    }

    void finishMe() {
        final SweetAlertDialog pDialog = new SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE);
        pDialog.getProgressHelper().setBarColor(Utils.getColorFromResource(this, R.color.colorPrimary));
        pDialog.setTitleText("مطمئنی ؟!");
        pDialog.setContentText("کد تاییدیه برات پیامک شده، مطمئنی میخوای بی‌خیال بشی ؟\nاگه الان بری باید از اول درخواست بازیابی کلمه‌ی عبور رو ارسال کنی.");
        pDialog.setCancelable(false);
        pDialog.setConfirmText("نه، نمیخوام برم");
        pDialog.setCancelText("آره، بی‌خیالش !");
        pDialog.setConfirmClickListener(sweetAlertDialog -> pDialog.dismissWithAnimation());
        pDialog.setCancelClickListener(sweetAlertDialog -> {
            pDialog.dismiss();
            finish();
        });
        pDialog.show();
    }
}
