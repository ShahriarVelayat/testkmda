package com.komodaa.app.activities;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;

import com.komodaa.app.App;
import com.komodaa.app.R;
import com.komodaa.app.interfaces.NetworkCallback;
import com.komodaa.app.models.Error;
import com.komodaa.app.models.Product;
import com.komodaa.app.utils.ApiClient;
import com.komodaa.app.utils.Utils;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.rey.material.widget.Button;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.pedant.SweetAlert.SweetAlertDialog;

public class SubmitPaymentReceipt extends AppCompatActivity {

    @BindView(R.id.metPrice) MaterialEditText metPrice;
    @BindView(R.id.metBank) MaterialEditText metBank;
    @BindView(R.id.metReceiptNo) MaterialEditText metReceiptNo;
    @BindView(R.id.btnHelp) Button btnHelp;
    @BindView(R.id.btnSubmit) Button btnSubmit;
    @BindView(R.id.metAddress) MaterialEditText metAddress;
    private Product p;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_submit_payment_recied);
        ButterKnife.bind(this);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(Utils.generateTypeFacedString(this, getTitle().toString()));
        Intent intent = getIntent();
        if (intent == null || !intent.hasExtra("item")) {
            finish();
        }

        p = intent.getParcelableExtra("item");
        metPrice.setText(String.format("%s تومان", Utils.formatNumber(p.getTotalCost())));
        String confs = App.getPrefs().getString("user_meta", null);
        if (!TextUtils.isEmpty(confs)) {
            try {
                JSONObject json = new JSONObject(confs);
                if (json.has("meta")) {
                    JSONArray meta = json.getJSONArray("meta");
                    for (int i = 0; i < meta.length(); i++) {
                        JSONObject m = meta.getJSONObject(i);
                        if (m.getString("meta").equals("address")) {
                            metAddress.setText(m.getString("value"));
                            break;
                        }
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    @OnClick({R.id.btnHelp, R.id.btnSubmit}) public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnHelp:
                startActivity(new Intent(this, HelpActivity.class));
                break;
            case R.id.btnSubmit:
                submit();


                break;
        }
    }

    private void submit() {
        final Context c = this;
        if (TextUtils.isEmpty(metBank.getText().toString())) {
            metBank.setError("نام بانک را وارد کنید");
            return;
        }
        if (TextUtils.isEmpty(metReceiptNo.getText().toString())) {
            metReceiptNo.setError("شماره رسید را وارد کنید");
            return;
        }

        final SweetAlertDialog pDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
        pDialog.getProgressHelper().setBarColor(Utils.getColorFromResource(this, R.color.colorPrimary));
        pDialog.setTitleText("در حال ثبت آیتم");
        pDialog.setContentText("داریم آیتم رو تو سرور ثبت می کنیم");
        pDialog.setCancelable(false);
        pDialog.show();

        JSONObject data = new JSONObject();
/**
 *     $bankName      = $data['bank_name'];
 $receiptNumber = $data['receipt_number'];
 */

        try {
            data.put("bank_name", metBank.getText().toString());
            data.put("receipt_number", metReceiptNo.getText().toString());
            data.put("address", metAddress.getText().toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        final Handler h = new Handler(getMainLooper());
        ApiClient.makeRequest(this, "POST", "/receipt/" + p.getPaymentId(), data, new NetworkCallback() {
            @Override public void onSuccess(int status, JSONObject data) {
                h.post(() -> pDialog.setTitleText("ایول! رسید پرداخت ثبت ش")
                        .setContentText("بعد از تایید، باهات هماهنگ می کنیم و خریدت رو به دست ات می رسونیم. همین حالا توی اهدافِ محیط زیستی و انسان دوستانه ی کُمُدا شریک شدی. به خودت افتخار کن!")
                        .setConfirmText("باشه، مرسی")
                        .setConfirmClickListener(sweetAlertDialog -> {
                            sweetAlertDialog.dismiss();
                            finish();
                        })
                        .changeAlertType(SweetAlertDialog.SUCCESS_TYPE));
            }

            @Override public void onFailure(int status, final Error error) {
                h.post(() -> pDialog.setTitleText("اوه اوه ! مثل اینکه خطایی رخ داده")
                        .setContentText(error.getMessage())
                        .setConfirmText("یه بار دیگه")
                        .setCancelText("بیخیالش شو !")
                        .setConfirmClickListener(sweetAlertDialog -> {
                            sweetAlertDialog.dismiss();
                            submit();
                        })
                        .setCancelClickListener(Dialog::dismiss)
                        .changeAlertType(SweetAlertDialog.ERROR_TYPE));
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
