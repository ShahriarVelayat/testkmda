package com.komodaa.app.models;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by nevercom on 10/12/16.
 */

public class User implements Parcelable {
    public static final Creator<User> CREATOR = new Creator<User>() {
        @Override public User createFromParcel(Parcel source) {
            return new User(source);
        }

        @Override public User[] newArray(int size) {
            return new User[size];
        }
    };
    private JSONObject json;
    private long id;
    private String firstName;
    private String lastName;
    private int cityId;
    private String phoneNumber;
    private String email;
    private String avatarUrl;
    private int accountStatus;
    private boolean isVerified;

    public User() {}

    public User(String jsonString) {
        if (TextUtils.isEmpty(jsonString)) {
            return;
        }
        try {
            JSONObject json = new JSONObject(jsonString);
            initFromJson(json);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public User(JSONObject json) {
        initFromJson(json);
    }

    protected User(Parcel in) {
        this.id = in.readLong();
        this.firstName = in.readString();
        this.lastName = in.readString();
        this.cityId = in.readInt();
        this.phoneNumber = in.readString();
        this.email = in.readString();
        this.avatarUrl = in.readString();
        this.accountStatus = in.readInt();
        this.isVerified = in.readByte() != 0;
    }

    private void initFromJson(JSONObject json) {
        if (json == null) {
            return;
        }
        this.json = json;
        try {
            if (json.has("user_id")) {
                id = json.getLong("user_id");
            }
            if (json.has("first_name")) {
                firstName = json.getString("first_name");
            }
            if (json.has("last_name")) {
                lastName = json.getString("last_name");
            }
            if (json.has("city")) {
                cityId = json.getInt("city");
            }
            if (json.has("email")) {
                email = json.getString("email");
            }
            if (json.has("cell_number")) {
                phoneNumber = json.getString("cell_number");
            }
            if (json.has("avatar")) {
                avatarUrl = json.getString("avatar");
            }
            if (json.has("status")) {
                accountStatus = json.getInt("status");
            }
            if (json.has("is_verified")) {
                isVerified = json.getBoolean("is_verified");
            }


        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public boolean isVerified() {
        return isVerified;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public int getCityId() {
        return cityId;
    }

    public void setCityId(int cityId) {
        this.cityId = cityId;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public int getAccountStatus() {
        return accountStatus;
    }

    public void setAccountStatus(int accountStatus) {
        this.accountStatus = accountStatus;
    }

    public String getJsonString() {
        if (json == null) {
            return null;
        }
        return json.toString();
    }

    @Override public int describeContents() {
        return 0;
    }

    @Override public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.id);
        dest.writeString(this.firstName);
        dest.writeString(this.lastName);
        dest.writeInt(this.cityId);
        dest.writeString(this.phoneNumber);
        dest.writeString(this.email);
        dest.writeString(this.avatarUrl);
        dest.writeInt(this.accountStatus);
        dest.writeByte(this.isVerified ? (byte) 1 : (byte) 0);
    }
}
