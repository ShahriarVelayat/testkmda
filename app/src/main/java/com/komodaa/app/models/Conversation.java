package com.komodaa.app.models;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by nevercom on 2/16/18.
 */

public class Conversation implements Parcelable {
    private User user;
    private String lastMessage;
    private String time;
    private int unreadCount;

    public Conversation() {
    }

    public Conversation(JSONObject json) {
        if (json == null) {
            return;
        }

        try {
            if (json.has("user")) {
                user = new User(json.getJSONObject("user"));
            }
            if (json.has("last_message")) {
                lastMessage = json.getString("last_message");
            }
            if (json.has("time")) {
                time = json.getString("time");
            }
            if (json.has("unread_count")) {
                unreadCount = json.getInt("unread_count");
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public User getUser() {
        return user;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public String getTime() {
        return time;
    }

    public int getUnreadCount() {
        return unreadCount;
    }

    @Override public int describeContents() {
        return 0;
    }

    @Override public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(this.user, flags);
        dest.writeString(this.lastMessage);
        dest.writeString(this.time);
        dest.writeInt(this.unreadCount);
    }

    protected Conversation(Parcel in) {
        this.user = in.readParcelable(User.class.getClassLoader());
        this.lastMessage = in.readString();
        this.time = in.readString();
        this.unreadCount = in.readInt();
    }

    public static final Creator<Conversation> CREATOR = new Creator<Conversation>() {
        @Override public Conversation createFromParcel(Parcel source) {
            return new Conversation(source);
        }

        @Override public Conversation[] newArray(int size) {
            return new Conversation[size];
        }
    };
}
