package com.komodaa.app.fragments;

import android.annotation.TargetApi;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.komodaa.app.R;
import com.komodaa.app.activities.ProductDetailsActivity;
import com.komodaa.app.activities.SubmitPaymentReceipt;
import com.komodaa.app.interfaces.NetworkCallback;
import com.komodaa.app.interfaces.OnItemClickListener;
import com.komodaa.app.models.Error;
import com.komodaa.app.models.Product;
import com.komodaa.app.utils.ApiClient;
import com.komodaa.app.utils.Blur;
import com.komodaa.app.utils.UIUtils;
import com.komodaa.app.utils.Utils;
import com.rey.material.widget.Button;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.yqritc.recyclerviewflexibledivider.HorizontalDividerItemDecoration;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import cn.pedant.SweetAlert.SweetAlertDialog;

/**
 * Created by nevercom on 11/7/16.
 */

public class MyPurchaseHistoryFragment extends BaseFragment {
    @BindView(R.id.recList) RecyclerView recList;
    private Unbinder unbinder;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recycler_view, container, false);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        final LinearLayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        recList.setLayoutManager(mLayoutManager);
        recList.setHasFixedSize(true);
        recList.addItemDecoration(new HorizontalDividerItemDecoration.Builder(getActivity()).size(Utils.dpToPx(getActivity(), 8)).color(Color.TRANSPARENT).build());
        refresh();
    }

    private void refresh() {
        final ProgressDialog pd = new ProgressDialog(getActivity());
        pd.setMessage(getString(R.string.loadin_please_wait));
        pd.setIndeterminate(true);
        pd.show();
        ApiClient.makeRequest(getActivity(), "GET", "/purchased_items", null, new NetworkCallback() {
            @Override
            public void onSuccess(int status, JSONObject data) {
                try {
                    if (recList == null || !isAdded()) {
                        return;
                    }
                    pd.dismiss();
                    List<Product> list = new ArrayList<>();

                    JSONArray products = data.getJSONArray("data");
                    for (int i = 0; i < products.length(); i++) {
                        list.add(new Product(products.getJSONObject(i)));
                    }
                    PurchaseAdapter adapter = new PurchaseAdapter(list);
                    adapter.setOnItemClickListener((view, position, data1) -> {
                        switch (view.getId()) {
                            case R.id.btnPay:
                                startActivity(new Intent(getActivity(), SubmitPaymentReceipt.class).putExtra("item", (Product) data1));
                                break;
                            case R.id.btnCancel:
                                cancelPayment((Product) data1);
                                break;
                            case R.id.btnRate:
                                UIUtils.showRatingDialog(getActivity(), (Product) data1, rated -> refresh());
                                break;
                            case R.id.imgProduct:
                                Intent i = new Intent(getActivity(), ProductDetailsActivity.class);
                                i.putExtra("item", (Product) data1);
                                startActivity(i);
                                break;
                        }
                    });
                    recList.setAdapter(adapter);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int status, Error error) {
                pd.dismiss();
            }
        });
    }

    private void cancelPayment(final Product p) {
        Log.d("", "cancelPayment() called with: p = [" + p + "]");
        final Context c = getActivity();
        final Handler h = new Handler(c.getMainLooper());
        final SweetAlertDialog pDialog = new SweetAlertDialog(c, SweetAlertDialog.WARNING_TYPE);
        pDialog.setTitleText("لغو خرید");
        pDialog.setContentText("درحال صرفنظر کردن از خرید این آیتم هستید\n آیا از لغو درخواست خرید برای این آیتم اطمینان دارید ؟");
        pDialog.setConfirmText("لغو خرید");
        pDialog.setCancelText("نه، می خوامش");
        pDialog.setConfirmClickListener(sweetAlertDialog -> {
            pDialog.setTitleText("در حال ثبت درخواست")
                    .setContentText("درحال حذف آیتم از لیست آیتم های درخواستی شما")
                    .showCancelButton(false)
                    .changeAlertType(SweetAlertDialog.PROGRESS_TYPE);

            ApiClient.makeRequest(c, "POST", "/payment/cancel/" + p.getPaymentId(), null, new NetworkCallback() {
                @Override
                public void onSuccess(int status, JSONObject data) {
                    try {
                        h.post(() -> pDialog.setTitleText("درخواست خرید حذف شد")
                                .setContentText("این آیتم از لیست آیتم های مورد درخواست برای خرید حذف شد\nتو کمدا بگرد و کمدت رو خوشحال کن")
                                .setConfirmText("باشه، ممنونم")
                                .setConfirmClickListener(sweetAlertDialog12 -> {
                                    pDialog.dismiss();
                                    refresh();
                                })
                                .showCancelButton(false)
                                .changeAlertType(SweetAlertDialog.SUCCESS_TYPE));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }

                @Override
                public void onFailure(int status, final Error error) {
                    try {
                        h.post(() -> pDialog.setTitleText("مثل اینکه یه مشکلی پیش اومده")
                                .setContentText(error.getMessage())
                                .setConfirmText("بیخیالش شو !")
                                .showCancelButton(false)
                                .setConfirmClickListener(Dialog::dismiss)
                                .changeAlertType(SweetAlertDialog.ERROR_TYPE));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            });
        });

        pDialog.setCustomImage(R.drawable.logo_komodaa);
        pDialog.show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @Override
    public String getTitle() {
        return "خریدم";
    }

    class PurchaseAdapter extends RecyclerView.Adapter<PurchaseAdapter.ViewHolder> {
        List<Product> list;
        Context c;
        private OnItemClickListener mItemClickListener;

        public PurchaseAdapter(List<Product> list) {
            this.list = list;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            c = parent.getContext();
            return new ViewHolder(LayoutInflater.from(c).inflate(R.layout.row_purchased, parent, false));
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            Product p = list.get(position);
            String imageUrl = p.getCover().getPath();
            if (!TextUtils.isEmpty(imageUrl)) {
                Picasso.with(c).load(imageUrl).fit().centerInside().placeholder(R.drawable.loading_main_activity).into(holder.imgProduct, new Callback() {
                    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
                    @Override
                    public void onSuccess() {
                        if (!isAdded()) {
                            return;
                        }
                        Bitmap resource = ((BitmapDrawable) holder.imgProduct.getDrawable()).getBitmap();
                        int intrinsicWidth = resource.getWidth();
                        int width = holder.imgProduct.getWidth();
                        int intrinsicHeight = resource.getHeight();
                        int height = holder.imgProduct.getHeight();
                        if ((intrinsicWidth < width) || intrinsicHeight < height) {
                            Log.d("Image Loaded", "onResourceReady: r.w = " + intrinsicWidth + ", i.w = " + width);
                            holder.imgProduct.setBackground(new BitmapDrawable(getResources(), Blur.fastBlur(c, Utils.scaleImage(c, resource, 50), 25)));
                        }
                    }

                    @Override
                    public void onError() {

                    }
                });
            }
            /**
             * -1 = Wainting, 0 = Invisible, 1 = Visible, 2 = Sold, 3 = Featured, 4 = To be paid
             */
            switch (p.getStatus()) {
                case -1:
                    holder.tvStatus.setText("در انتظار تایید");
                    holder.layoutPaymentButtons.setVisibility(View.INVISIBLE);
                    holder.llPaymentInfo.setVisibility(View.GONE);
                    break;
                case 0:
                    holder.tvStatus.setText("نا موجود");
                    holder.layoutPaymentButtons.setVisibility(View.INVISIBLE);
                    holder.llPaymentInfo.setVisibility(View.GONE);
                    break;
                case 1:
                    holder.tvStatus.setText("موجود");
                    holder.layoutPaymentButtons.setVisibility(View.INVISIBLE);
                    holder.llPaymentInfo.setVisibility(View.GONE);
                    break;
                case 2:
                    holder.tvStatus.setText("پرداخت توسط خریدار");
                    holder.layoutPaymentButtons.setVisibility(View.GONE);
                    holder.llPaymentInfo.setVisibility(View.VISIBLE);
                    holder.tvPrice.setText(String.format(Locale.US, "%s تومان", Utils.formatNumber(p.getPrice())));
                    holder.tvDate.setText(Utils.getPersianDateText(p.getDate(), false));

                    break;
//                case 3:
//                    holder.tvStatus.setText("ویژه");
//                    holder.layoutPaymentButtons.setVisibility(View.INVISIBLE);
//                    holder.llPaymentInfo.setVisibility(View.GONE);
//                    break;
                case 4:
                    holder.tvStatus.setText("در انتظار پرداخت");
                    holder.layoutPaymentButtons.setVisibility(View.VISIBLE);
                    holder.llPaymentInfo.setVisibility(View.GONE);
                    holder.btnPay.setVisibility(View.GONE);
                    break;
                case 5:
                    holder.tvStatus.setText("خریدمش");
                    holder.layoutPaymentButtons.setVisibility(View.GONE);
                    holder.llPaymentInfo.setVisibility(View.VISIBLE);
                    holder.tvPrice.setText(String.format(Locale.US, "%s تومان", Utils.formatNumber(p.getPrice())));
                    holder.tvDate.setText(Utils.getPersianDateText(p.getDate(), false));
                    break;
            }

            holder.btnRate.setVisibility(p.hasRated() ? View.GONE : View.VISIBLE);

        }

        @Override
        public int getItemCount() {
            return list == null ? 0 : list.size();
        }


        public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

            @BindView(R.id.imgProduct) ImageView imgProduct;
            @BindView(R.id.tvStatus) TextView tvStatus;
            @BindView(R.id.llPaymentInfo) LinearLayout llPaymentInfo;
            @BindView(R.id.tvPrice) TextView tvPrice;
            @BindView(R.id.tvDate) TextView tvDate;
            @BindView(R.id.btnPay) Button btnPay;
            @BindView(R.id.btnRate) Button btnRate;
            @BindView(R.id.btnCancel) Button btnCancel;
            @BindView(R.id.llPaymentButtons) LinearLayout layoutPaymentButtons;

            ViewHolder(View itemView) {
                super(itemView);
                ButterKnife.bind(this, itemView);

                btnPay.setOnClickListener(this);
                btnRate.setOnClickListener(this);
                btnCancel.setOnClickListener(this);
                imgProduct.setOnClickListener(this);
            }

            @Override
            public void onClick(View v) {
                if (mItemClickListener != null) {
                    int adapterPosition = getAdapterPosition();
                    mItemClickListener.onItemClick(v, adapterPosition, list.get(adapterPosition));
                }
            }
        }

        public void setOnItemClickListener(final OnItemClickListener itemClickListener) {
            this.mItemClickListener = itemClickListener;
        }

    }
}
