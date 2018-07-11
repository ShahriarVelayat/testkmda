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

public class ForgetPasswordActivity extends AppCompatActivity {

    @BindView(R.id.metPhoneNumber) MaterialEditText metPhoneNumber;
    @BindView(R.id.btnSend) Button btnSend;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forget_password);
        ButterKnife.bind(this);

        getSupportActionBar().setTitle(Utils.generateTypeFacedString(this, getTitle().toString()));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @OnClick(R.id.btnSend) public void onClick() {
        final String phoneNumber = metPhoneNumber.getText().toString();
        if (TextUtils.isEmpty(phoneNumber) || phoneNumber.length() < 11) {
            metPhoneNumber.setError("شماره موبایلت معتبر نیست");
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
        } catch (JSONException e) {
            e.printStackTrace();
        }
        ApiClient.makeRequest(this, "POST", "/forget_password/request", data, new NetworkCallback() {
            @Override public void onSuccess(int status, JSONObject data) {
                try {
                    if (data.has("status") && data.getBoolean("status")) {
                        pDialog.changeAlertType(SweetAlertDialog.SUCCESS_TYPE);
                        pDialog.showCancelButton(false).setConfirmText("باشه");
                        new Handler(getMainLooper()).postDelayed(() -> {
                            pDialog.dismissWithAnimation();
                            startActivity(new Intent(ForgetPasswordActivity.this, EnterTokenActivity.class).putExtra("phone_number", phoneNumber));
                        }, 600);
                        return;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
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
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
