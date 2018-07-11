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
import com.komodaa.app.models.Conversation;
import com.komodaa.app.utils.Komovatar;
import com.komodaa.app.utils.Utils;
import com.pkmmte.view.CircularImageView;
import com.squareup.picasso.Picasso;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by nevercom on 2/16/18.
 */

public class ConversationsAdapter extends RecyclerView.Adapter<ConversationsAdapter.ViewHolder> {
    private final Komovatar komovatar;
    private List<Conversation> list;
    private Context context;
    private OnItemClickListener mItemClickListener;

    public ConversationsAdapter() {
        this.komovatar = new Komovatar();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        context = parent.getContext();
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.row_conversation_item, parent, false));
    }

    @Override public void onBindViewHolder(ViewHolder holder, int position) {
        Conversation c = list.get(position);

        holder.sender.setText(c.getUser().getFirstName());
        holder.lastMessage.setText(c.getLastMessage());
        holder.time.setText(Utils.getPersianChatTime(c.getTime()));
        String avatarUrl = c.getUser().getAvatarUrl();
        if (!TextUtils.isEmpty(avatarUrl) && avatarUrl.length() > 10) {
            Picasso.with(context).load(avatarUrl).placeholder(R.drawable.no_avatar).into(holder.imgAvatar);
        } else {
            holder.imgAvatar.setImageResource(komovatar.getOrderedAvatar(position));
        }

        if (c.getUnreadCount() > 0) {
            holder.badge.setText(c.getUnreadCount() + "");
            holder.badge.setVisibility(View.VISIBLE);
        } else {

            holder.badge.setVisibility(View.GONE);
        }
    }

    public void setData(List<Conversation> data, boolean append) {
        if (append && list != null) {
            list.addAll(data);
        } else {
            list = data;
        }
    }

    @Override public int getItemCount() {
        return list == null ? 0 : list.size();
    }

    public void setOnItemClickListener(final OnItemClickListener itemClickListener) {
        this.mItemClickListener = itemClickListener;
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        @BindView(R.id.imgAvatar) CircularImageView imgAvatar;
        @BindView(R.id.tvSenderName) TextView sender;
        @BindView(R.id.tvTime) TextView time;
        @BindView(R.id.tvLastMessage) TextView lastMessage;
        @BindView(R.id.tvNewMessagesCount) TextView badge;

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

}
