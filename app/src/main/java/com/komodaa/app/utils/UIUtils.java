package com.komodaa.app.utils;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.view.ViewCompat;
import android.text.Html;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import com.komodaa.app.App;
import com.komodaa.app.R;
import com.komodaa.app.activities.PurchasedProductsActivity;
import com.komodaa.app.interfaces.NetworkCallback;
import com.komodaa.app.interfaces.RatingCallback;
import com.komodaa.app.models.Error;
import com.komodaa.app.models.Product;
import com.rey.material.widget.Button;
import com.rey.material.widget.ProgressView;
import com.squareup.picasso.Picasso;

import net.darkion.AchievementUnlockedLib.AchievementUnlocked;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;

import cn.pedant.SweetAlert.SweetAlertDialog;
import me.zhanghai.android.materialratingbar.MaterialRatingBar;

/**
 * Created by nevercom on 10/14/17.
 */

public class UIUtils {

    public static void showSuccessMessage(Context c, String title, String subtitle) {
        new AchievementUnlocked(c)
                .setTitle(title)
                .setIcon(Utils.getDrawableFromRes(c, R.drawable.tick))
                .setTitleColor(Color.WHITE)
                .setSubtitleColor(Color.WHITE)
                .setTypeface(ResourcesCompat.getFont(c, R.font.iran_sans))
                .setSubTitle(subtitle)
                .setBackgroundColor(Utils.getColorFromResource(c, R.color.notification_ok_purple))
                .isLarge(false)
                .build()
                .show();
    }

    public static void showErrorMessage(Context c, String title, String subtitle) {
        new AchievementUnlocked(c)
                .setTitle(title)
                .setIcon(Utils.getDrawableFromRes(c, R.drawable.no))
                .setTitleColor(Color.WHITE)
                .setSubtitleColor(Color.WHITE)
                .setTypeface(ResourcesCompat.getFont(c, R.font.iran_sans))
                .setDuration(3500)
                .setSubTitle(subtitle)
                .setBackgroundColor(Utils.getColorFromResource(c, R.color.notification_error))
                .isLarge(false)
                .build()
                .show();
    }

    public static void showRatingDialog(Context c, Product p, RatingCallback callback) {
        int[] colors = {
                R.color.rate_0,
                R.color.rate_1,
                R.color.rate_2,
                R.color.rate_3,
                R.color.rate_4,
                R.color.rate_5,
        };
        final Dialog d = new Dialog(c, R.style.RateDialog);
        d.getWindow().setBackgroundDrawable(
                new ColorDrawable(
                        Color.TRANSPARENT));
        d.requestWindowFeature(Window.FEATURE_NO_TITLE);
        d.setContentView(R.layout.dialog_rate);

        MaterialRatingBar rQuality = d.findViewById(R.id.ratingBarQuality);
        MaterialRatingBar rPack = d.findViewById(R.id.ratingBarPack);
        MaterialRatingBar rStory = d.findViewById(R.id.ratingBarStory);

        TextView tvQuality = d.findViewById(R.id.tvRateQuality);
        TextView tvPack = d.findViewById(R.id.tvRatePack);
        TextView tvStory = d.findViewById(R.id.tvRateStory);
        TextView tvDesc = d.findViewById(R.id.tvItemDesc);
        TextView tvTitle = d.findViewById(R.id.tvTitle);

        ImageView imgClose = d.findViewById(R.id.imgClose);
        ImageView itemImage = d.findViewById(R.id.itemImage);

        Button btnRate = d.findViewById(R.id.btnRate);
        Button btnNotNow = d.findViewById(R.id.btnNotNow);

        tvDesc.setText(String.format("%s\n%s", Utils.formatNumber(p.getPrice()) + " تومان", Utils.getPersianDateText(p.getDate(), false)));
        tvTitle.setText(String.format(Locale.US, "امتیاز شما به %s برای خرید آیتم %d", p.getUser().getFirstName(), p.getId()));

        ProgressView progress = d.findViewById(R.id.progress);

        imgClose.setOnClickListener(v -> d.dismiss());
        btnNotNow.setOnClickListener(v -> {
            App.getPrefs().edit().putBoolean("no_rating_popup", true).apply();
            d.dismiss();
        });
        String imageUrl = p.getCover().getPath();
        if (!TextUtils.isEmpty(imageUrl)) {
            imageUrl = imageUrl.replace(".jpg", "_320.jpg");
            Picasso.with(c).load(imageUrl).fit().centerInside().placeholder(R.drawable.loading_main_activity).into(itemImage);
        }


        rQuality.setOnRatingChangeListener((ratingBar, rating) -> {
            int rt = (int) rating;
            tvQuality.setText(rt + "");
            ViewCompat.setBackgroundTintList(tvQuality, ContextCompat.getColorStateList(c, colors[rt]));
        });

        rPack.setOnRatingChangeListener((ratingBar, rating) -> {
            int rt = (int) rating;
            tvPack.setText(rt + "");
            ViewCompat.setBackgroundTintList(tvPack, ContextCompat.getColorStateList(c, colors[rt]));
        });

        rStory.setOnRatingChangeListener((ratingBar, rating) -> {
            int rt = (int) rating;
            tvStory.setText(rt + "");
            ViewCompat.setBackgroundTintList(tvStory, ContextCompat.getColorStateList(c, colors[rt]));
        });

        btnRate.setOnClickListener(v -> {

            int rq = (int) rQuality.getRating();
            int rp = (int) rPack.getRating();
            int rs = (int) rStory.getRating();

            if (rq < 1 || rp < 1 || rs < 1) {
                new AchievementUnlocked(c)
                        .setTitle("خطا !")
                        .setIcon(Utils.getDrawableFromRes(c, R.drawable.no))
                        .setTitleColor(Color.WHITE)
                        .setSubtitleColor(Color.WHITE)
                        .setTypeface(ResourcesCompat.getFont(c, R.font.iran_sans))
                        .setDuration(2500)
                        .setSubTitle("لطفاً به همه‌ی بخش ها امتیاز دهید")
                        .setBackgroundColor(Utils.getColorFromResource(c, R.color.notification_error))
                        .isLarge(false)
                        .build().show();

                return;
            }

            btnRate.setEnabled(false);
            progress.setVisibility(View.VISIBLE);
            JSONObject data = null;
            try {
                data = new JSONObject();

                data.put("rate_quality", rq);
                data.put("rate_pack", rp);
                data.put("rate_story", rs);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            ApiClient.makeRequest(c, "POST", "/rate/" + p.getId(), data, new NetworkCallback() {
                @Override public void onSuccess(int status, JSONObject data) {

                    btnRate.setEnabled(true);
                    progress.setVisibility(View.INVISIBLE);
                    if (callback != null) {
                        callback.hasRated(true);
                    }
                    d.dismiss();
                    final SweetAlertDialog pDialog = new SweetAlertDialog(c, SweetAlertDialog.SUCCESS_TYPE);
                    pDialog.setTitleText("");
                    pDialog.setContentText("مرسی که برای کیفیت کمدا وقت گذاشتی.");
                    pDialog.setConfirmText("باشه");
                    pDialog.setConfirmClickListener(SweetAlertDialog::dismissWithAnimation);
                    pDialog.show();
                }

                @Override public void onFailure(int status, Error error) {

                    btnRate.setEnabled(true);
                    progress.setVisibility(View.INVISIBLE);


                    if (callback != null) {
                        callback.hasRated(false);
                    }
                }
            });
        });
        d.show();
    }

    public static void showRatingDialog(Context c, Product p) {
        showRatingDialog(c, p, null);
    }

    public static void showRatingRequestDialog(Context c, String title, String text) {

        final Dialog d = new Dialog(c, R.style.RateDialog);
        d.getWindow().setBackgroundDrawable(
                new ColorDrawable(
                        Color.TRANSPARENT));
        d.requestWindowFeature(Window.FEATURE_NO_TITLE);
        d.setContentView(R.layout.dialog_rate_info);


        TextView tvTitle = d.findViewById(R.id.tvTitle);
        TextView tvContent = d.findViewById(R.id.tvCaption);

        ImageView imgClose = d.findViewById(R.id.imgClose);

        Button btnRate = d.findViewById(R.id.btnRate);
        Button btnNotNow = d.findViewById(R.id.btnNotNow);

        tvTitle.setText(title);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            tvContent.setText(Html.fromHtml(text, Html.FROM_HTML_MODE_LEGACY));
        } else {
            tvContent.setText(Html.fromHtml(text));
        }
        imgClose.setOnClickListener(v -> d.dismiss());
        btnNotNow.setOnClickListener(v -> {
            App.getPrefs().edit().putBoolean("no_rating_popup", true).apply();
            d.dismiss();
        });
        btnRate.setOnClickListener(v -> {
            c.startActivity(new Intent(c, PurchasedProductsActivity.class));
            d.dismiss();
        });

        d.show();
    }

}
