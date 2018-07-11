package com.komodaa.app.fragments;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetView;
import com.komodaa.app.App;
import com.komodaa.app.R;
import com.komodaa.app.activities.HomeActivity;
import com.komodaa.app.activities.MainActivity;
import com.komodaa.app.interfaces.NetworkCallback;
import com.komodaa.app.interfaces.OnItemClickListener;
import com.komodaa.app.models.Error;
import com.komodaa.app.models.Product;
import com.komodaa.app.utils.ApiClient;
import com.komodaa.app.utils.UserUtils;
import com.komodaa.app.utils.Utils;
import com.rey.material.widget.Button;
import com.rey.material.widget.ProgressView;
import com.squareup.picasso.Picasso;
import com.yqritc.recyclerviewflexibledivider.HorizontalDividerItemDecoration;

import net.darkion.AchievementUnlockedLib.AchievementUnlocked;

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
 * Created by nevercom on 11/12/16.
 */

public class RequestsListFragment extends BaseFragment {
    @BindView(R.id.recyclerView)
    RecyclerView mRecyclerView;
    @BindView(R.id.swipe_refresh_layout)
    SwipeRefreshLayout mRefresh;
    boolean loading = true;
    int visibleThreshold = 1;
    int previousTotal = 0;
    int mMaxRecords = 0;
    int mOffset = 0;
    int firstVisibleItem, visibleItemCount, totalItemCount;
    int page = 1;
    int maxPages;
    ListAdapter adapter;
    private Unbinder unbinder;
    private int categoryId = -1;
    private int cityId = -1;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_requests_list, container, false);
        unbinder = ButterKnife.bind(this, v);
        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        performSearch();
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        SharedPreferences prefs = App.getPrefs();

        if (isVisibleToUser) {
            try {
                ((HomeActivity) getActivity()).displaySearchButton(false);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (prefs.getBoolean("is_first_time_req_fragment", true)) {
                prefs.edit().putBoolean("is_first_time_req_fragment", false).apply();

                TapTargetView.showFor(getActivity(), TapTarget.forBounds(
                        getRectForPosition(3, 2),
                        Utils.generateTypeFacedString(getActivity(), "این بخش واسه اونایی هست که دنبال یه آیتم خاصی هستن.\nمیان داخل کمدا و میگن چی می‌خوان، تا سایر کُمُدی ها به دادشون برسن!")).id(1));
            }
        }

    }

    private Rect getRectForPosition(int totalTabs, int position) {
        View tab = getActivity().findViewById(R.id.tabs);
        int top = tab.getTop();
        int w = tab.getWidth();
        int h = tab.getHeight();
        int unit = w / totalTabs;
        Log.d("", String.format("Top =  %d, w = %d, h = %d, unit = %d", top, w, h, unit));
        return new Rect((w - (position * unit) + (unit / 2)) - (unit / 4), top + (h / 2), (w - (position * unit) + (unit / 2)) + (unit / 4), top + h + (h / 2));
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        adapter = new ListAdapter();

        final LinearLayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.addItemDecoration(new HorizontalDividerItemDecoration.Builder(getActivity()).margin(Utils.dpToPx(getActivity(), 16)).build());

        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                visibleItemCount = mRecyclerView.getChildCount();
                totalItemCount = mLayoutManager.getItemCount();
                firstVisibleItem = mLayoutManager.findFirstVisibleItemPosition();


                if (dy < 0 || maxPages <= page) {
                    mRefresh.setRefreshing(false);
                    return;
                }
                if (loading) {
                    if (totalItemCount > previousTotal) {
                        loading = false;
                        previousTotal = totalItemCount;
                    }
                }
                if (!loading && (totalItemCount - visibleItemCount)
                        <= (firstVisibleItem + visibleThreshold)) {
                    // End has been reached
                    page++;
                    loading = true;
                    performSearch();
                }
                // mRefresh.setRefreshing(loading);
            }
        });

        mRecyclerView.setAdapter(adapter);


        mRefresh.setOnRefreshListener(() -> {
            mOffset = 0;
            mMaxRecords = 0;
            previousTotal = 0;
            page = 1;
            loading = false;
            performSearch();
        });

        adapter.setOnItemClickListener((view1, position, data) -> showSelectMyProductDialog((Product) data));

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    void showSelectMyProductDialog(final Product p) {
        final Dialog d = new Dialog(getActivity());
        d.getWindow().setBackgroundDrawable(
                new ColorDrawable(
                        android.graphics.Color.TRANSPARENT));
        d.requestWindowFeature(Window.FEATURE_NO_TITLE);
        d.setContentView(R.layout.dialog_select_product);

        final RecyclerView rec = d.findViewById(R.id.recList);
        final LinearLayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        final ProgressView progress = d.findViewById(R.id.progress);
        rec.setLayoutManager(mLayoutManager);
        rec.setHasFixedSize(true);
        rec.addItemDecoration(new HorizontalDividerItemDecoration.Builder(getActivity()).margin(Utils.dpToPx(getActivity(), 16)).build());
        d.findViewById(R.id.imgClose).setOnClickListener(v -> d.dismiss());
        final TextView empty = d.findViewById(R.id.empty);
        JSONObject data = null;
        try {
            data = new JSONObject();
            data.put("count", 1000);
            data.put("page", 1);
            data.put("category_id", p.getCategoryId());
            data.put("user_id", UserUtils.getUser().getId());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        progress.setVisibility(View.VISIBLE);
        ApiClient.makeRequest(getActivity(), "GET", "/products", data, new NetworkCallback() {
            @Override
            public void onSuccess(int status, JSONObject data) {
                if (rec == null || !isAdded()) {
                    return;
                }
                try {
                    progress.setVisibility(View.GONE);
                    JSONArray products = data.getJSONArray("data");
                    if (products.length() < 1) {
                        rec.setVisibility(View.INVISIBLE);
                        empty.setVisibility(View.VISIBLE);
                    }
                    List<Product> list = new ArrayList<>();
                    for (int i = 0; i < products.length(); i++) {
                        list.add(new Product(products.getJSONObject(i)));
                    }
                    MyProductsAdapter adapter = new MyProductsAdapter(list);
                    rec.setAdapter(adapter);


                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onFailure(int status, Error error) {

                try {
                    progress.setVisibility(View.GONE);
                    rec.setVisibility(View.INVISIBLE);
                    empty.setVisibility(View.VISIBLE);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });


        d.findViewById(R.id.btnSearch).setOnClickListener(v -> {
            int item = ((MyProductsAdapter) rec.getAdapter()).getSelectedProduct();
            if (item < 1) {
                new AchievementUnlocked(getActivity())
                        .setTitle("خطا !")
                        .setIcon(Utils.getDrawableFromRes(getActivity(), R.drawable.no))
                        .setTitleColor(Color.WHITE)
                        .setSubtitleColor(Color.WHITE)
                        .setTypeface(ResourcesCompat.getFont(getActivity(), R.font.iran_sans))
                        .setDuration(3500)
                        .setSubTitle("برای ثبت پیشنهاد باید آیتم انتخاب شده باشد")
                        .setBackgroundColor(Color.parseColor("#e51c23"))
                        .isLarge(false)
                        .build().show();
                return;
            }
            final SweetAlertDialog pDialog = new SweetAlertDialog(getActivity(), SweetAlertDialog.PROGRESS_TYPE);
            pDialog.getProgressHelper().setBarColor(Utils.getColorFromResource(getActivity(), R.color.colorPrimary));
            pDialog.setTitleText("در حال ثبت پیشنهاد");
            pDialog.setContentText("داریم پیشنهاد رو تو سرور ثبت می کنیم");
            pDialog.setCancelable(false);
            pDialog.show();
            JSONObject data1 = null;
            data1 = new JSONObject();
            try {
                data1.put("offered_product_id", item);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            final Handler h = new Handler(getActivity().getMainLooper());
            ApiClient.makeRequest(getActivity(), "POST", "/offer/" + p.getId(), data1, new NetworkCallback() {
                @Override
                public void onSuccess(int status, JSONObject data1) {

                    h.post(() -> pDialog.setTitleText("تبریک ! پیشنهاد آیتمتون ثبت شد")
                            .setContentText("به کاربر کمدایی که این آیتم رو نیاز داشت خبر دادیم، امیدواریم تجربه ی خوبی در استفاده از کمدا داشته باشید")
                            .setConfirmText("باشه، مرسی")
                            .setConfirmClickListener(sweetAlertDialog -> {
                                sweetAlertDialog.dismiss();
                                d.dismiss();
                            })
                            .changeAlertType(SweetAlertDialog.SUCCESS_TYPE));
                }

                @Override
                public void onFailure(int status, final Error error) {
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
    }


    void performSearch() {
        JSONObject data = null;
        try {
            data = new JSONObject();
            data.put("count", 20);
            data.put("page", this.page);
            data.put("type", 2);
//            data.put("city_id", cityId);
//            data.put("category_id", categoryId);
            //data.put("user_id", 1);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        mRefresh.setRefreshing(true);
        ApiClient.makeRequest(getActivity(), "GET", "/products", data, new NetworkCallback() {
            @Override
            public void onSuccess(int status, JSONObject data) {

                try {
                    if (mRefresh == null) {
                        return;
                    }
                    mRefresh.setRefreshing(false);

                    Log.d("", "onSuccess: " + data.toString());
                    maxPages = data.getInt("total_pages");
                    JSONArray products = data.getJSONArray("data");
                    List<Product> list = new ArrayList<>();
                    for (int i = 0; i < products.length(); i++) {
                        list.add(new Product(products.getJSONObject(i)));
                    }
                    adapter.setData(list, page > 1);
                    adapter.notifyDataSetChanged();


                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onFailure(int status, Error error) {

                try {
                    mRefresh.setRefreshing(false);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public String getTitle() {
        return "نیازمندی‌ ها";
    }

    public interface OnItemRemoveListener {
        void onItemRemove(int id);
    }

    public class ListAdapter extends RecyclerView.Adapter<ListAdapter.ViewHolder> {
        OnItemClickListener mItemClickListener;
        private List<Product> list;
        private Context c;
        private OnItemRemoveListener mItemRemoveListener;


        void setData(List<Product> data, boolean append) {
            if (append && list != null) {
                list.addAll(data);
            } else {
                list = data;
            }
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            c = parent.getContext();
            return new ViewHolder(LayoutInflater.from(c).inflate(R.layout.row_list_request, parent, false));
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            final Product p = list.get(position);
            holder.tvCategory.setText(String.format("درخواست برای %s", Utils.getCategoryName(p.getCategoryId())));
            holder.tvCity.setText(String.format("شهر: %s", Utils.getCityName(c, p.getCityId())));
            if (p.getUserId() == UserUtils.getUser().getId() || !UserUtils.isLoggedIn()) {
                holder.btnSubmit.setVisibility(View.INVISIBLE);
            } else {
                holder.btnSubmit.setVisibility(View.VISIBLE);

            }
            try {
                JSONObject json = new JSONObject(p.getDescription());
                String description = json.getString("description");
                String dueDate = json.getString("due_date");

                String persianDateText = Utils.getPersianDateText(dueDate, false);
                holder.tvDesc.setText(description);
                if (!TextUtils.isEmpty(persianDateText)) {
                    holder.tvDueDate.setVisibility(View.VISIBLE);
                    holder.tvDueDate.setText(String.format("مهلت: %s", persianDateText));
                } else {
                    holder.tvDueDate.setVisibility(View.INVISIBLE);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }

        @Override
        public int getItemCount() {
            return list == null ? 0 : list.size();
        }

        public void setOnItemClickListener(final OnItemClickListener itemClickListener) {
            this.mItemClickListener = itemClickListener;
        }

        public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

            @BindView(R.id.tvCategory)
            TextView tvCategory;
            @BindView(R.id.tvCity)
            TextView tvCity;
            @BindView(R.id.tvDueDate)
            TextView tvDueDate;
            @BindView(R.id.tvDesc)
            TextView tvDesc;
            @BindView(R.id.btnSubmit)
            Button btnSubmit;

            ViewHolder(View itemView) {
                super(itemView);
                ButterKnife.bind(this, itemView);

                btnSubmit.setOnClickListener(this);
            }

            @Override
            public void onClick(View v) {
                if (mItemClickListener != null) {
                    int adapterPosition = getAdapterPosition();
                    mItemClickListener.onItemClick(v, adapterPosition, list.get(adapterPosition));
                }
            }
        }
    }

    public class MyProductsAdapter extends RecyclerView.Adapter<MyProductsAdapter.ViewHolder> {
        private List<Product> list;
        private Context context;
        private int selectedItem = -1;

        public MyProductsAdapter(List<Product> list) {
            this.list = list;
        }

        public int getSelectedProduct() {
            return selectedItem;
        }

        @Override
        public MyProductsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            context = parent.getContext();
            View v = LayoutInflater.from(context).inflate(R.layout.row_spinner_product, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(MyProductsAdapter.ViewHolder holder, int position) {
            Product p = list.get(position);

            Picasso.with(context).load(p.getCover().getPath()).fit().centerInside().into(holder.imgProduct);
            holder.tvPrice.setText(String.format("%s تومان", Utils.formatNumber(p.getPrice())));

            if (selectedItem == p.getId()) {
                holder.itemView.setBackgroundColor(Color.parseColor("#00ac71"));
            } else {
                holder.itemView.setBackgroundColor(Color.parseColor("#ffffff"));
            }

        }

        @Override
        public int getItemCount() {
            return list != null ? list.size() : 0;
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            //@BindView(R.id.rlRow) RelativeLayout rlRow;
            @BindView(R.id.imgProduct)
            ImageView imgProduct;
            @BindView(R.id.tvPrice)
            TextView tvPrice;

            public ViewHolder(View itemView) {
                super(itemView);
                ButterKnife.bind(this, itemView);
                itemView.setOnClickListener(v -> {
                    selectedItem = list.get(getAdapterPosition()).getId();
                    notifyDataSetChanged();
                });
            }
        }
    }

}
