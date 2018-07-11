package com.komodaa.app.models;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by nevercom on 2/16/18.
 */

public class Message implements Parcelable {
    public static final Parcelable.Creator<Message> CREATOR = new Parcelable.Creator<Message>() {
        @Override public Message createFromParcel(Parcel source) {
            return new Message(source);
        }

        @Override public Message[] newArray(int size) {
            return new Message[size];
        }
    };
    private boolean fromMe;
    private String message;
    private String sentTime;

    public Message(JSONObject json) {
        if (json == null) {
            return;
        }

        try {
            if (json.has("from_me")) {
                fromMe = json.getBoolean("from_me");
            }
            if (json.has("message")) {
                message = json.getString("message");
            }
            if (json.has("time")) {
                sentTime = json.getString("time");
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public Message(boolean fromMe, String message, String sentTime) {
        this.fromMe = fromMe;
        this.message = message;
        this.sentTime = sentTime;
    }

    protected Message(Parcel in) {
        this.fromMe = in.readByte() != 0;
        this.message = in.readString();
        this.sentTime = in.readString();
    }

    public boolean isFromMe() {
        return fromMe;
    }

    public String getMessage() {
        return message;
    }

    public String getSentTime() {
        return sentTime;
    }

    @Override public int describeContents() {
        return 0;
    }

    @Override public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte(this.fromMe ? (byte) 1 : (byte) 0);
        dest.writeString(this.message);
        dest.writeString(this.sentTime);
    }
}
