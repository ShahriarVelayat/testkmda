package com.komodaa.app.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.komodaa.app.R;
import com.komodaa.app.models.Message;
import com.komodaa.app.utils.Utils;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by nevercom on 2/16/18.
 */

public class PrivateMessagesAdapter extends RecyclerView.Adapter<PrivateMessagesAdapter.ViewHolder> {
    static final int SENT = 1;
    static final int RECEIVED = 2;
    private List<Message> list;
    private Context context;

    @Override public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        context = parent.getContext();
        if (viewType == SENT) {
            return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.row_chat_out, parent, false));
        } else {
            return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.row_chat_in, parent, false));
        }
    }

    @Override public void onBindViewHolder(ViewHolder holder, int position) {
        Message m = list.get(position);
        holder.message.setText(m.getMessage());
        holder.time.setText(Utils.getPersianChatTime(m.getSentTime()));
    }

    public void setData(List<Message> data, boolean append) {
        if (append && list != null) {
            list.addAll(data);
        } else {
            list = data;
        }
    }

    @Override public int getItemViewType(int position) {
        return list.get(position).isFromMe() ? SENT : RECEIVED;
    }

    @Override public int getItemCount() {
        return list == null ? 0 : list.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.tvMessage) TextView message;
        @BindView(R.id.tvTime) TextView time;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
