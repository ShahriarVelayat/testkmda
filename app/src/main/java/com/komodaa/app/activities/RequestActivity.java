package com.komodaa.app.activities;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import com.komodaa.app.R;
import com.komodaa.app.interfaces.NetworkCallback;
import com.komodaa.app.models.Category;
import com.komodaa.app.models.Error;
import com.komodaa.app.utils.ApiClient;
import com.komodaa.app.utils.JDF;
import com.komodaa.app.utils.UserUtils;
import com.komodaa.app.utils.Utils;
import com.mohamadamin.persianmaterialdatetimepicker.date.DatePickerDialog;
import com.mohamadamin.persianmaterialdatetimepicker.utils.PersianCalendar;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.rey.material.widget.Button;
import com.toptoche.searchablespinnerlibrary.SearchableSpinner;

import net.darkion.AchievementUnlockedLib.AchievementUnlocked;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.pedant.SweetAlert.SweetAlertDialog;

public class RequestActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener {

    @BindView(R.id.spCategory) Spinner spCategory;
    @BindView(R.id.mspCity) SearchableSpinner mspCity;
    @BindView(R.id.metTime) MaterialEditText metTime;
    @BindView(R.id.imgDatePicker) ImageView imgDatePicker;
    @BindView(R.id.metDescription) MaterialEditText metDescription;
    @BindView(R.id.btnSubmit) Button btnSubmit;
    private CategoriesAdapter catAdapter;
    private String gregorianDate;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request);
        ButterKnife.bind(this);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(Utils.generateTypeFacedString(this, getTitle().toString()));

        List<Category> categories = Utils.getCategories();
        catAdapter = new CategoriesAdapter(categories);

        spCategory.setAdapter(catAdapter);

        mspCity.setTitle("شهر محل سکونت خود را انتخاب کنید:");
        mspCity.setPositiveButton("باشه");

        //mspCity.setSelection(UserUtils.getUser().getCityId());
        mspCity.postDelayed(() -> {
            // Default City, Shiraz, before it was `cityId` which is user's selected city
            mspCity.setSelection(737);

        }, 500);
        mspCity.setEnabled(false);
    }


    @OnClick({R.id.imgDatePicker, R.id.btnSubmit}) public void onClick(View view) {
        switch (view.getId()) {
            case R.id.imgDatePicker:
                PersianCalendar persianCalendar = new PersianCalendar();
                DatePickerDialog datePickerDialog = DatePickerDialog.newInstance(
                        this,
                        persianCalendar.getPersianYear(),
                        persianCalendar.getPersianMonth(),
                        persianCalendar.getPersianDay()
                );
                datePickerDialog.setMinDate(persianCalendar);
                datePickerDialog.show(getFragmentManager(), "DatePickerDialog");
                break;
            case R.id.btnSubmit:

                submitRequest();
                break;
        }
    }

    public void submitRequest() {
        if (spCategory.getSelectedItemPosition() == SpinnerAdapter.NO_SELECTION) {
            new AchievementUnlocked(this)
                    .setTitle("خطا !")
                    .setIcon(Utils.getDrawableFromRes(this, R.drawable.no))
                    .setTitleColor(Color.WHITE)
                    .setSubtitleColor(Color.WHITE)
                    .setTypeface(ResourcesCompat.getFont(this, R.font.iran_sans))
                    .setDuration(1500)
                    .setSubTitle("لطفاً نوع آیتم را انتخاب کنید")
                    .setBackgroundColor(Utils.getColorFromResource(this, R.color.notification_error))
                    .isLarge(false)
                    .build().show();
            return;
        }
        if (mspCity.getSelectedItemPosition() == SpinnerAdapter.NO_SELECTION) {
            new AchievementUnlocked(this)
                    .setTitle("خطا !")
                    .setIcon(Utils.getDrawableFromRes(this, R.drawable.no))
                    .setTitleColor(Color.WHITE)
                    .setSubtitleColor(Color.WHITE)
                    .setTypeface(ResourcesCompat.getFont(this, R.font.iran_sans))
                    .setDuration(1500)
                    .setSubTitle("لطفاً شهر خود را انتخاب کنید")
                    .setBackgroundColor(Utils.getColorFromResource(this, R.color.notification_error))
                    .isLarge(false)
                    .build().show();
            return;
        }

        if (TextUtils.isEmpty(metDescription.getText().toString())) {
            metDescription.setError("برای درخواستت توضیح مناسب بنویس");
            return;
        }
        final int cityId = mspCity.getSelectedItemPosition();

        Object selectedCity = mspCity.getSelectedItem();


        final String city = selectedCity.toString();


        final Handler h = new Handler(getMainLooper());
        final Context c = this;

        if (!UserUtils.isLoggedIn()) {
            Utils.displayLoginErrorDialog(c);
            return;
        }
        final SweetAlertDialog pDialog = new SweetAlertDialog(c, SweetAlertDialog.CUSTOM_IMAGE_TYPE);
        pDialog.setTitleText("ثبت درخواست");
        pDialog.setContentText("همین رو ثبت کنم دیگه؟");
        pDialog.setConfirmText("آره ثبت شه");
        pDialog.setCancelText(" نه، ولش کن!");
        pDialog.setConfirmClickListener(sweetAlertDialog -> {
            pDialog.setTitleText("در حال ثبت درخواست")
                    .setContentText("داریم درخواست رو ثبت می کنیم")
                    .showCancelButton(false)
                    .changeAlertType(SweetAlertDialog.PROGRESS_TYPE);
            JSONObject data = new JSONObject();
            try {
                data.put("category_id", spCategory.getSelectedItemId());
                data.put("city_id", cityId);
                data.put("city_name", city);
                data.put("due_date", metTime.getText().toString());
                data.put("due_date_gregorian", gregorianDate);
                data.put("description", metDescription.getText().toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }

            ApiClient.makeRequest(c, "POST", "/request", data, new NetworkCallback() {
                @Override public void onSuccess(int status, JSONObject data) {
                    h.post(() -> pDialog.setTitleText("درخواست ثبت شد !")
                            .setContentText("واستا بررسی کنیم، بعد آیتم ات رو نمایش میدیم!\nاگه هر کدوم از کُمُدی ها، چیزی که می خوای رو داشته باشن بهت خبر میدیم!")
                            .setConfirmText("باشه، ممنونم")
                            .setConfirmClickListener(sweetAlertDialog12 -> {
                                pDialog.dismiss();
                                finish();
                            })
                            .showCancelButton(false)
                            .changeAlertType(SweetAlertDialog.SUCCESS_TYPE));

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
    }

    @Override
    public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
        PersianCalendar p = new PersianCalendar();
        p.setPersianDate(year, monthOfYear, dayOfMonth);
        //PersianCalendarUtils.persianToJulian(year,monthOfYear,dayOfMonth);
        JDF jdf = new JDF();
        jdf.setIranianDate(year, monthOfYear + 1, dayOfMonth);
        gregorianDate = jdf.getGregorianYear() + "-" + jdf.getGregorianMonth() + "-" + jdf.getGregorianDay();
        metTime.setText(p.getPersianLongDate());
    }


    class CategoriesAdapter extends BaseAdapter {
        private List<Category> list;

        CategoriesAdapter(List<Category> list) {
            this.list = list;
        }

        @Override public int getCount() {
            return list == null ? 0 : list.size();
        }

        @Override public Category getItem(int position) {
            return list.get(position);
        }

        @Override public long getItemId(int position) {
            return list.get(position).getId();
        }

        @Override public View getView(int position, View convertView, ViewGroup parent) {
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

}
