package com.komodaa.app.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.SignUpEvent;
import com.komodaa.app.App;
import com.komodaa.app.R;
import com.komodaa.app.interfaces.NetworkCallback;
import com.komodaa.app.models.Error;
import com.komodaa.app.utils.ApiClient;
import com.komodaa.app.utils.UserUtils;
import com.komodaa.app.utils.Utils;
import com.onesignal.OneSignal;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.rey.material.widget.ProgressView;
import com.toptoche.searchablespinnerlibrary.SearchableSpinner;

import net.darkion.AchievementUnlockedLib.AchievementUnlocked;

import org.json.JSONException;
import org.json.JSONObject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SignupActivity extends AppCompatActivity {

    @BindView(R.id.metFirstName) MaterialEditText metFirstName;
    @BindView(R.id.metLastName) MaterialEditText metLastName;
    @BindView(R.id.metEmail) MaterialEditText metEmail;
    @BindView(R.id.metPhoneNumber) MaterialEditText metPhoneNumber;
    @BindView(R.id.imgCity) ImageView imgCity;
    @BindView(R.id.mspCity) SearchableSpinner mspCity;
    @BindView(R.id.metPassword) MaterialEditText metPassword;
    @BindView(R.id.tvPolicy) TextView tvPolicy;
    @BindView(R.id.btnSignup) Button btnSignup;
    @BindView(R.id.tvLogin) TextView tvLogin;
    @BindView(R.id.progress) ProgressView progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        ButterKnife.bind(this);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(Utils.generateTypeFacedString(this, getTitle().toString()));


        mspCity.setTitle("شهر محل سکونت خود را انتخاب کنید:");
        mspCity.setPositiveButton("باشه");

        mspCity.postDelayed(() -> {
            // Default City, Shiraz, before it was `cityId` which is user's selected city
            mspCity.setSelection(737);

        }, 500);
        //mspCity.setEnabled(false);


    }


    @OnClick({R.id.tvPolicy, R.id.btnSignup, R.id.tvLogin}) public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tvPolicy:
                startActivity(new Intent(this, RulesActivity.class));
                break;
            case R.id.btnSignup:
                signUp();
                break;
            case R.id.tvLogin:
                startActivity(new Intent(this, LoginActivity.class).putExtra("from_signup", true));
                finish();
                break;
        }
    }

    void signUp() {

        if (!Utils.isNetworkAvailable(this)) {
            AchievementUnlocked notif = new AchievementUnlocked(SignupActivity.this)
                    .setTitle("خطا !")
                    .setIcon(Utils.getDrawableFromRes(this, R.drawable.no))
                    .setTitleColor(Color.WHITE)
                    .setSubtitleColor(Color.WHITE)
                    .setTypeface(ResourcesCompat.getFont(this, R.font.iran_sans))
                    .setDuration(3500)
                    .setSubTitle("دسترسی به اینترنت وجود ندارد")
                    .setBackgroundColor(Utils.getColorFromResource(SignupActivity.this, R.color.notification_error))
                    .isLarge(false)
                    .build();

            notif.getAchievementView().setOnClickListener(v -> {
                Intent intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
                startActivity(intent);

            });
            notif.show();
            return;
        }
        String firstName = metFirstName.getText().toString();
        String lastName = metLastName.getText().toString();
        String phoneNumber = metPhoneNumber.getText().toString();
        final String email = metEmail.getText().toString();
        String password = metPassword.getText().toString();
        int cityId = mspCity.getSelectedItemPosition();

        Object selectedCity = mspCity.getSelectedItem();

        if (TextUtils.isEmpty(firstName)) {
            metFirstName.setError("لطفاً نام را وارد کنید");
            return;
        }
        if (TextUtils.isEmpty(lastName)) {
            metLastName.setError("لطفاً نام خانوادگی را وارد کنید");
            return;
        }

        if (!Utils.isPersianCharactersOnly(firstName)) {
            metFirstName.setError("لطفاً نام را به فارسی وارد کنید");
            return;
        }
        if (!Utils.isPersianCharactersOnly(lastName)) {
            metLastName.setError("لطفاً نام خانوادگی را به فارسی وارد کنید");
            return;
        }
//        if (TextUtils.isEmpty(email)) {
//            metEmail.setError("لطفاً ایمیل را وارد کنید");
//            return;
//        }
//        if (!Utils.isEmailValid(email)) {
//            metEmail.setError("لطفاً ایمیل معتبری را وارد کنید");
//            return;
//        }
        if (TextUtils.isEmpty(phoneNumber) || !TextUtils.isDigitsOnly(phoneNumber)) {
            metPhoneNumber.setError("لطفاً شماره همراه را وارد کنید");
            return;
        }
        if (TextUtils.isEmpty(password)) {
            metPassword.setError("لطفاً کلمه عبور را وارد کنید");
            return;
        }
        if (cityId == SpinnerAdapter.NO_SELECTION || selectedCity == null) {
            new AchievementUnlocked(SignupActivity.this)
                    .setTitle("خطا !")
                    .setIcon(Utils.getDrawableFromRes(this, R.drawable.no))
                    .setTitleColor(Color.WHITE)
                    .setSubtitleColor(Color.WHITE)
                    .setTypeface(ResourcesCompat.getFont(this, R.font.iran_sans))
                    .setDuration(1500)
                    .setSubTitle("لطفاً شهر خود را انتخاب کنید")
                    .setBackgroundColor(Utils.getColorFromResource(SignupActivity.this, R.color.notification_error))
                    .isLarge(false)
                    .build().show();
            return;
        }

        final String city = selectedCity.toString();
        JSONObject data = new JSONObject();
        try {
            data.put("first_name", firstName);
            data.put("last_name", lastName);
            data.put("city_id", cityId);
            data.put("city_name", city);
            data.put("phone_number", phoneNumber);
            data.put("email", email);
            data.put("password", password);
            data.put("android_version", Utils.getAndroidVersion());
            data.put("device_name", Utils.getDeviceName());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        btnSignup.setEnabled(false);
        progress.setVisibility(View.VISIBLE);
        ApiClient.makeRequest(this, "POST", "/signup", data, new NetworkCallback() {
            @Override public void onSuccess(int status, JSONObject data) {
                btnSignup.setEnabled(true);
                progress.setVisibility(View.GONE);

                AchievementUnlocked test =
                        new AchievementUnlocked(SignupActivity.this)
                                .setTitle("تبریک !")
                                .setIcon(Utils.getDrawableFromRes(SignupActivity.this, R.drawable.tick))
                                .setTitleColor(Color.WHITE)
                                .setSubtitleColor(Color.WHITE)
                                .setTypeface(ResourcesCompat.getFont(SignupActivity.this, R.font.iran_sans))
                                .setSubTitle("به جمع کاربران کمدا خوش اومدی.")
                                .setBackgroundColor(Utils.getColorFromResource(SignupActivity.this, R.color.notification_ok_purple))
                                .isLarge(false)
                                .build();

                test.show();
                try {
                    Answers.getInstance().logSignUp(new SignUpEvent()
                            .putMethod("Direct")
                            .putSuccess(true)
                            .putCustomAttribute("City", city)
                            .putCustomAttribute("Android Version", Utils.getAndroidVersion())
                            .putCustomAttribute("Device Name", Utils.getDeviceName())
                    );
                } catch (Exception e) {
                    e.printStackTrace();
                }
                OneSignal.syncHashedEmail(email);
                OneSignal.sendTag("userId", UserUtils.getUser().getId() + "");
                App.sendPushId(getApplicationContext());
                finish();

            }

            @Override public void onFailure(int status, Error error) {
                btnSignup.setEnabled(true);
                progress.setVisibility(View.GONE);

                Log.d("Komodaa", String.format("onFailure: st: %d msg= %s, err=%d", status, error.getMessage(), error.getErrorCode()));
                new AchievementUnlocked(SignupActivity.this)
                        .setTitle("خطا !")
                        .setIcon(Utils.getDrawableFromRes(SignupActivity.this, R.drawable.no))
                        .setTitleColor(Color.WHITE)
                        .setSubtitleColor(Color.WHITE)
                        .setTypeface(ResourcesCompat.getFont(SignupActivity.this, R.font.iran_sans))
                        .setSubTitle(error.getMessage())
                        .setBackgroundColor(Color.parseColor("#e51c23"))
                        .isLarge(false)
                        .build().show();

                try {
                    Answers.getInstance().logSignUp(new SignUpEvent()
                            .putMethod("Direct")
                            .putSuccess(false)
                            .putCustomAttribute("City", city)
                            .putCustomAttribute("Android Version", Utils.getAndroidVersion())
                            .putCustomAttribute("Device Name", Utils.getDeviceName())
                    );
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
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
