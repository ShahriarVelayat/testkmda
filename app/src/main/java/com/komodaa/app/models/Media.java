package com.komodaa.app.models;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by nevercom on 10/6/16.
 */

public class Media implements Parcelable {
    public static final Parcelable.Creator<Media> CREATOR = new Parcelable.Creator<Media>() {
        @Override public Media createFromParcel(Parcel source) {return new Media(source);}

        @Override public Media[] newArray(int size) {return new Media[size];}
    };
    private int id;
    private String path;
    private boolean cover;

    public Media() {
    }

    public Media(JSONObject json) {
        if (json == null) {
            return;
        }

        try {
            if (json.has("id")) {
                id = json.getInt("id");
            }
            if (json.has("path")) {
                path = json.getString("path");
            }
            if (json.has("is_cover")) {
                cover = json.getBoolean("is_cover");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    protected Media(Parcel in) {
        this.id = in.readInt();
        this.path = in.readString();
        this.cover = in.readByte() != 0;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public boolean isCover() {
        return cover;
    }

    public void setCover(boolean cover) {
        this.cover = cover;
    }

    @Override public int describeContents() { return 0; }

    @Override public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.id);
        dest.writeString(this.path);
        dest.writeByte(this.cover ? (byte) 1 : (byte) 0);
    }
}
