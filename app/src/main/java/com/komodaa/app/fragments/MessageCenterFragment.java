package com.komodaa.app.fragments;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
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
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.komodaa.app.R;
import com.komodaa.app.activities.ProductDetailsActivity;
import com.komodaa.app.interfaces.NetworkCallback;
import com.komodaa.app.models.Comment;
import com.komodaa.app.models.Error;
import com.komodaa.app.models.Product;
import com.komodaa.app.utils.ApiClient;
import com.komodaa.app.utils.UserUtils;
import com.komodaa.app.utils.Utils;
import com.pkmmte.view.CircularImageView;
import com.squareup.picasso.Picasso;
import com.yqritc.recyclerviewflexibledivider.HorizontalDividerItemDecoration;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import fr.tkeunebr.gravatar.Gravatar;

/**
 * Created by nevercom on 1/25/17.
 */

public class MessageCenterFragment extends BaseFragment {
    @BindView(R.id.recList) RecyclerView recList;
    private Unbinder unbinder;
    private MessagesAdapter adapter;


    @Nullable @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {


        View view = inflater.inflate(R.layout.fragment_recycler_view, container, false);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        LinearLayoutManager lManager = new LinearLayoutManager(getActivity());
        lManager.setOrientation(LinearLayoutManager.VERTICAL);
        recList.setLayoutManager(lManager);
        recList.setHasFixedSize(true);
        recList.addItemDecoration(new HorizontalDividerItemDecoration.Builder(getActivity()).size(Utils.dpToPx(getActivity(), 8)).color(Utils.getColorFromResource(getActivity(), android.R.color.transparent)).build());


        adapter = new MessagesAdapter();
        recList.setAdapter(adapter);
        adapter.setOnItemClickListener((view1, position, comment) -> {
            if (comment == null) {
                return;
            }
            long productId = 0;
            if (comment.getSection() == 3) {
                try {
                    productId = Long.parseLong(comment.getText());
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            } else {
                productId = comment.getItemId();
            }
            if (productId < 1) {
                return;
            }
            final ProgressDialog pd = new ProgressDialog(getActivity());
            pd.setIndeterminate(true);
            pd.setMessage("لطفاً منتظر بمانید");
            pd.show();
            final Handler h = new Handler(getActivity().getMainLooper());
            ApiClient.makeRequest(getActivity(), "GET", "/product/" + productId, null, new NetworkCallback() {
                @Override public void onSuccess(int status, final JSONObject data) {
                    h.post(() -> {
                        pd.dismiss();
                        if (data == null) {
                            return;
                        }
                        try {
                            Product p = new Product(data.getJSONObject("data"));
                            Intent intent = new Intent(getActivity(), ProductDetailsActivity.class);
                            intent.putExtra("item", p);
                            if (comment.getSection() != 4) {
                                intent.putExtra("from_messages", true);
                            }
                            startActivity(intent);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    });
                }

                @Override public void onFailure(int status, Error error) {
                    h.post(pd::dismiss);
                }
            });

        });
    }

    @Override public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    public void refreshData(List<Comment> list) {
        if (adapter == null || list == null || !isAdded()) {
            return;
        }
        adapter.addData(list, false);
        adapter.notifyDataSetChanged();
    }

    public class MessagesAdapter extends RecyclerView.Adapter<MessagesAdapter.ViewHolder> {
        Context context;
        List<Comment> list;
        private OnItemClickListener mItemClickListener;


        public MessagesAdapter(List<Comment> list) {
            this.list = list;
        }

        public MessagesAdapter() {
        }

        public void addData(List<Comment> data, boolean append) {
            if (append && list != null) {
                list.addAll(data);
            } else {
                list = data;
            }
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            context = parent.getContext();
            return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.row_message, parent, false));
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            Comment c = list.get(position);
            holder.tvDate.setText(Utils.getPersianDateText(c.getDate(), false));

            String avatarUrl = c.getAvatarUrl();

            if (TextUtils.isEmpty(avatarUrl) || avatarUrl.length() < 10) {
                int width = holder.imgAvatar.getWidth();
                if (width < 1) {
                    width = 200;
                }
                Log.d("Avatar", "size: " + width);
                avatarUrl = Gravatar.init().with(c.getEmail()).force404().size(width).build();
            }

            Picasso.with(context)
                    .load(avatarUrl)
                    .placeholder(R.drawable.no_avatar)
                    .error(R.drawable.no_avatar)
                    .fit().centerInside()
                    .into(holder.imgAvatar);
            String title = "";
            String text = "";
            if (c.getSection() == 1) {
                holder.rlTitleWrapper.setBackgroundColor(Utils.getColorFromResource(getActivity(), R.color.colorPrimary));
                title = c.getUserName();
                if (c.getTo() != null && c.getTo().getId() == UserUtils.getUser().getId()) {
                    title += " به نظر شما پاسخ داده است.";
                } else {
                    title += " برای آیتم شما نظری ارسال کرده است.";
                }
                text = c.getText();
            } else if (c.getSection() == 3) {
                holder.rlTitleWrapper.setBackgroundColor(Utils.getColorFromResource(getActivity(), R.color.colorAccent));
                title = c.getUserName() + " یه پیشنهاد برات داره.";
                text = "به آیتم پیشنهادی " + c.getUserName() + " سر بزن.";
            } else if (c.getSection() == 4) {
                holder.rlTitleWrapper.setBackgroundColor(Utils.getColorFromResource(getActivity(), R.color.btn_bg));
                title = "هورا! آیتم ات فروخته شد.";
                text = c.getText();
            } else if (c.getSection() == 5) {
                holder.rlTitleWrapper.setBackgroundColor(Utils.getColorFromResource(getActivity(), R.color.notification_error));
                title = "متاسفانه آیتم شما قابل نمایش در کمدا نیست.";
                text = c.getText();
            }
            holder.tvTitle.setText(title);
            holder.tvCommentText.setText(text);
        }

        @Override public int getItemCount() {
            return list != null ? list.size() : 0;
        }

        public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            @BindView(R.id.tvDate) TextView tvDate;
            @BindView(R.id.tvTitle) TextView tvTitle;
            @BindView(R.id.rlTitleWrapper) RelativeLayout rlTitleWrapper;
            @BindView(R.id.imgAvatar) CircularImageView imgAvatar;
            @BindView(R.id.tvCommentText) TextView tvCommentText;

            public ViewHolder(View itemView) {
                super(itemView);
                ButterKnife.bind(this, itemView);
                itemView.setOnClickListener(this);
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

    public interface OnItemClickListener {
        void onItemClick(View view, int position, Comment data);
    }
}
