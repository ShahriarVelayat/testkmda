package com.komodaa.app.adapters;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.komodaa.app.R;
import com.komodaa.app.interfaces.OnItemClickListener;
import com.komodaa.app.utils.Komovatar;
import com.komodaa.app.utils.Utils;
import com.pkmmte.view.CircularImageView;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by nevercom on 5/26/17.
 */
public class UsersListAdapter extends RecyclerView.Adapter<UsersListAdapter.ViewHolder> {
    private List<JSONObject> list;
    private Context context;
    OnItemClickListener mItemClickListener;
    private Komovatar komovatar;

    public UsersListAdapter() {

        komovatar = new Komovatar();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        context = parent.getContext();

        View v = LayoutInflater.from(context).inflate(R.layout.row_followings_user, parent, false);
        return new ViewHolder(v);
    }


    public void setData(List<JSONObject> data, boolean append) {
        if (append && list != null) {
            list.addAll(data);
        } else {
            list = data;
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN) @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
/*            {
            "user_id": 11,
                "name": "حامد تکمیل",
                "avatar": "http://komodaa.com/api/images/avatar//11_58997aaf81b65.jpg",
                "items_count": 1
        }*/
        try {
            JSONObject j = list.get(position);
            holder.tvUserName.setText(j.getString("name"));
            if (j.has("items_count")) {
                holder.tvItemsCount.setVisibility(View.VISIBLE);
                holder.tvItemsCount.setText(String.format(Locale.US, "%s آیتم", Utils.formatNumber(j.getInt("items_count"))));
            } else {
                holder.tvItemsCount.setVisibility(View.INVISIBLE);
            }
            String avatarUrl = j.getString("avatar");
            if (!TextUtils.isEmpty(avatarUrl) && avatarUrl.length() > 10) {
                Picasso.with(context).load(avatarUrl).placeholder(R.drawable.no_avatar).into(holder.imgAvatar);
            } else {
                holder.imgAvatar.setImageResource(komovatar.getOrderedAvatar(position));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    @Override public int getItemCount() {
        return list == null ? 0 : list.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        @BindView(R.id.imgAvatar) CircularImageView imgAvatar;
        @BindView(R.id.tvUserName) TextView tvUserName;
        @BindView(R.id.tvItemsCount) TextView tvItemsCount;

        ViewHolder(View itemView) {
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
