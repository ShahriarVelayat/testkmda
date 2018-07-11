package com.komodaa.app.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.komodaa.app.App;
import com.komodaa.app.R;
import com.komodaa.app.activities.BookmarksActivity;
import com.komodaa.app.activities.ConversationsActivity;
import com.komodaa.app.activities.FinancialReportActivity;
import com.komodaa.app.activities.FollowingsActivity;
import com.komodaa.app.activities.HelpActivity;
import com.komodaa.app.activities.ProfileActivity;
import com.komodaa.app.utils.Komovatar;
import com.komodaa.app.utils.Utils;
import com.pkmmte.view.CircularImageView;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * Created by nevercom on 2/21/18.
 */

public class MyProfileFragment extends BaseFragment {

    @BindView(R.id.imgAvatar) CircularImageView imgAvatar;
    @BindView(R.id.tvName) TextView tvName;
    @BindView(R.id.tvBalance) TextView tvBalance;
    @BindView(R.id.tvFollowersCount) TextView tvFollowersCount;
    @BindView(R.id.tvFollowingsCount) TextView tvFollowingsCount;
    //@BindView(R.id.tvBadgeChats) TextView badge;
    @BindView(R.id.imgVerified) ImageView imgVerified;
    @BindView(R.id.tvPrivateMessages) TextView tvPrivateMessages;
    private Unbinder unbinder;

    @Nullable @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_my_profile, container, false);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @Override public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        String meta = App.getPrefs().getString("user_meta", null);
        if (TextUtils.isEmpty(meta)) {
            return;
        }
        try {
            JSONObject data = new JSONObject(meta);
            if (data == null || !data.has("me")) {
                return;
            }
            JSONObject myInfo = data.getJSONObject("me");
            String avatarUrl = myInfo.getString("avatar");
            if (!TextUtils.isEmpty(avatarUrl) && avatarUrl.length() > 10) {

                Picasso.with(getActivity())
                        .load(avatarUrl)
                        .placeholder(R.drawable.ic_account_circle_48px)
                        .error(R.drawable.ic_account_circle_48px)
                        .fit().centerInside()
                        .into(imgAvatar);
            } else {
                Picasso.with(getActivity())
                        .load(Komovatar.getRandomAvatar())
                        .placeholder(R.drawable.ic_account_circle_48px)
                        .error(R.drawable.ic_account_circle_48px)
                        .fit().centerInside()
                        .into(imgAvatar);
            }
//            if (data.has("unread_chat_count")) {
//                try {
//                    int unreadCount = data.getInt("unread_chat_count");
//                    if (unreadCount > 0) {
//                        badge.setText(unreadCount + "");
//                        badge.setVisibility(View.VISIBLE);
//                    } else {
//                        badge.setVisibility(View.GONE);
//                    }
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//            }
            tvName.setText(myInfo.getString("name"));
            tvBalance.setText(String.format("%s تومان", Utils.formatNumber(myInfo.getInt("balance"))));
            tvFollowersCount.setText(myInfo.getString("followers_count"));
            tvFollowingsCount.setText(myInfo.getString("following_count"));
            imgVerified.setVisibility(myInfo.getBoolean("is_verified") ? View.VISIBLE : View.GONE);
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    @OnClick({R.id.tvEditProfile, R.id.tvBookmarks, R.id.tvFinancialReport, R.id.tvFollowingsList, R.id.tvVerifyAccount, R.id.tvKomoTour, R.id.tvPrivateMessages})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.tvEditProfile:
                startActivity(new Intent(getActivity(), ProfileActivity.class));
                break;
            case R.id.tvBookmarks:
                startActivity(new Intent(getActivity(), BookmarksActivity.class));
                break;
            case R.id.tvFinancialReport:

                startActivity(new Intent(getActivity(), FinancialReportActivity.class));
                break;
            case R.id.tvFollowingsList:
                startActivity(new Intent(getActivity(), FollowingsActivity.class));

                break;
            case R.id.tvVerifyAccount:
                startActivity(new Intent(getActivity(), HelpActivity.class).putExtra("url", "https://komodaa.com/verification").putExtra("title", "تایید حساب کاربری"));

                break;
            case R.id.tvKomoTour:
                startActivity(new Intent(getActivity(), HelpActivity.class).putExtra("url", "https://komodaa.com/help").putExtra("title", "آموزش کار با کمدا"));

                break;
            case R.id.tvPrivateMessages:
                startActivity(new Intent(getActivity(), ConversationsActivity.class));
                //badge.setVisibility(View.GONE);

                break;
        }
    }
}
