package com.komodaa.app.fragments;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.komodaa.app.R;
import com.komodaa.app.activities.SubmitPaymentReceipt;
import com.komodaa.app.interfaces.NetworkCallback;
import com.komodaa.app.interfaces.OnItemClickListener;
import com.komodaa.app.models.Error;
import com.komodaa.app.models.Product;
import com.komodaa.app.utils.ApiClient;
import com.komodaa.app.utils.UserUtils;
import com.komodaa.app.utils.Utils;
import com.yqritc.recyclerviewflexibledivider.HorizontalDividerItemDecoration;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import cn.pedant.SweetAlert.SweetAlertDialog;

/**
 * Created by nevercom on 11/7/16.
 */

public class MyRequestsFragment extends BaseFragment {
    @BindView(R.id.recList) RecyclerView recList;
    private Unbinder unbinder;

    @Nullable @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recycler_view, container, false);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        final LinearLayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        recList.setLayoutManager(mLayoutManager);
        recList.setHasFixedSize(true);
        recList.addItemDecoration(new HorizontalDividerItemDecoration.Builder(getActivity()).size(Utils.dpToPx(getActivity(), 8)).color(Color.TRANSPARENT).build());

    }

    @Override public void onResume() {
        super.onResume();
        refresh();
    }

    void refresh() {
        JSONObject data = null;
        try {
            data = new JSONObject();
            data.put("user_id", UserUtils.getUser().getId());
            data.put("full", true);
            data.put("type", 2);
            data.put("count", 200);

            //data.put("user_id", 1);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        ApiClient.makeRequest(getActivity(), "GET", "/products", data, new NetworkCallback() {
            @Override public void onSuccess(int status, JSONObject data) {
                try {
                    if (recList == null||!isAdded()) {
                        return;
                    }
                    JSONArray products = data.getJSONArray("data");
                    List<Product> list = new ArrayList<>();
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
                        }
                    });
                    recList.setAdapter(adapter);


                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }

            @Override public void onFailure(int status, Error error) {

            }
        });
    }

    private void cancelPayment(final Product p) {
        Log.d("", "cancelPayment() called with: p = [" + p + "]");
        final Context c = getActivity();
        final Handler h = new Handler(c.getMainLooper());
        final SweetAlertDialog pDialog = new SweetAlertDialog(c, SweetAlertDialog.WARNING_TYPE);
        pDialog.setTitleText("حذف درخواست");
        pDialog.setContentText("واقعا می خوای حذفش کنی؟");
        pDialog.setConfirmText("آره حذف کن بره!");
        pDialog.setCancelText("وااای نه!");
        pDialog.setConfirmClickListener(sweetAlertDialog -> {
            pDialog.setTitleText("در حال حذف درخواست")
                    .setContentText("درحال حذف درخواست از لیست درخواست های شما")
                    .showCancelButton(false)
                    .changeAlertType(SweetAlertDialog.PROGRESS_TYPE);

            ApiClient.makeRequest(c, "DELETE", "/product/" + p.getId(), null, new NetworkCallback() {
                @Override public void onSuccess(int status, JSONObject data) {
                    try {
                        h.post(() -> pDialog.setTitleText("درخواست حذف شد")
                                .setContentText("این درخواست از لیست درخواست های شما حذف شد\nتو کمدا بگرد و کمدت رو خوشحال کن")
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

                @Override public void onFailure(int status, final Error error) {
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

    @Override public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @Override public String getTitle() {
        return "لازم دارم";
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
            return new ViewHolder(LayoutInflater.from(c).inflate(R.layout.row_request, parent, false));
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            final Product p = list.get(position);
            holder.tvCategory.setText(String.format("درخواست برای %s", Utils.getCategoryName(p.getCategoryId())));
            holder.tvCity.setText(String.format("شهر: %s", Utils.getCityName(c, p.getCityId())));
            holder.tvOffersCount.setText(p.getCommentsCount() > 0 ? p.getCommentsCount() + " پیشنهاد ارسال شده است" : "پیشنهادی ارسال نشده است");

            try {
                JSONObject json = new JSONObject(p.getDescription());
                String description = json.getString("description");
                String dueDate = json.getString("due_date");
                holder.tvDesc.setText(description);
                holder.tvDueDate.setText(String.format("مهلت: %s", Utils.getPersianDateText(dueDate, false)));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            holder.imgDelete.setOnClickListener(v -> {
                final Handler h = new Handler(c.getMainLooper());


                if (!UserUtils.isLoggedIn()) {
                    Utils.displayLoginErrorDialog(c);
                    return;
                }
                final SweetAlertDialog pDialog = new SweetAlertDialog(c, SweetAlertDialog.WARNING_TYPE);
                pDialog.setTitleText("حذف نیازمندی");
                pDialog.setContentText("آیا از حذف این درخواست اطمینان دارید ؟");
                pDialog.setConfirmText("آره حذفش کن");
                pDialog.setCancelText("واای نه !");
                pDialog.setConfirmClickListener(sweetAlertDialog -> {
                    pDialog.setTitleText("در حال حذف درخواست")
                            .setContentText("داریم این درخواست رو حذف می کنیم")
                            .showCancelButton(false)
                            .changeAlertType(SweetAlertDialog.PROGRESS_TYPE);

                    ApiClient.makeRequest(c, "DELETE", "/request/" + p.getId(), null, new NetworkCallback() {
                        @Override public void onSuccess(int status, JSONObject data) {
                            h.post(() -> pDialog.setTitleText("درخواست حذف شد !")
                                    .setContentText("این درخواست از کمدا حذف شد، توی کمدا بگرد کلی چیزای خوب هست واسه کمدت :)")
                                    .setConfirmText("باشه، ممنونم")
                                    .setConfirmClickListener(sweetAlertDialog12 -> {
                                        pDialog.dismiss();
                                        refresh();
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

            });
        }

        @Override public int getItemCount() {
            return list == null ? 0 : list.size();
        }


        public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

            @BindView(R.id.tvCategory) TextView tvCategory;
            @BindView(R.id.tvCity) TextView tvCity;
            @BindView(R.id.tvOffersCount) TextView tvOffersCount;
            @BindView(R.id.tvDueDate) TextView tvDueDate;
            @BindView(R.id.tvDesc) TextView tvDesc;
            @BindView(R.id.imgDelete) ImageView imgDelete;

            ViewHolder(View itemView) {
                super(itemView);
                ButterKnife.bind(this, itemView);

                imgDelete.setOnClickListener(this);
            }

            @Override public void onClick(View v) {
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
