package com.komodaa.app.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.komodaa.app.R;
import com.komodaa.app.activities.UserProfileActivity;
import com.komodaa.app.interfaces.CommentListener;
import com.komodaa.app.interfaces.NetworkCallback;
import com.komodaa.app.models.Comment;
import com.komodaa.app.models.Error;
import com.komodaa.app.utils.ApiClient;
import com.komodaa.app.utils.Komovatar;
import com.komodaa.app.utils.UserUtils;
import com.komodaa.app.utils.Utils;
import com.pkmmte.view.CircularImageView;
import com.rey.material.widget.ProgressView;
import com.squareup.picasso.Picasso;
import com.yqritc.recyclerviewflexibledivider.HorizontalDividerItemDecoration;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Created by nevercom on 10/12/16.
 */

public class CommentsListFragment extends BaseFragment {
    long itemId;
    @BindView(R.id.rlProgress)
    RelativeLayout rlProgress;
    @BindView(R.id.progress)
    ProgressView progress;
    private Unbinder unbinder;
    private CommentsAdapter adapter;
    private CommentListener callback;

    public CommentsListFragment() {
    }

    @BindView(R.id.recList)
    RecyclerView recList;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_comments, container, false);
        unbinder = ButterKnife.bind(this, view);

        Bundle args = getArguments();
        itemId = args.getLong("item_id");
        return view;
    }

    @Override
    public String getTitle() {
        return "مشاهده نظرات";
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        adapter = new CommentsAdapter();

        LinearLayoutManager lManager = new LinearLayoutManager(getActivity());
        lManager.setOrientation(LinearLayoutManager.VERTICAL);
        recList.setLayoutManager(lManager);
        recList.setHasFixedSize(true);
        recList.addItemDecoration(new HorizontalDividerItemDecoration.Builder(getActivity()).margin(Utils.dpToPx(getActivity(), 16)).build());
        recList.setAdapter(adapter);
        recList.setOnTouchListener((v, event) -> {
            v.getParent().requestDisallowInterceptTouchEvent(true);
            return false;
        });
        reload(false);
    }

    public static List<Comment> toThreadedComments(List<Comment> comments) {

        //comments should be sorted by date first

        //The resulting array of threaded comments
        List<Comment> threaded = new ArrayList<>();

        //An array used to hold processed comments which should be removed at the end of the cycle
        List<Comment> removeComments = new ArrayList<>();

        //get the root comments first (comments with no parent)
        for (int i = 0; i < comments.size(); i++) {
            Comment c = comments.get(i);
            if (c.getParentId() == 0) {
                c.setCommentDepth(0); //A property of Comment to hold its depth
                c.setChildCount(0); //A property of Comment to hold its child count
                threaded.add(c);
                removeComments.add(c);
            }
        }

        if (removeComments.size() > 0) {
            //clear processed comments
            comments.removeAll(removeComments);
            removeComments.clear();
        }

        int depth = 0;
        //get the child comments up to a max depth of 10
        while (comments.size() > 0 && depth <= 10) {
            depth++;
            for (int j = 0; j < comments.size(); j++) {
                Comment child = comments.get(j);
                //check root comments for match
                for (int i = 0; i < threaded.size(); i++) {
                    Comment parent = threaded.get(i);
                    if (parent.getId() == child.getParentId()) {
                        parent.setChildCount(parent.getChildCount() + 1);
                        child.setCommentDepth(depth + parent.getCommentDepth());
                        threaded.add(i + parent.getChildCount(), child);
                        removeComments.add(child);
                        continue;
                    }
                }
            }
            if (removeComments.size() > 0) {
                //clear processed comments
                comments.removeAll(removeComments);
                removeComments.clear();
            }
        }
        int index = 0;
        for (Comment c : threaded) {
            Log.d("ThreadedComment", String.format(Locale.US, "index: %d, id: %d, ParentId: %d, ChildsCount: %d, Depth: %d", index, c.getId(), c.getParentId(), c.getChildCount(), c.getCommentDepth()));
            index++;
        }
        return threaded;
    }

    public void reload(final boolean newComment) {
        if (!isAdded()) {
            return;
        }
        rlProgress.setVisibility(View.VISIBLE);
        progress.setVisibility(View.VISIBLE);
        ApiClient.makeRequest(getActivity(), "GET", "/comment/list/" + itemId, null, new NetworkCallback() {
            @Override
            public void onSuccess(int status, JSONObject data) {
                if (recList == null || !isAdded()) {
                    return;
                }
                try {
                    rlProgress.setVisibility(View.GONE);
                    progress.setVisibility(View.GONE);
                    int totalPages = data.getInt("total_pages");
                    JSONArray array = data.getJSONArray("data");
                    List<Comment> list = new ArrayList<>();
                    for (int i = 0; i < array.length(); i++) {
                        list.add(new Comment(array.getJSONObject(i)));
                    }

                    //toThreadedComments(list);
                    adapter.addData(list, false);
                    adapter.notifyDataSetChanged();
                    if (recList.getAdapter() == null) {
                        recList.setAdapter(adapter);
                    }
                    if (newComment) {
                        recList.smoothScrollToPosition(adapter.getItemCount() - 1);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int status, Error error) {
                if (!isAdded()) {
                    return;
                }
                if (rlProgress != null) {
                    rlProgress.setVisibility(View.GONE);
                    progress.setVisibility(View.GONE);
                }
            }
        });
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            callback = (CommentListener) context;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    class CommentsAdapter extends RecyclerView.Adapter<CommentsAdapter.ViewHolder> {
        private final Komovatar komovatar;
        List<Comment> list;
        private Context context;

        public CommentsAdapter() {

            komovatar = new Komovatar();
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
            View v = LayoutInflater.from(context).inflate(R.layout.row_comment, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {

            final Comment c = list.get(position);
            holder.imgReply.setOnClickListener(v -> {
                Log.d("", "onClick: ");
                if (callback != null) {
                    callback.replyTo(c.getId(), c.getUserName());
                }
            });
            holder.imgAvatar.setOnClickListener(v -> {
                if (UserUtils.isLoggedIn()) {
                    startActivity(new Intent(getActivity(), UserProfileActivity.class).putExtra("user_id", c.getUserId()));
                }
            });
            holder.tvCommentText.setText(c.getText());
            if (c.getParentId() > 0 && c.getTo() != null) {
                holder.tvCommentRepliedTo.setText(String.format("در پاسخ به %s", c.getTo().getFirstName()));
            } else {
                holder.tvCommentRepliedTo.setText("");
            }
            holder.tvCommentTitle.setText(c.getUserName());
            String avatarUrl = c.getAvatarUrl();

            if (!TextUtils.isEmpty(avatarUrl) && avatarUrl.length() > 10) {
//                int width = holder.imgAvatar.getWidth();
//                if (width < 1) {
//                    width = 200;
//                }
//                Log.d("Avatar", "size: " + width);
//                avatarUrl = Gravatar.init().with(c.getEmail()).force404().size(width).build();

                Picasso.with(context)
                        .load(avatarUrl)
                        .placeholder(R.drawable.no_avatar)
                        .error(R.drawable.no_avatar)
                        .fit().centerInside()
                        .into(holder.imgAvatar);
            } else {
                holder.imgAvatar.setImageResource(komovatar.getOrderedAvatar((int) c.getUserId()));
            }


        }

        @Override
        public int getItemCount() {
            return list == null ? 0 : list.size();
        }


        class ViewHolder extends RecyclerView.ViewHolder {
            @BindView(R.id.imgAvatar) CircularImageView imgAvatar;
            @BindView(R.id.imgReply) ImageView imgReply;
            @BindView(R.id.tvCommentTitle) TextView tvCommentTitle;
            @BindView(R.id.tvCommentRepliedTo) TextView tvCommentRepliedTo;
            @BindView(R.id.tvCommentText) TextView tvCommentText;

            ViewHolder(View itemView) {
                super(itemView);
                ButterKnife.bind(this, itemView);
//                imgReply.setOnClickListener(new View.OnClickListener() {
//                    @Override public void onClick(View v) {
//                        Log.d("", "onClick: "+(callback!= null?"notNull":"null"));
//                        if (callback != null) {
//
//                            final Comment c = list.get(getAdapterPosition());
//                        callback.replyTo(c.getUserId(), c.getUserName());
//                    }
//                    }
//                });
            }
        }
    }

}
