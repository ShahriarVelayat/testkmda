package com.komodaa.app.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.komodaa.app.R;
import com.komodaa.app.models.NavigationItem;

import java.util.List;

/**
 * Created by nevercom on 1/31/18.
 */

public class NavigationItemsAdapter extends BaseAdapter {
    private List<NavigationItem> list;

    public NavigationItemsAdapter(List<NavigationItem> list) {
        this.list = list;
    }

    @Override public int getCount() {
        return list != null ? list.size() : 0;
    }

    @Override public NavigationItem getItem(int position) {
        return list != null ? list.get(position) : null;
    }

    @Override public long getItemId(int position) {
        return list != null ? list.get(position).getId() : 0;
    }

    @Override public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder vh;
        if (convertView == null) {
            vh = new ViewHolder();
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_nav_item, parent, false);
            vh.icon = convertView.findViewById(R.id.imgIcon);
            vh.text = convertView.findViewById(R.id.tvCaption);
            convertView.setTag(vh);
        } else {
            vh = (ViewHolder) convertView.getTag();
        }

        NavigationItem nav = list.get(position);

        vh.icon.setImageResource(nav.getIcon());
        vh.text.setText(nav.getTitle());

        return convertView;
    }

    class ViewHolder {
        private ImageView icon;
        private TextView text;
    }
}
