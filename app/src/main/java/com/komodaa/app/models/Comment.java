package com.komodaa.app.models;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by nevercom on 10/12/16.
 */

public class Comment implements Parcelable {
    public static final Creator<Comment> CREATOR = new Creator<Comment>() {
        @Override
        public Comment createFromParcel(Parcel source) {
            return new Comment(source);
        }

        @Override
        public Comment[] newArray(int size) {
            return new Comment[size];
        }
    };
    private long id;
    private long userId;
    private long itemId;
    private long parentId;
    private int section;
    private String text;
    private String date;
    private String userName;
    private String avatarUrl;
    private String email;
    private User to;
    private int childCount;
    private int commentDepth;
    private boolean lastChild = false;

    public Comment() {
    }

    public Comment(JSONObject json) {
        if (json == null) {
            return;
        }
        try {
            id = json.getLong("comment_id");
            userId = json.getLong("user_id");
            itemId = json.getLong("item_id");
            parentId = json.getLong("parent");
            section = json.getInt("section");
            text = json.getString("comment");
            date = json.getString("date");
            userName = json.getString("user_name");
            avatarUrl = json.getString("avatar_url");
            if (json.has("email")) {
                email = json.getString("email");
            }
            if (parentId > 0 && json.has("to_user")) {
                to = new User(json.getJSONObject("to_user"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    protected Comment(Parcel in) {
        this.id = in.readLong();
        this.userId = in.readLong();
        this.itemId = in.readLong();
        this.parentId = in.readLong();
        this.section = in.readInt();
        this.text = in.readString();
        this.date = in.readString();
        this.userName = in.readString();
        this.avatarUrl = in.readString();
        this.email = in.readString();
        this.to = in.readParcelable(User.class.getClassLoader());
        this.childCount = in.readInt();
        this.commentDepth = in.readInt();
        this.lastChild = in.readByte() != 0;
    }

    public int getChildCount() {
        return childCount;
    }

    public void setChildCount(int childCount) {
        this.childCount = childCount;
    }

    public int getCommentDepth() {
        return commentDepth;
    }

    public void setCommentDepth(int commentDepth) {
        this.commentDepth = commentDepth;
    }

    public boolean isLastChild() {
        return lastChild;
    }

    public void setLastChild(boolean lastChild) {
        this.lastChild = lastChild;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public User getTo() {
        return to;
    }

    public void setTo(User to) {
        this.to = to;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public long getItemId() {
        return itemId;
    }

    public void setItemId(long itemId) {
        this.itemId = itemId;
    }

    public long getParentId() {
        return parentId;
    }

    public void setParentId(long parentId) {
        this.parentId = parentId;
    }

    public int getSection() {
        return section;
    }

    public void setSection(int section) {
        this.section = section;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.id);
        dest.writeLong(this.userId);
        dest.writeLong(this.itemId);
        dest.writeLong(this.parentId);
        dest.writeInt(this.section);
        dest.writeString(this.text);
        dest.writeString(this.date);
        dest.writeString(this.userName);
        dest.writeString(this.avatarUrl);
        dest.writeString(this.email);
        dest.writeParcelable(this.to, flags);
        dest.writeInt(this.childCount);
        dest.writeInt(this.commentDepth);
        dest.writeByte(this.lastChild ? (byte) 1 : (byte) 0);
    }
}
