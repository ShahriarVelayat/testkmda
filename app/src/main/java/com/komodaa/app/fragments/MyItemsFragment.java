package com.komodaa.app.fragments;

import android.annotation.TargetApi;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
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
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.komodaa.app.App;
import com.komodaa.app.R;
import com.komodaa.app.activities.EditProductActivity;
import com.komodaa.app.activities.ProductDetailsActivity;
import com.komodaa.app.activities.ProfileActivity;
import com.komodaa.app.interfaces.NetworkCallback;
import com.komodaa.app.interfaces.OnItemClickListener;
import com.komodaa.app.models.Error;
import com.komodaa.app.models.Product;
import com.komodaa.app.utils.ApiClient;
import com.komodaa.app.utils.Blur;
import com.komodaa.app.utils.UserUtils;
import com.komodaa.app.utils.Utils;
import com.rengwuxian.materialedittext.MaterialEditText;
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
import butterknife.OnClick;
import butterknife.Unbinder;
import cn.pedant.SweetAlert.SweetAlertDialog;

import static android.app.Activity.RESULT_OK;

/**
 * Created by nevercom on 11/7/16.
 */

public class MyItemsFragment extends BaseFragment {
    public static final int REQUEST_CODE = 15865;
    @BindView(R.id.recList) RecyclerView recList;
    @BindView(R.id.tvWarningSheba) TextView tvWarningSheba;
    private Unbinder unbinder;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recycler_view_my_items, container, false);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode != REQUEST_CODE) {
            return;
        }
        if (resultCode == RESULT_OK) {
            refresh();
        }
    }

    void refresh() {
        JSONObject data = null;
        try {
            data = new JSONObject();
            data.put("user_id", UserUtils.getUser().getId());
            data.put("full", true);
            data.put("type", 1);
            data.put("count", 200);
            data.put("my_items", true);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        ApiClient.makeRequest(getActivity(), "GET", "/products", data, new NetworkCallback() {
            @Override
            public void onSuccess(int status, JSONObject data) {
                try {
                    if (recList == null || !isAdded()) {
                        return;
                    }
                    Log.d("Data", data.toString());
                    JSONArray products = data.getJSONArray("data");
                    List<Product> list = new ArrayList<>();
                    for (int i = 0; i < products.length(); i++) {
                        list.add(new Product(products.getJSONObject(i)));
                    }
                    PurchaseAdapter adapter = new PurchaseAdapter(list);
                    adapter.setOnItemClickListener((view, position, data1) -> {
                        switch (view.getId()) {
                            case R.id.btnEdit:
                                edit((Product) data1);
                                break;
                            case R.id.btnCancel:
                                cancelPayment((Product) data1);
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

            }
        });
    }

    private void edit(final Product p) {
        startActivityForResult(new Intent(getActivity(), EditProductActivity.class).putExtra("item", p), REQUEST_CODE);
        /*
        final Dialog d = new Dialog(getActivity());
        d.getWindow().setBackgroundDrawable(
                new ColorDrawable(Color.TRANSPARENT));
        d.requestWindowFeature(Window.FEATURE_NO_TITLE);
        d.setContentView(R.layout.dialog_edit_item);
        final MaterialEditText metPrice = d.findViewById(R.id.metPrice);

        d.findViewById(R.id.imgClose).setOnClickListener(v -> d.dismiss());


        d.findViewById(R.id.btnEdit).setOnClickListener(v -> {
            String price = metPrice.getText().toString();
            if (TextUtils.isEmpty(price) || !TextUtils.isDigitsOnly(price) || Integer.parseInt(price) < 1) {
                metPrice.setError("مبلغ معتبری رو وارد کن");
                return;
            }

            final SweetAlertDialog pDialog = new SweetAlertDialog(getActivity(), SweetAlertDialog.PROGRESS_TYPE);
            pDialog.getProgressHelper().setBarColor(Utils.getColorFromResource(getActivity(), R.color.colorPrimary));
            pDialog.setTitleText("در حال ویرایش آیتم");
            pDialog.setContentText("داریم آیتم رو ویرایش می کنیم");
            pDialog.setCancelable(false);
            pDialog.show();

            JSONObject data = null;
            try {
                data = new JSONObject();
                data.put("price", price);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            final Handler h = new Handler(getActivity().getMainLooper());
            ApiClient.makeRequest(getActivity(), "POST", "/product/" + p.getId(), data, new NetworkCallback() {
                @Override public void onSuccess(int status, JSONObject data) {

                    h.post(() -> pDialog.setTitleText("آیتم ویرایش شد")
                            .setContentText("تغییرات مدنظرت روی آیتم اعمال شد.")
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
        */
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        try {
            JSONObject json = new JSONObject(App.getPrefs().getString("user_meta", null));
            if (json.has("sheba_missing") && json.getBoolean("sheba_missing")) {
                tvWarningSheba.setVisibility(View.VISIBLE);
            } else {
                tvWarningSheba.setVisibility(View.GONE);

            }
        } catch (JSONException e) {
            e.printStackTrace();
            tvWarningSheba.setVisibility(View.GONE);
        }
        final LinearLayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        recList.setLayoutManager(mLayoutManager);
        recList.setHasFixedSize(true);
        recList.addItemDecoration(new HorizontalDividerItemDecoration.Builder(getActivity()).size(Utils.dpToPx(getActivity(), 8)).color(Color.TRANSPARENT).build());
        refresh();

    }


    private void cancelPayment(final Product p) {
        Log.d("", "cancelPayment() called with: p = [" + p + "]");
        final Context c = getActivity();
        final Handler h = new Handler(c.getMainLooper());
        final SweetAlertDialog pDialog = new SweetAlertDialog(c, SweetAlertDialog.WARNING_TYPE);
        pDialog.setTitleText("حذف آیتم");
        pDialog.setContentText(" داری آیتم رو از کمدت حذف می کنی، مطمئنی؟");
        pDialog.setConfirmText("آره دیگه");
        pDialog.setCancelText("نه، ولش کن!");
        pDialog.setConfirmClickListener(sweetAlertDialog -> {
            pDialog.setTitleText("در حال حذف آیتم")
                    .setContentText("درحال حذف آیتم از لیست آیتم های شما")
                    .showCancelButton(false)
                    .changeAlertType(SweetAlertDialog.PROGRESS_TYPE);

            ApiClient.makeRequest(c, "DELETE", "/product/" + p.getId(), null, new NetworkCallback() {
                @Override
                public void onSuccess(int status, JSONObject data) {
                    h.post(() -> pDialog.setTitleText("آیتم حذف شد")
                            .setContentText("این آیتم از لیست آیتم های شما حذف شد\nتو کمدا بگرد و کمدت رو خوشحال کن")
                            .setConfirmText("باشه، ممنونم")
                            .setConfirmClickListener(sweetAlertDialog12 -> {
                                pDialog.dismiss();
                                refresh();
                            })
                            .showCancelButton(false)
                            .changeAlertType(SweetAlertDialog.SUCCESS_TYPE));

                }

                @Override
                public void onFailure(int status, final Error error) {
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
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @Override
    public String getTitle() {
        return "می فروشم";
    }

    @OnClick(R.id.tvWarningSheba)
    public void onClick() {
        startActivity(new Intent(getActivity(), ProfileActivity.class));
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
            return new ViewHolder(LayoutInflater.from(c).inflate(R.layout.row_my_items, parent, false));
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, final int position) {
            Product p = list.get(position);
            String imageUrl = p.getCover().getPath();
            if (!TextUtils.isEmpty(imageUrl)) {
                Picasso.with(c).load(imageUrl).fit().centerInside().placeholder(R.drawable.loading_main_activity).into(holder.imgProduct, new Callback() {
                    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
                    @Override
                    public void onSuccess() {
                        // Since there is an Async operation here,
                        // make sure fragment is still attached to the activity
                        if (c == null || !isAdded()) {
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
            holder.tvProductId.setText(String.format(Locale.US, "کد آیتم: %s", Utils.formatNumber(p.getId())));
            switch (p.getStatus()) {
                case -1:
                    holder.tvStatus.setText("در انتظار تایید");
                    holder.layoutPaymentButtons.setVisibility(View.VISIBLE);
                    holder.btnEdit.setVisibility(View.GONE);
                    holder.llPaymentInfo.setVisibility(View.GONE);
                    break;
                case 0:
                    holder.tvStatus.setText("نا موجود");
                    holder.layoutPaymentButtons.setVisibility(View.VISIBLE);
                    holder.btnEdit.setVisibility(View.GONE);
                    holder.llPaymentInfo.setVisibility(View.GONE);
                    break;
                case 1:
                    holder.tvStatus.setText("تایید شده");
                    holder.layoutPaymentButtons.setVisibility(View.VISIBLE);
                    holder.llPaymentInfo.setVisibility(View.GONE);
                    break;
                case 2:
                    holder.tvStatus.setText("پرداخت توسط خریدار");
                    holder.layoutPaymentButtons.setVisibility(View.GONE);
                    holder.llPaymentInfo.setVisibility(View.VISIBLE);
                    holder.tvPrice.setText(p.getBuyerName());
                    holder.tvDate.setText(Utils.getPersianDateText(p.getSellDate(), false));
                    break;
//                case 3:
//                    holder.tvStatus.setText("ویژه");
//                    holder.layoutPaymentButtons.setVisibility(View.VISIBLE);
//                    holder.llPaymentInfo.setVisibility(View.GONE);
//                    break;
                case 4:
                    holder.tvStatus.setText("در انتظار پرداخت");
                    holder.layoutPaymentButtons.setVisibility(View.GONE);
                    holder.llPaymentInfo.setVisibility(View.VISIBLE);
                    holder.tvPrice.setText(String.format(Locale.US, "%s تومان", Utils.formatNumber(p.getPrice())));
                    holder.tvDate.setText(Utils.getPersianDateText(p.getDate(), false));
                    break;
                case 5:
                    holder.tvStatus.setText("تحویل خریدار شد");
                    holder.layoutPaymentButtons.setVisibility(View.GONE);
                    holder.llPaymentInfo.setVisibility(View.VISIBLE);
                    holder.tvPrice.setText(p.getBuyerName());
                    holder.tvDate.setText(Utils.getPersianDateText(p.getSellDate(), false));
                    break;
            }

            if (p.getStatus() == 2 || p.getStatus() == 5) {
                holder.ind.setVisibility(View.VISIBLE);
//                holder.itemView.setBackgroundColor(Utils.getColorFromResource(c, R.color.colorPrimary));
//
//                holder.tvPrice.setTextColor(Color.parseColor("#ffffff"));
//                holder.tvDate.setTextColor(Color.parseColor("#ffffff"));
//                holder.tvStatus.setTextColor(Color.parseColor("#ffffff"));
//                holder.tvProductId.setTextColor(Color.parseColor("#ffffff"));
            } else {
                holder.ind.setVisibility(View.GONE);

//                holder.itemView.setBackgroundColor(Color.parseColor("#ffffff"));
//
//                holder.tvPrice.setTextColor(holder.defaultTextColor);
//                holder.tvDate.setTextColor(holder.defaultTextColor);
//                holder.tvStatus.setTextColor(holder.defaultTextColor);
//                holder.tvProductId.setTextColor(holder.defaultTextColor);
            }
        }

        @Override
        public int getItemCount() {
            return list == null ? 0 : list.size();
        }


        public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

            @BindView(R.id.imgProduct) ImageView imgProduct;
            @BindView(R.id.tvStatus) TextView tvStatus;
            @BindView(R.id.tvProductId) TextView tvProductId;
            @BindView(R.id.btnEdit) Button btnEdit;
            @BindView(R.id.btnCancel) Button btnCancel;
            @BindView(R.id.llPaymentButtons) LinearLayout layoutPaymentButtons;
            @BindView(R.id.llPaymentInfo) LinearLayout llPaymentInfo;
            @BindView(R.id.tvPrice) TextView tvPrice;
            @BindView(R.id.tvDate) TextView tvDate;
            @BindView(R.id.ind) View ind;
            //int defaultTextColor = 0;

            ViewHolder(View itemView) {
                super(itemView);
                ButterKnife.bind(this, itemView);
                //defaultTextColor = tvStatus.getCurrentTextColor();
                btnEdit.setOnClickListener(this);
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
