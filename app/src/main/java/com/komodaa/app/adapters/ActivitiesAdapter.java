package com.komodaa.app.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.komodaa.app.R;
import com.komodaa.app.interfaces.OnItemClickListener;
import com.komodaa.app.models.UserActivity;
import com.komodaa.app.utils.Komovatar;
import com.komodaa.app.utils.Utils;
import com.pkmmte.view.CircularImageView;
import com.squareup.picasso.Picasso;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by nevercom on 2/11/18.
 */

public class ActivitiesAdapter extends RecyclerView.Adapter<ActivitiesAdapter.ViewHolder> {
    private final Komovatar komovatar;
    private List<UserActivity> list;
    private OnItemClickListener mItemClickListener;
    private Context context;

    public ActivitiesAdapter() {
        komovatar = new Komovatar();
    }

    @Override
    public ActivitiesAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        context = parent.getContext();
        View v = LayoutInflater.from(context).inflate(R.layout.row_activity, parent, false);
        return new ViewHolder(v);
    }

    @Override public void onBindViewHolder(ActivitiesAdapter.ViewHolder holder, int position) {
        UserActivity activity = list.get(position);
        holder.text.setText(activity.getActivity());
        holder.time.setText(Utils.getPersianDate(activity.getTime(), Utils.MODE_RELATIVE));
        String avatarUrl = activity.getUser().getAvatarUrl();
        if (!TextUtils.isEmpty(avatarUrl) && avatarUrl.length() > 10) {
            Picasso.with(context).load(avatarUrl).placeholder(R.drawable.no_avatar).into(holder.imgAvatar);
        } else {
            holder.imgAvatar.setImageResource(komovatar.getOrderedAvatar(position));
        }

    }

    public void setData(List<UserActivity> data, boolean append) {
        if (append && list != null) {
            list.addAll(data);
        } else {
            list = data;
        }
    }

    @Override public int getItemCount() {
        return list == null ? 0 : list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        @BindView(R.id.imgAvatar) CircularImageView imgAvatar;
        @BindView(R.id.tvText) TextView text;
        @BindView(R.id.tvTime) TextView time;

        ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(this);
            imgAvatar.setOnClickListener(this);
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
