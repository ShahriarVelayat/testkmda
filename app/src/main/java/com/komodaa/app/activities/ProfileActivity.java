package com.komodaa.app.activities;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.ColorRes;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.komodaa.app.App;
import com.komodaa.app.R;
import com.komodaa.app.interfaces.NetworkCallback;
import com.komodaa.app.models.Error;
import com.komodaa.app.models.User;
import com.komodaa.app.utils.ApiClient;
import com.komodaa.app.utils.ImageUtils;
import com.komodaa.app.utils.JWTUtils;
import com.komodaa.app.utils.UserUtils;
import com.komodaa.app.utils.Utils;
import com.pkmmte.view.CircularImageView;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.rey.material.widget.Button;
import com.soundcloud.android.crop.Crop;
import com.squareup.picasso.Picasso;
import com.toptoche.searchablespinnerlibrary.SearchableSpinner;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.pedant.SweetAlert.SweetAlertDialog;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ProfileActivity extends AppCompatActivity {

    private static final int CAMERA_REQUEST = 12;
    @BindView(R.id.imgAvatar) CircularImageView imgAvatar;
    @BindView(R.id.metFirstName) MaterialEditText metFirstName;
    @BindView(R.id.metLastName) MaterialEditText metLastName;
    @BindView(R.id.metEmail) MaterialEditText metEmail;
    @BindView(R.id.metPhoneNumber) MaterialEditText metPhoneNumber;
    @BindView(R.id.mspCity) SearchableSpinner mspCity;
    @BindView(R.id.btnUpdate) Button btnUpdate;
    @BindView(R.id.metBank) MaterialEditText metBank;
    @BindView(R.id.metShaba) MaterialEditText metShaba;
    @BindView(R.id.metAddress) MaterialEditText metAddress;

    private Bitmap avatarBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        ButterKnife.bind(this);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(Utils.generateTypeFacedString(this, getTitle().toString()));

        User user = UserUtils.getUser();
        metFirstName.setText(user.getFirstName());
        metLastName.setText(user.getLastName());
        metEmail.setText(user.getEmail());
        //metEmail.setEnabled(false);
        metPhoneNumber.setText(user.getPhoneNumber());
        metPhoneNumber.setEnabled(false);
        mspCity.setSelection(user.getCityId());
        if (!TextUtils.isEmpty(user.getAvatarUrl())) {
            Picasso.with(this)
                    .load(user.getAvatarUrl())
                    .placeholder(R.drawable.ic_account_circle_48px)
                    .into(imgAvatar);
        }
        mspCity.setTitle("شهر محل سکونت خود را انتخاب کنید:");
        mspCity.setPositiveButton("باشه");

        String confs = App.getPrefs().getString("user_meta", null);
        if (!TextUtils.isEmpty(confs)) {
            try {
                JSONObject j = new JSONObject(confs);
                // Set address field based on user meta
                if (j.has("meta")) {
                    JSONArray meta = j.getJSONArray("meta");
                    for (int i = 0; i < meta.length(); i++) {
                        JSONObject m = meta.getJSONObject(i);
                        if (m.getString("meta").equals("bank.shaba")) {
                            String shaba = m.getString("value");
                            if (shaba.toLowerCase().startsWith("ir")) {
                                shaba = shaba.substring(2);
                            }
                            metShaba.setText(shaba);
                        } else if (m.getString("meta").equals("bank.name")) {
                            metBank.setText(m.getString("value"));

                        } else if (m.getString("meta").equals("address")) {
                            metAddress.setText(m.getString("value"));

                        }
                    }
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    @OnClick({R.id.imgAvatar, R.id.btnUpdate, R.id.tvShebaLink}) public void onClick(View view) {
        switch (view.getId()) {
            case R.id.imgAvatar:
                displayPicker();
                break;
            case R.id.btnUpdate:
                if (!UserUtils.isLoggedIn()) {
                    Utils.displayLoginErrorDialog(this);
                    return;
                }
                String firstName = metFirstName.getText().toString();
                String lastName = metLastName.getText().toString();
                if (TextUtils.isEmpty(firstName)) {
                    metFirstName.setError("نام باید وارد شود");
                    return;
                }
                if (TextUtils.isEmpty(lastName)) {
                    metLastName.setError("نام خانوادگی باید وارد شود");
                    return;
                }
                String shaba = metShaba.getText().toString();
                if (!shaba.isEmpty() && !Utils.isIBANValid("ir" + shaba)) {
                    metShaba.setError("شبا معتبر نیست");
                    return;
                }
                uploadData();
                break;
            case R.id.tvShebaLink:
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://komodaa.com/sheba")));
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

    private int getColorFromResource(@ColorRes int color) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return getColor(color);
        }
        return getResources().getColor(R.color.colorPrimary);
    }

    void pickCamera() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(getTempFile(CAMERA_REQUEST, imgAvatar)));
        startActivityForResult(cameraIntent, CAMERA_REQUEST);
    }

    private void uploadData() {

        final SweetAlertDialog pDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
        pDialog.getProgressHelper().setBarColor(getColorFromResource(R.color.colorPrimary));
        pDialog.setTitleText("در حال آپدیت پروفایل");
        pDialog.setContentText("اطلاعات پروفایل شما داره آپدیت میشه...");
        pDialog.setCancelable(false);
        pDialog.show();


        MultipartBody.Builder builder = new MultipartBody.Builder();
        builder.setType(MultipartBody.FORM);
        builder.addFormDataPart("city_id", String.valueOf(mspCity.getSelectedItemPosition()));
        builder.addFormDataPart("first_name", metFirstName.getText().toString());
        builder.addFormDataPart("last_name", metLastName.getText().toString());
        builder.addFormDataPart("shaba", metShaba.getText().toString());
        builder.addFormDataPart("email", metEmail.getText().toString());
        builder.addFormDataPart("bank_name", metBank.getText().toString());
        builder.addFormDataPart("address", metAddress.getText().toString());

        if (avatarBitmap != null) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            avatarBitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);

            builder.addFormDataPart("avatar", "avatar.jpg", RequestBody.create(MediaType.parse("image/jpeg"), baos.toByteArray()));
        }


        final RequestBody requestBody = builder.build();

        Request request = new Request.Builder()
                .url(ApiClient.API_BASE_ADDRESS + "/profile")
                .post(requestBody)
                .header("X-Authentication-Token", App.getPrefs().getString(UserUtils.TOKEN, ""))
                .build();
        OkHttpClient client = new OkHttpClient.Builder()

                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) {
                Log.d("Failed", "onFailure: " + e.getMessage());
                Handler h = new Handler(getMainLooper());
                h.post(() -> pDialog.setContentText("مثل اینکه یه مشکلی پیش اومده")
                        .setConfirmText("یه بار دیگه")
                        .setCancelText("بیخیالش شو !")
                        .setConfirmClickListener(sweetAlertDialog -> {
                            sweetAlertDialog.dismiss();
                            uploadData();
                        })
                        .setCancelClickListener(Dialog::dismiss)
                        .changeAlertType(SweetAlertDialog.ERROR_TYPE));
            }

            @Override public void onResponse(Call call, Response response) throws IOException {
                String responseStr = response.body().string();
                Log.d("Komodaa", "onResponse: " + responseStr);
                if (!response.isSuccessful()) {
                    onFailure(call, new IOException("Error"));
                    return;
                }
                try {
                    JSONObject json = new JSONObject(responseStr);
                    if (json.has("token")) {

                        String token = json.getString("token");
                        try {
                            User newUser = new User(JWTUtils.decode(token));
                            User currentUser = UserUtils.getUser();
                            if (currentUser.getId() != newUser.getId()) {
                                Log.d("ApiClientDebug", String.format(Locale.US, "Token Mismatch, old User ID: %d, New User ID: %d", currentUser.getId(), newUser.getId()));
                                JSONObject logData = new JSONObject();
                                logData.put("oldUserToken", App.getPrefs().getString(UserUtils.TOKEN, null));
                                logData.put("newUserToken", token);
                                logData.put("oldUserId", currentUser.getId());
                                logData.put("newUserId", newUser.getId());
                                logData.put("route", "/profile");
                                logData.put("responseString", responseStr);


                                ApiClient.makeRequest(ProfileActivity.this, "POST", "/app_log", logData, new NetworkCallback() {
                                    @Override
                                    public void onSuccess(int status, JSONObject data) {

                                    }

                                    @Override public void onFailure(int status, Error error) {

                                    }
                                });
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }


                        App.getPrefs().edit().putString(UserUtils.TOKEN, json.getString("token")).apply();
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Handler h = new Handler(getMainLooper());
                h.post(() -> pDialog.setContentText("اطلاعات پروفایل شما آپدیت شد.")
                        .setConfirmText("باشه، مرسی")
                        .setConfirmClickListener(Dialog::dismiss)
                        .changeAlertType(SweetAlertDialog.SUCCESS_TYPE));

            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent result) {
        Log.d("", "onActivityResult() called with: requestCode = [" + requestCode + "], resultCode = [" + resultCode + "], result = [" + result + "]");
        if (resultCode != RESULT_OK) {
            return;
        }
        if (requestCode == Crop.REQUEST_PICK) {
            beginCrop(result.getData());
        } else if (requestCode == Crop.REQUEST_CROP) {
            handleCrop(resultCode, result);
        } else if (requestCode == CAMERA_REQUEST) {

            beginCrop(Uri.fromFile(getTempFile(requestCode, imgAvatar)));

        }
    }

    private void beginCrop(Uri source) {
        Uri destination = Uri.fromFile(getTempFile(Crop.REQUEST_CROP, imgAvatar));
        String sourcePath = Utils.getPath(this, source);
        Crop of = Crop.of(Uri.fromFile(new File(sourcePath)), destination);

        of.asSquare();

        of.start(this);
    }

    private void handleCrop(int resultCode, Intent result) {
        if (resultCode == RESULT_OK) {
            Uri uri = Crop.getOutput(result);
            ImageUtils.normalizeImageForUri(this, uri);
            imgAvatar.setScaleType(ImageView.ScaleType.FIT_CENTER);
            avatarBitmap = Utils.decodeSampledBitmapFromUri(this, uri, imgAvatar.getWidth(), imgAvatar.getHeight());
            imgAvatar.setImageBitmap(avatarBitmap);

        } else if (resultCode == Crop.RESULT_ERROR) {
            Toast.makeText(this, Crop.getError(result).getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    File getTempFile(int requestCode, ImageView view) {
        File f = new File(getExternalCacheDir(), String.format(Locale.US, "_%d_%d.jpg", requestCode, view.getId()));
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
