package com.komodaa.app.activities;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;
import com.komodaa.app.App;
import com.komodaa.app.R;
import com.komodaa.app.models.Attribute;
import com.komodaa.app.models.Category;
import com.komodaa.app.utils.ApiClient;
import com.komodaa.app.utils.Blur;
import com.komodaa.app.utils.NumberTextWatcherForThousand;
import com.komodaa.app.utils.UserUtils;
import com.komodaa.app.utils.Utils;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.rey.material.widget.Button;
import com.toptoche.searchablespinnerlibrary.SearchableSpinner;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.pedant.SweetAlert.SweetAlertDialog;
import okhttp3.CacheControl;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class AddProductStep2Activity extends AppCompatActivity {

    private static final String TAG = "Komodaa";
    @BindView(R.id.imgMainPhoto) ImageView imgMainPhoto;
    @BindView(R.id.spCategory) Spinner spCategory;
    @BindView(R.id.spCity) SearchableSpinner spCity;
    @BindView(R.id.metPrice) MaterialEditText metPrice;
    @BindView(R.id.llAttribsContainer) LinearLayout llAttribsContainer;
    @BindView(R.id.metDescription) MaterialEditText metDescription;
    @BindView(R.id.metAddress) MaterialEditText metAddress;
    @BindView(R.id.btnNext) Button btnNext;
    List<Category> categories = new ArrayList<>();
    List<MaterialEditText> views = new ArrayList<>();
    ArrayList<Uri> uris;
    int count;
    @BindView(R.id.scrollView) ScrollView scrollView;
    @BindView(R.id.metOriginalPrice) MaterialEditText metOriginalPrice;
    private boolean isNew;
    private Context c;
    private boolean isHomemade;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_product_step2);
        ButterKnife.bind(this);

        getSupportActionBar().setTitle(Utils.generateTypeFacedString(this, getTitle().toString()));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        c = this;
        final Intent intent = getIntent();
        if (intent == null || !intent.hasExtra("count") || intent.getIntExtra("count", 0) < 1) {
            finish();
        }
        count = intent.getIntExtra("count", 0);
        uris = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            uris.add(intent.getParcelableExtra("uri" + i));
        }
        isNew = intent.getBooleanExtra("status", false);
        isHomemade = intent.getBooleanExtra("is_homemade", false);
        metOriginalPrice.setVisibility(isHomemade ? View.GONE : View.VISIBLE);
        Log.d(TAG, "isNew: " + String.valueOf(isNew));
        getWindow().getDecorView().getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    getWindow().getDecorView().getViewTreeObserver().removeOnGlobalLayoutListener(this);
                } else {
                    getWindow().getDecorView().getViewTreeObserver().removeGlobalOnLayoutListener(this);
                }
                Bitmap bitmap = Utils.decodeSampledBitmapFromUri(
                        AddProductStep2Activity.this,
                        uris.get(0),
                        imgMainPhoto.getWidth(),
                        imgMainPhoto.getHeight()
                );
                imgMainPhoto.setImageBitmap(bitmap);
                BitmapDrawable background = new BitmapDrawable(getResources(), Blur.fastBlur(c, Utils.scaleImage(c, bitmap, 50), 25));

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    imgMainPhoto.setBackground(background);
                } else {
                    imgMainPhoto.setBackgroundDrawable(background);
                }
                imgMainPhoto.getBackground().setColorFilter(0x7f000000, PorterDuff.Mode.DARKEN);
            }
        });

        SharedPreferences prefs = App.getPrefs();
        String json = prefs.getString("config", null);
        Log.d(TAG, "onCreate: " + json);
        if (json != null) {
            try {
                JSONArray cats = new JSONObject(json).getJSONArray("cats");

                for (int i = 0; i < cats.length(); i++) {
                    JSONObject catItem = cats.getJSONObject(i);

                    Category c = new Category();
                    c.setId(catItem.getInt("id"));
                    c.setName(catItem.getString("name"));
                    JSONArray atts = catItem.getJSONArray("attribs");
                    for (int j = 0; j < atts.length(); j++) {
                        c.addAttribute(new Attribute(atts.getJSONObject(j)));
                    }
                    categories.add(c);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        final CategoriesAdapter adapter = new CategoriesAdapter(categories);
        String confs = prefs.getString("user_meta", null);
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
        } catch (JSONException e) {
            e.printStackTrace();
        }
        spCategory.setAdapter(adapter);

        spCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Category c = adapter.getItem(position);
                List<Attribute> attribs = c.getAttributes();
                updateAttributesList(attribs);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        spCity.setTitle("شهر محل سکونت خود را انتخاب کنید:");
        spCity.setPositiveButton("باشه");
        //final int cityId = UserUtils.getUser().getCityId();
        //if (cityId >= 0) {
        spCity.postDelayed(() -> {
            // Default City, Shiraz, before it was `cityId` which is user's selected city
            spCity.setSelection(737);

        }, 500);
        spCity.setEnabled(true);
        //}
        metOriginalPrice.addTextChangedListener(new NumberTextWatcherForThousand(metOriginalPrice));
        metPrice.addTextChangedListener(new NumberTextWatcherForThousand(metPrice));
        metDescription.setOnTouchListener((v, event) -> {
            v.getParent().requestDisallowInterceptTouchEvent(true);
            return false;
        });
    }

    private int getIndex(Spinner spinner, String myString) {

        for (int i = 0; i < spinner.getCount(); i++) {
            if (spinner.getItemAtPosition(i).toString().equalsIgnoreCase(myString)) {
                return i;
            }
        }
        return 0;
    }

    @OnClick(R.id.btnNext)
    public void onClick() {

        if (!UserUtils.isLoggedIn()) {
            Utils.displayLoginErrorDialog(this);
            return;
        }
        if (spCity.getSelectedItemPosition() == SearchableSpinner.NO_ITEM_SELECTED) {
            Toast.makeText(this, "شهر را انتخاب کنید", Toast.LENGTH_SHORT).show();
            return;
        }
        String price = NumberTextWatcherForThousand.trimCommaOfString(metPrice.getText().toString());
        Log.d(TAG, "price: " + price);
        if (TextUtils.isEmpty(price) || Integer.parseInt(price) < 1000) {
            metPrice.setError("مبلغ باید عددی بالاتر از ۱۰۰۰ تومان باشد");
            return;
        }
        for (final MaterialEditText e : views) {
            if (TextUtils.isEmpty(e.getText())) {
                e.setError("مقداری برای این قسمت وارد کنید");
                scrollView.post(() -> {
                    scrollView.scrollTo(0, e.getBottom() + e.getHeight() + Utils.dpToPx(AddProductStep2Activity.this, 16));
                    e.requestFocus();
                });
                return;
            }
        }
        if (TextUtils.isEmpty(metDescription.getText().toString())) {
            metDescription.setError("توضیحات محصول را وارد کنید");
            return;
        }
        if (TextUtils.isEmpty(metAddress.getText().toString())) {
            metAddress.setError("آدرس رو وارد کن");
            return;
        }
        uploadData();

    }


    private void uploadData() {

        final SweetAlertDialog pDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
        pDialog.getProgressHelper().setBarColor(Utils.getColorFromResource(this, R.color.colorPrimary));
        pDialog.setTitleText("در حال ثبت ایتم");
        pDialog.setContentText("داریم آیتم رو تو سرور ثبت می کنیم");
        pDialog.setCancelable(false);
        pDialog.show();
        try {
            JSONArray attribs = new JSONArray();
            for (MaterialEditText e : views) {
                JSONObject obj = new JSONObject();
                obj.put(e.getId() + "", e.getText().toString());
                attribs.put(obj);
            }


            MultipartBody.Builder builder = new MultipartBody.Builder();
            builder.setType(MultipartBody.FORM);
            builder.addFormDataPart("attribs", attribs.toString());
            builder.addFormDataPart("city_id", String.valueOf(spCity.getSelectedItemPosition()));
            builder.addFormDataPart("category_id", String.valueOf(spCategory.getSelectedItemId()));
            builder.addFormDataPart("original_price", NumberTextWatcherForThousand.trimCommaOfString(metOriginalPrice.getText().toString()));
            builder.addFormDataPart("price", NumberTextWatcherForThousand.trimCommaOfString(metPrice.getText().toString()));
            builder.addFormDataPart("description", metDescription.getText().toString());
            builder.addFormDataPart("address", metAddress.getText().toString());
            builder.addFormDataPart("is_new", String.valueOf(isNew));
            builder.addFormDataPart("is_homemade", String.valueOf(isHomemade));
            for (int i = 0; i < uris.size(); i++) {
                Bitmap b = Utils.decodeSampledBitmapFromUri(this, uris.get(i), 1440, 1440);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                b.compress(Bitmap.CompressFormat.JPEG, 80, baos);


                String key = i == 0 ? "cover" : "file" + i;
                builder.addFormDataPart(key, "image" + i + ".jpg", RequestBody.create(MediaType.parse("image/jpeg"), baos.toByteArray()));

            }
            RequestBody requestBody = builder.build();

            Request request = new Request.Builder()
                    .url(ApiClient.API_BASE_ADDRESS + "/product")
                    .post(requestBody)
                    .cacheControl(CacheControl.FORCE_NETWORK)
                    .header("X-Authentication-Token", App.getPrefs().getString(UserUtils.TOKEN, ""))
                    .build();
            OkHttpClient client = new OkHttpClient.Builder()

                    .build();
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, final IOException e) {

                    //Log.d("Failed", "onFailure: " + e.getMessage());
                    Handler h = new Handler(getMainLooper());
                    h.post(() -> {
                        boolean isLimitReached = false;
                        try {
                            isLimitReached = Integer.parseInt(e != null && e.getCause() != null ? e.getCause().getMessage() : "-1") == -24;
                        } catch (NumberFormatException e1) {
                            e1.printStackTrace();
                        }
                        final boolean finalIsLimitReached = isLimitReached;
                        pDialog.setContentText(e != null ? e.getMessage() : "")
                                .setConfirmText(isLimitReached ? "آها فهمیدم" : "یه بار دیگه")
                                .setCancelText("بیخیالش شو !")
                                .setConfirmClickListener(sweetAlertDialog -> {
                                    sweetAlertDialog.dismiss();
                                    if (finalIsLimitReached) {
                                        startActivity(new Intent(c, HomeActivity.class));
                                        finish();
                                    } else {
                                        uploadData();
                                    }
                                })
                                .setCancelClickListener(Dialog::dismiss)
                                .changeAlertType(SweetAlertDialog.ERROR_TYPE);
                    });
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String responseStr = response.body().string();
                    Log.d("Komodaa", "onResponse: " + responseStr);
                    try {
                        JSONObject json = new JSONObject(responseStr);
                        if (!response.isSuccessful()) {
                            String errorMessage = json.has("errorMessage")
                                    ? json.getString("errorMessage")
                                    : "مثل اینکه خطایی رخ داده";
                            onFailure(call, new IOException(errorMessage, new Throwable((json.has("errorCode") ? json.getInt("errorCode") : -1) + "")));
                            return;
                        }
                    } catch (JSONException e) {
                        onFailure(call, new IOException("مثل اینکه خطایی رخ داده", new Throwable("-1")));
                        return;
                    }

                    Handler h = new Handler(getMainLooper());
                    h.post(() -> {
                        pDialog.setContentText("تبریک! آیتم با موفقیت ذخیره شد.\n" +
                                "واستا بررسی کنیم، بعد آیتم ات رو نمایش میدیم!")
                                .setConfirmText("باشه، مرسی")
                                .setConfirmClickListener(sweetAlertDialog -> {
                                    sweetAlertDialog.dismiss();
                                    App.getPrefs().edit().putBoolean("has_item", true).apply();
                                    startActivity(new Intent(c, HomeActivity.class));
                                    finish();
                                })
                                .changeAlertType(SweetAlertDialog.SUCCESS_TYPE);

                        try {
                            String vUserName = UserUtils.getUser().getFirstName() + " " + UserUtils.getUser().getLastName();
                            if (TextUtils.isEmpty(vUserName.trim())) {
                                vUserName = "نا شناس";
                            }
                            Answers.getInstance().logCustom(new CustomEvent("Add Item")
                                    .putCustomAttribute("Category", Utils.getCategoryName(spCategory.getSelectedItemPosition()))
                                    .putCustomAttribute("City", Utils.getCityName(AddProductStep2Activity.this, spCity.getSelectedItemPosition()))
                                    .putCustomAttribute("Owner", vUserName));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    class CategoriesAdapter extends BaseAdapter {
        private List<Category> list;

        CategoriesAdapter(List<Category> list) {
            this.list = list;
        }

        @Override
        public int getCount() {
            return list == null ? 0 : list.size();
        }

        @Override
        public Category getItem(int position) {
            return list.get(position);
        }

        @Override
        public long getItemId(int position) {
            return list.get(position).getId();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(android.R.layout.simple_spinner_item, parent, false);
                holder = new ViewHolder();

                holder.text = convertView.findViewById(android.R.id.text1);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            holder.text.setText(list.get(position).getName());
            return convertView;
        }

        class ViewHolder {
            TextView text;
        }
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

    MaterialEditText createView(Attribute a) {
        MaterialEditText m = (MaterialEditText) getLayoutInflater().inflate(R.layout.template_edit_text, llAttribsContainer, false);
        m.setHint(a.getName());
        m.setId(a.getId());
        int inputType = InputType.TYPE_CLASS_NUMBER;
        if (!TextUtils.isEmpty(a.getInputType())) {
            Log.d(TAG, "inputType: " + a.getInputType());
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
            }

            //Log.d(TAG, "inputType: " + inputType);
        }
        //m.setInputType(inputType);
        //m.setFocusable(true);
        //m.setFocusableInTouchMode(true);
//        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
//        m.setLayoutParams(params);
        //m.setEnabled(true);
        m.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(v, InputMethodManager.SHOW_IMPLICIT);
            }
        });
        views.add(m);
        return m;
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
