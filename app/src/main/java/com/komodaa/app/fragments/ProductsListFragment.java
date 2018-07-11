package com.komodaa.app.fragments;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.SearchEvent;
import com.komodaa.app.App;
import com.komodaa.app.BuildConfig;
import com.komodaa.app.R;
import com.komodaa.app.activities.HomeActivity;
import com.komodaa.app.activities.MainActivity;
import com.komodaa.app.activities.ProductDetailsActivity;
import com.komodaa.app.interfaces.ISearchableFragment;
import com.komodaa.app.interfaces.NetworkCallback;
import com.komodaa.app.interfaces.OnItemClickListener;
import com.komodaa.app.models.Attribute;
import com.komodaa.app.models.Category;
import com.komodaa.app.models.Error;
import com.komodaa.app.models.Product;
import com.komodaa.app.utils.ApiClient;
import com.komodaa.app.utils.Blur;
import com.komodaa.app.utils.Utils;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.rey.material.widget.FloatingActionButton;
import com.rey.material.widget.ImageButton;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.toptoche.searchablespinnerlibrary.SearchableSpinner;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import cn.pedant.SweetAlert.SweetAlertDialog;

/**
 * Created by nevercom on 11/12/16.
 */

public class ProductsListFragment extends BaseFragment implements ISearchableFragment {
    @BindView(R.id.recyclerView) RecyclerView mRecyclerView;
    @BindView(R.id.swipe_refresh_layout) SwipeRefreshLayout mRefresh;
    @BindView(R.id.tvViewingUserProducts) TextView tvViewingUserProducts;
    @BindView(R.id.rlViewingUserWrap) RelativeLayout rlViewingUserWrap;
    @BindView(R.id.mbtn_close) ImageButton mbtnClose;
    //@BindView(R.id.fabTop) android.support.design.widget.FloatingActionButton fabTop;
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
    private CategoriesAdapter catAdapter;
    private int userId;
    private String userName;
    //private int scWidth;
    private int itemCondition;
    private int itemPriceRange;
    private String komodiName;
    private boolean isSearchRequested;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.content_main, container, false);
        Bundle b = getArguments();
        userId = b.getInt("user_id", -1);
        userName = b.getString("user_name");
        isSearchRequested = b.getBoolean("do_search");
        unbinder = ButterKnife.bind(this, v);
        //scWidth = getActivity().getWindowManager().getDefaultDisplay().getWidth();
        return v;
    }

    @OnClick(R.id.fabTop)
    void onFabClick() {
        goToTop();
    }

    public void goToTop() {
        mRecyclerView.smoothScrollToPosition(0);
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshCategories();
        //performSearch(false);
    }

    @Override public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            try {
                ((HomeActivity) getActivity()).displaySearchButton(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
//        if (item.getItemId() == R.id.item_search) {
//            showSearchDialog();
//            return true;
//        }
        return super.onOptionsItemSelected(item);
    }

    void refreshCategories() {
        String conf = App.getPrefs().getString("config", null);

        if (conf != null) {
            return;
        }
        JSONObject data = new JSONObject();
        try {
            data.put("app_version", BuildConfig.VERSION_CODE);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        ApiClient.makeRequest(getActivity(), "GET", "/all", data, new NetworkCallback() {
            @Override
            public void onSuccess(int status, JSONObject data) {
                Log.d("", "onSuccess: ");
                App.getPrefs().edit().putString("config", data.toString()).apply();
                String json = App.getPrefs().getString("config", null);

                List<Category> categories = new ArrayList<>();
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
                                JSONObject aObj = atts.getJSONObject(j);
                                Attribute a = new Attribute();
                                a.setId(aObj.getInt("id"));
                                a.setName(aObj.getString("name"));

                                c.addAttribute(a);
                            }
                            categories.add(c);
                        }
                        Category catAll = new Category();
                        catAll.setId(-1);
                        catAll.setName("همه ی آیتم ها");
                        categories.add(0, catAll);
                        catAdapter = new CategoriesAdapter(categories);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

            }

            @Override
            public void onFailure(int status, Error error) {
                Log.d("", "onFailure: ");
            }
        });

    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        adapter = new ListAdapter();
        if (userId > 0) {
            rlViewingUserWrap.setVisibility(View.VISIBLE);
            tvViewingUserProducts.setText(String.format("درحال مشاهده آیتم های %s هستی", userName));
            tvViewingUserProducts.setEllipsize(TextUtils.TruncateAt.MARQUEE);
            tvViewingUserProducts.setSingleLine(true);
            tvViewingUserProducts.setSelected(true);
            mbtnClose.setOnClickListener(v -> {
                //startActivity(new Intent(getActivity(), MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK))
                resetSearch();
                performSearch(false);
            });
        } else {
            rlViewingUserWrap.setVisibility(View.GONE);
        }
        final LinearLayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                visibleItemCount = mRecyclerView.getChildCount();
                totalItemCount = mLayoutManager.getItemCount();
                firstVisibleItem = mLayoutManager.findFirstVisibleItemPosition();
                Log.d("", String.format("onScrolled: MaxPages: %d, Current Page: %d, Dy: %d", maxPages, page, dy));
                if (maxPages <= page) {
                    mRefresh.setRefreshing(false);
                    return;
                }
//                if (recyclerView.computeVerticalScrollOffset() > scWidth * 10) {
//                    fabTop.show();
//                } else {
//                    fabTop.hide();
//                }
                if (dy < 0) {
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
                    loading = true;
                    page++;
                    performSearch(false);
                }
                // mRefresh.setRefreshing(loading);
            }
        });

        mRecyclerView.setAdapter(adapter);

        if (isSearchRequested) {
            doSearch();
        }

        mRefresh.setOnRefreshListener(() -> {
            mOffset = 0;
            mMaxRecords = 0;
            previousTotal = 0;
            page = 1;
            loading = false;
            performSearch(false);
        });

        adapter.setOnItemClickListener((view1, position, data) -> {
            Intent i = new Intent(getActivity(), ProductDetailsActivity.class);
            i.putExtra("item", (Product) data);
            startActivity(i);
        });


        String json = App.getPrefs().getString("config", null);

        List<Category> categories = new ArrayList<>();
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
                        JSONObject aObj = atts.getJSONObject(j);
                        Attribute a = new Attribute();
                        a.setId(aObj.getInt("id"));
                        a.setName(aObj.getString("name"));

                        c.addAttribute(a);
                    }
                    categories.add(c);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        Category catAll = new Category();
        catAll.setId(-1);
        catAll.setName("همه ی آیتم ها");
        categories.add(0, catAll);
        catAdapter = new CategoriesAdapter(categories);
        performSearch(false);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    private void showSearchDialog() {
        final Dialog d = new Dialog(getActivity());
        d.getWindow().setBackgroundDrawable(
                new ColorDrawable(
                        Color.TRANSPARENT));
        d.requestWindowFeature(Window.FEATURE_NO_TITLE);
        d.setContentView(R.layout.dialog_search);
        final Spinner spCategory = d.findViewById(R.id.spCategory);
        final Spinner spCondition = d.findViewById(R.id.spCondition);
        final Spinner spPriceRange = d.findViewById(R.id.spPriceRange);
        //final MaterialEditText metKomodi = d.findViewById(R.id.metKomodi);
        spCategory.setAdapter(catAdapter);

        ArrayAdapter<String> conditionsAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, new String[]{
                "همه",
                "کاملاً نو",
                "در حد نو",
                "کاردستی",
        });
        conditionsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spCondition.setAdapter(conditionsAdapter);

        ArrayAdapter<String> pricesAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, new String[]{
                "هر قیمتی",
                "تا 10 هزار تومان",
                "از 10 هزار تا 30 هزار تومان",
                "از 30 هزار تا 50 هزار تومان",
                "از 50 هزار تا 80 هزار تومان",
                "از 80 هزار تا 150 هزار تومان",
                "از 150 هزار تومان به بالا",
        });
        pricesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spPriceRange.setAdapter(pricesAdapter);

        final SearchableSpinner spCity = d.findViewById(R.id.mspCity);
        List<String> cities = new LinkedList<>(Arrays.asList(getResources().getStringArray(R.array.cities)));
        cities.add(0, "همه شهرها");
        final ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, cities);
        spCity.setAdapter(adapter);
        spCity.setTitle("شهر محل سکونت خود را انتخاب کنید:");
        spCity.setPositiveButton("باشه");
        spCity.setSelection(738/* Shiraz = 737*/);
        d.findViewById(R.id.btnSearch).setOnClickListener(v -> {
            categoryId = ((Category) spCategory.getSelectedItem()).getId();
            cityId = spCity.getSelectedItemPosition() - 1;
            itemCondition = spCondition.getSelectedItemPosition();
            itemPriceRange = spPriceRange.getSelectedItemPosition();
            //komodiName = metKomodi.getText().toString();
            performSearch(true);
            d.dismiss();

            goToTop();

            try {
                String categoryName = Utils.getCategoryName(categoryId);
                if (TextUtils.isEmpty(categoryName)) {
                    categoryName = "همه آیتم ها";
                }
                String cityName = Utils.getCityName(getActivity(), cityId);
                Answers.getInstance().logSearch(new SearchEvent()
                        .putQuery(String.format(Locale.US, " %s - %s - %s - %s", categoryName, cityName, spCondition.getSelectedItem(), spPriceRange.getSelectedItem()))
                        .putCustomAttribute("Category", categoryName)
                        .putCustomAttribute("City", cityName)
                        .putCustomAttribute("Condition", (String) spCondition.getSelectedItem())
                        .putCustomAttribute("Price Range", (String) spPriceRange.getSelectedItem())
                );
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        d.show();
    }

    private void resetSearch() {
        cityId = -1;
        categoryId = -1;
        itemCondition = -1;
        itemPriceRange = -1;
        komodiName = "";
        userId = -1;
        userName = "";
        page = 1;
        rlViewingUserWrap.setVisibility(View.GONE);
    }

    void performSearch(final boolean userInitiated) {
        JSONObject data = null;
        try {
            data = new JSONObject();
            data.put("count", 20);
            data.put("page", this.page);
            data.put("city_id", cityId);
            data.put("category_id", categoryId);
            data.put("condition", itemCondition);
            data.put("price_range", itemPriceRange);
            //data.put("komodi_name", komodiName);
            if (userId > 0) {
                data.put("user_id", userId);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        mRefresh.setRefreshing(true);
        final SweetAlertDialog pDialog = new SweetAlertDialog(getActivity(), SweetAlertDialog.PROGRESS_TYPE);
        if (userInitiated) {

            pDialog.getProgressHelper().setBarColor(Utils.getColorFromResource(getActivity(), R.color.colorPrimary));
            pDialog.setTitleText("در حال جستجو");
            pDialog.setContentText(getString(R.string.loadin_please_wait));
            pDialog.setCancelable(false);
            pDialog.show();
        }
        ApiClient.makeRequest(getActivity(), "GET", "/products", data, new NetworkCallback() {
            @Override
            public void onSuccess(int status, JSONObject data) {
                try {
                    if (mRefresh == null || !isAdded()) {
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
                    if (list.size() < 1 && userInitiated) {
                        pDialog.setTitleText("اینی که می خوای رو نداریم")
                                .setContentText(TextUtils.isEmpty(komodiName) ? "لطفاً منتظر باش تا یکی برای فروش بذاره..." : "اِ ایشون که دنبالشی، یا نیستش یا آیتمی برای فروش نداره!")
                                .setConfirmText("باشه")
                                .showCancelButton(false)
                                .setConfirmClickListener(Dialog::dismiss)
                                .setCancelClickListener(Dialog::dismiss)
                                .changeAlertType(SweetAlertDialog.ERROR_TYPE);

                        cityId = -1;
                        categoryId = -1;
                        itemCondition = -1;
                        itemPriceRange = -1;
                        komodiName = "";
                        return;
                    }
                    adapter.setData(list, page > 1);
                    adapter.notifyDataSetChanged();
                    if (userInitiated) {
                        pDialog.dismissWithAnimation();

                        if (userId < 1) {
                            rlViewingUserWrap.setVisibility(View.VISIBLE);

                            String cat = categoryId > 0 ? "دسته ی " + Utils.getCategoryName(categoryId) : "آیتم های";
                            String cityName = cityId > 0 ? "شهر " + Utils.getCityName(getActivity(), cityId) : "همه شهرها";
                            tvViewingUserProducts.setText(String.format(Locale.US, "درحال مشاهده %s %s هستی", cat, cityName));

                            tvViewingUserProducts.setEllipsize(TextUtils.TruncateAt.MARQUEE);
                            tvViewingUserProducts.setSingleLine(true);
                            tvViewingUserProducts.setSelected(true);
                            mbtnClose.setOnClickListener(v -> {
                                //startActivity(new Intent(getActivity(), MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK))
                                resetSearch();
                                performSearch(false);
                            });
                        }
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int status, Error error) {
                if (mRefresh == null || !isAdded()) {
                    return;
                }
                try {
                    cityId = -1;
                    categoryId = -1;
                    itemCondition = -1;
                    itemPriceRange = -1;
                    komodiName = "";
                    mRefresh.setRefreshing(false);
                    if (userInitiated) {
                        pDialog.setTitleText("اینی که می خوای رو نداریم")
                                //.setContentText(error.getMessage())
                                .setConfirmText("باشه")
                                .showCancelButton(false)
                                .setConfirmClickListener(Dialog::dismiss)
                                .setCancelClickListener(Dialog::dismiss)
                                .changeAlertType(SweetAlertDialog.ERROR_TYPE);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });
    }

    @Override
    public String getTitle() {
        return "آیتم ها";
    }

    @Override public void doSearch() {
        showSearchDialog();
    }

    public interface OnItemRemoveListener {
        void onItemRemove(int id);
    }

    public class ListAdapter extends RecyclerView.Adapter<ListAdapter.ViewHolder> {
        OnItemClickListener mItemClickListener;
        private List<Product> list;
        private Context context;
        private OnItemRemoveListener mItemRemoveListener;

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            context = parent.getContext();
            View v = LayoutInflater.from(context).inflate(R.layout.row_product, parent, false);
            return new ViewHolder(v);
        }

        void setData(List<Product> data, boolean append) {
            if (append && list != null) {
                list.addAll(data);
            } else {
                list = data;
            }
        }

        @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            Product p = list.get(position);

            holder.tvPrice.setText(String.format("%s تومان", Utils.formatNumber(p.getPrice())));

            String itemStatus = p.isHomemade() ? "کاردستی" : p.isBrandNew() ? "کاملاً نو" : "در حد نو";
            holder.tvNewStatus.setText(itemStatus);
            if (p.isFeatured()) {
                holder.tvFeatured.setVisibility(View.VISIBLE);
            } else {
                holder.tvFeatured.setVisibility(View.GONE);
            }
            if (p.getDiscountPercent() > 0) {
                holder.tvDiscount.setVisibility(View.VISIBLE);
                SpannableString span = new SpannableString(String.format(Locale.US, "%d%% Off", p.getDiscountPercent()));
                StyleSpan ssp = new StyleSpan(Typeface.BOLD);
                span.setSpan(new RelativeSizeSpan(1.4f), 0, (p.getDiscountPercent() + "").length() + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                span.setSpan(ssp, 0, (p.getDiscountPercent() + "").length() + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                holder.tvDiscount.setText(span);
            } else {
                holder.tvDiscount.setVisibility(View.GONE);
            }
            if (p.isHomemade()) {
                holder.tvNewStatus.setBackgroundColor(Utils.getColorFromResource(context, R.color.homemade_item));

            } else if (p.isBrandNew()) {
                holder.tvNewStatus.setBackgroundColor(Utils.getColorFromResource(context, R.color.new_item));
            } else {
                holder.tvNewStatus.setBackgroundColor(Utils.getColorFromResource(context, R.color.used_item));
            }
            holder.tvCommentsCount.setText(p.getCommentsCount() + "");
            Picasso.with(context).load(p.getCover().getPath()).fit().centerInside().placeholder(R.drawable.loading_main_activity).into(holder.imgProductImage, new Callback() {
                @Override
                public void onSuccess() {
                    if (!isAdded()) {
                        return;
                    }
                    Bitmap resource = ((BitmapDrawable) holder.imgProductImage.getDrawable()).getBitmap();
                    int intrinsicWidth = resource.getWidth();
                    int width = holder.imgProductImage.getWidth();
                    int intrinsicHeight = resource.getHeight();
                    int height = holder.imgProductImage.getHeight();
                    if ((intrinsicWidth < width) || intrinsicHeight < height) {
                        Log.d("Image Loaded", "onResourceReady: r.w = " + intrinsicWidth + ", i.w = " + width);
                        holder.imgProductImage.setBackground(new BitmapDrawable(getResources(), Blur.fastBlur(context, Utils.scaleImage(context, resource, 50), 25)));
                    }
                }

                @Override
                public void onError() {

                }
            });
        }

        @Override
        public int getItemCount() {
            return list == null ? 0 : list.size();
        }

        public void setOnItemClickListener(final OnItemClickListener itemClickListener) {
            this.mItemClickListener = itemClickListener;
        }

        public void setOnItemRemoveListener(final OnItemRemoveListener itemRemoveListener) {
            this.mItemRemoveListener = itemRemoveListener;
        }

        class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            @BindView(R.id.imgProductImage) ImageView imgProductImage;
            @BindView(R.id.tvPrice) TextView tvPrice;
            @BindView(R.id.tvComments) TextView tvCommentsCount;
            @BindView(R.id.tvNewStatus) TextView tvNewStatus;
            @BindView(R.id.tvFeatured) TextView tvFeatured;
            @BindView(R.id.tvDiscount) TextView tvDiscount;
            @BindView(R.id.fabPurchase) FloatingActionButton fab;

            ViewHolder(View itemView) {
                super(itemView);
                ButterKnife.bind(this, itemView);
                itemView.setOnClickListener(this);
                fab.setOnClickListener(this);
                imgProductImage.getLayoutParams().height = ((Activity) context).getWindowManager().getDefaultDisplay().getWidth();

            }

            @Override
            public void onClick(View v) {
                if (mItemClickListener != null) {
                    int adapterPosition = getAdapterPosition();
                    if (adapterPosition == RecyclerView.NO_POSITION) {
                        return;
                    }
                    mItemClickListener.onItemClick(v, adapterPosition, list.get(adapterPosition));
                }
            }
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
                convertView = getActivity().getLayoutInflater().inflate(android.R.layout.simple_spinner_dropdown_item, parent, false);
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
