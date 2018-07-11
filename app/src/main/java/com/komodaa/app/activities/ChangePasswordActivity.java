package com.komodaa.app.activities;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.MenuItem;

import com.komodaa.app.R;
import com.komodaa.app.interfaces.NetworkCallback;
import com.komodaa.app.models.Error;
import com.komodaa.app.utils.ApiClient;
import com.komodaa.app.utils.Utils;
import com.rengwuxian.materialedittext.MaterialEditText;

import org.json.JSONException;
import org.json.JSONObject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.pedant.SweetAlert.SweetAlertDialog;

public class ChangePasswordActivity extends AppCompatActivity {

    @BindView(R.id.metPassword) MaterialEditText metPassword;
    @BindView(R.id.metPasswordRepeat) MaterialEditText metPasswordRepeat;
    private String token;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);
        ButterKnife.bind(this);

        Intent intent = getIntent();
        if (intent == null || !intent.hasExtra("verification_token")) {
            finish();
        }

        token = intent.getStringExtra("verification_token");
        if (TextUtils.isEmpty(token)) {
            finish();
        }

        getSupportActionBar().setTitle(Utils.generateTypeFacedString(this, getTitle().toString()));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @OnClick(R.id.btnChangePassword) public void onClick() {
        String password = metPassword.getText().toString();
        String password2 = metPasswordRepeat.getText().toString();
        if (TextUtils.isEmpty(password)) {
            metPassword.setError("کلمه‌ی عبور را وارد کنید");
            return;
        }
        if (TextUtils.isEmpty(password2)) {
            metPasswordRepeat.setError("کلمه‌ی عبور را وارد کنید");
            return;
        }
        if (!password.equals(password2)) {
            metPasswordRepeat.setError("تکرار کلمه‌ی عبور با کلمه‌ی عبور وارد شده همخوانی ندارد");
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
            data.put("verification_token", token);
            data.put("password", password);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        ApiClient.makeRequest(this, "POST", "/forget_password/change_password", data, new NetworkCallback() {
            @Override public void onSuccess(int status, JSONObject data) {

                try {
                    boolean isOK = data.getBoolean("status");
                    String message = data.getString("message");
                    if (isOK) {
                        pDialog.setContentText(message)
                                .setConfirmText("باشه")
                                .showCancelButton(false)
                                .setConfirmClickListener(sweetAlertDialog -> {
                                    sweetAlertDialog.dismiss();
                                    startActivity(new Intent(ChangePasswordActivity.this, LoginActivity.class)
                                            .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK)
                                            .putExtra("go_to_main", true)
                                    );
                                    finish();
                                })
                                .changeAlertType(SweetAlertDialog.SUCCESS_TYPE);
                        return;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                pDialog.setContentText("مثل اینکه خطایی رخ داده")
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
        pDialog.setContentText("هنوز کلمه‌ی عبور رو تغییر ندادی، مطمئنی میخوای بی‌خیال بشی ؟\nاگه الان بری باید از اول درخواست بازیابی کلمه‌ی عبور رو ارسال کنی.");
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

