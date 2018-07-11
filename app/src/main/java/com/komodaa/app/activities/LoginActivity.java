package com.komodaa.app.activities;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;
import com.crashlytics.android.answers.LoginEvent;
import com.komodaa.app.App;
import com.komodaa.app.R;
import com.komodaa.app.interfaces.NetworkCallback;
import com.komodaa.app.models.Error;
import com.komodaa.app.utils.ApiClient;
import com.komodaa.app.utils.UserUtils;
import com.komodaa.app.utils.Utils;
import com.onesignal.OneSignal;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.rey.material.widget.Button;
import com.rey.material.widget.ProgressView;

import net.darkion.AchievementUnlockedLib.AchievementUnlocked;

import org.json.JSONException;
import org.json.JSONObject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.pedant.SweetAlert.SweetAlertDialog;

public class LoginActivity extends AppCompatActivity {

    @BindView(R.id.metPhoneNumber) MaterialEditText metPhoneNumber;
    @BindView(R.id.metPassword) MaterialEditText metPassword;
    @BindView(R.id.btnLogin) Button btnLogin;
    @BindView(R.id.tvForgetPassword) TextView tvForgetPassword;
    @BindView(R.id.progress) ProgressView progress;
    private boolean goToMain;
    private boolean isFromSignup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(Utils.generateTypeFacedString(this, getTitle().toString()));

        Intent intent = getIntent();
        if (intent != null) {
            goToMain = intent.getBooleanExtra("go_to_main", false);
            isFromSignup = intent.getBooleanExtra("from_signup", false);
        }

        tvForgetPassword.setPaintFlags(tvForgetPassword.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
    }

    @OnClick({R.id.btnLogin, R.id.tvForgetPassword}) public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnLogin:
                login();
                break;
            case R.id.tvForgetPassword:
                //showForgetPasswordDialog();
                startActivity(new Intent(this, ForgetPasswordActivity.class));
                break;
        }
    }

    void showForgetPasswordDialog() {
        final Dialog d = new Dialog(this);
        d.getWindow().setBackgroundDrawable(
                new ColorDrawable(
                        android.graphics.Color.TRANSPARENT));
        d.requestWindowFeature(Window.FEATURE_NO_TITLE);
        d.setContentView(R.layout.dialog_forget_password);

        d.findViewById(R.id.imgClose).setOnClickListener(v -> d.dismiss());
        final MaterialEditText metEmailForget = d.findViewById(R.id.metEmail);


        d.findViewById(R.id.btnSearch).setOnClickListener(v -> {
            String email = metEmailForget.getText().toString();
            if (TextUtils.isEmpty(email) || !Utils.isEmailValid(email)) {
                metEmailForget.setError("ایمیل معتبری وارد کنید");
                return;
            }

            final SweetAlertDialog pDialog = new SweetAlertDialog(LoginActivity.this, SweetAlertDialog.PROGRESS_TYPE);
            pDialog.getProgressHelper().setBarColor(Utils.getColorFromResource(LoginActivity.this, R.color.colorPrimary));
            pDialog.setTitleText("در حال ثبت درخواست");
            pDialog.setContentText("چند لحظه منتظر بمونید...");
            pDialog.setCancelable(false);
            pDialog.show();
            JSONObject data = null;
            data = new JSONObject();
            try {
                data.put("email", email);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            Answers.getInstance().logCustom(new CustomEvent("Forget Password"));

            final Handler h = new Handler(getMainLooper());
            ApiClient.makeRequest(LoginActivity.this, "POST", "/forget_password", data, new NetworkCallback() {
                @Override public void onSuccess(int status, JSONObject data) {

                    h.post(() -> pDialog.setTitleText("درخواست ثبت شد")
                            .setContentText("یک ایمیل براتون ارسال میشه که حاوی لینک بازیابی کلمه‌ی عبور هست")
                            .setConfirmText("باشه، مرسی")
                            .setConfirmClickListener(sweetAlertDialog -> {
                                sweetAlertDialog.dismiss();
                                d.dismiss();
                            })
                            .changeAlertType(SweetAlertDialog.SUCCESS_TYPE));
                }

                @Override public void onFailure(int status, final Error error) {
                    h.post(() -> pDialog.setTitleText("اوه اوه ! مثل اینکه خطایی رخ داده")
                            .setContentText(error.getMessage())
                            .setConfirmText("بیخیالش شو !")
                            .showCancelButton(false)
                            .setConfirmClickListener(Dialog::dismiss)
                            .setCancelClickListener(Dialog::dismiss)
                            .changeAlertType(SweetAlertDialog.ERROR_TYPE));
                }
            });
        });
        d.show();
    }

    private void login() {

        if (!Utils.isNetworkAvailable(this)) {
            AchievementUnlocked notif = new AchievementUnlocked(LoginActivity.this)
                    .setTitle("خطا !")
                    .setIcon(Utils.getDrawableFromRes(this, R.drawable.no))
                    .setTitleColor(Color.WHITE)
                    .setSubtitleColor(Color.WHITE)
                    .setTypeface(ResourcesCompat.getFont(this, R.font.iran_sans))
                    .setDuration(3500)
                    .setSubTitle("دسترسی به اینترنت وجود ندارد")
                    .setBackgroundColor(Utils.getColorFromResource(this, R.color.notification_error))
                    .isLarge(false)
                    .build();

            notif.getAchievementView().setOnClickListener(v -> {
                Intent intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
                startActivity(intent);

            });
            notif.show();
            return;
        }

        final String phoneNumber = metPhoneNumber.getText().toString();
        String password = metPassword.getText().toString();

        if (TextUtils.isEmpty(phoneNumber)) {
            metPhoneNumber.setError("شماره موبایل وارد نشده است");
            return;
        }
        if (phoneNumber.length() < 11) {
            metPhoneNumber.setError("شماره موبایل معتبر نیست");
            return;
        }

//        if (!Utils.isEmailValid(phoneNumber)) {
//            metEmail.setError("لطفاً ایمیل معتبری را وارد کنید");
//            return;
//        }
        if (TextUtils.isEmpty(password)) {
            metPassword.setError("لطفاً کلمه عبور را وارد کنید");
            return;
        }

        JSONObject data = new JSONObject();
        try {

            data.put("phone_number", phoneNumber);
            data.put("password", password);
            data.put("android_version", Utils.getAndroidVersion());
            data.put("device_name", Utils.getDeviceName());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        progress.setVisibility(View.VISIBLE);
        btnLogin.setEnabled(false);
        ApiClient.makeRequest(this, "POST", "/login", data, new NetworkCallback() {
            @Override public void onSuccess(int status, JSONObject data) {
                btnLogin.setEnabled(true);
                progress.setVisibility(View.GONE);
                AchievementUnlocked test =
                        new AchievementUnlocked(LoginActivity.this)
                                .setTitle("تبریک !")
                                .setIcon(Utils.getDrawableFromRes(LoginActivity.this, R.drawable.tick))
                                .setTitleColor(Color.WHITE)
                                .setSubtitleColor(Color.WHITE)
                                .setTypeface(ResourcesCompat.getFont(LoginActivity.this, R.font.iran_sans))
                                .setSubTitle("از کمدا لذت ببر.")
                                .setBackgroundColor(Utils.getColorFromResource(LoginActivity.this, R.color.notification_ok_purple))
                                .isLarge(false)
                                .build();

                test.show();
                try {
                    Answers.getInstance().logLogin(new LoginEvent()
                            .putMethod("Direct")
                            .putSuccess(true)
                            .putCustomAttribute("user-id", UserUtils.getUser().getId() + ""));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                //OneSignal.syncHashedEmail(phoneNumber);
                OneSignal.sendTag("userId", UserUtils.getUser().getId() + "");
                App.sendPushId(getApplicationContext());
                if (goToMain) {
                    startActivity(new Intent(LoginActivity.this, HomeActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                }
                finish();

            }

            @Override public void onFailure(int status, Error error) {
                btnLogin.setEnabled(true);
                progress.setVisibility(View.GONE);
                Log.d("Komodaa", String.format("onFailure: st: %d msg= %s, err=%d", status, error.getMessage(), error.getErrorCode()));
                new AchievementUnlocked(LoginActivity.this)
                        .setTitle("خطا !")
                        .setIcon(Utils.getDrawableFromRes(LoginActivity.this, R.drawable.no))
                        .setTitleColor(Color.WHITE)
                        .setSubtitleColor(Color.WHITE)
                        .setTypeface(ResourcesCompat.getFont(LoginActivity.this, R.font.iran_sans))
                        .setSubTitle(error.getMessage())
                        .setBackgroundColor(Utils.getColorFromResource(LoginActivity.this, R.color.notification_error))
                        .isLarge(false)
                        .build().show();

                try {
                    Answers.getInstance().logLogin(new LoginEvent()
                            .putMethod("Direct")
                            .putSuccess(false));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

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
                if (isFromSignup) {
                    startActivity(new Intent(this, SignupActivity.class));
                } else if (goToMain) {
                    startActivity(new Intent(this, HomeActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                }
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
