
package com.komodaa.app.models;

import android.os.Parcel;
import android.os.Parcelable;

public class Error implements Parcelable {
    private int errorCode;
    private String message;

    public int getErrorCode() {
        return errorCode;
    }

    public String getMessage() {
        return message;
    }

    public static final Creator<Error> CREATOR = new Creator<Error>() {
        public Error createFromParcel(Parcel source) {
            return new Error(source);
        }

        public Error[] newArray(int size) {
            return new Error[size];
        }
    };

    protected Error(Parcel in) {
        this.errorCode = in.readInt();
        this.message = in.readString();
    }

    public Error(int errorCode, String message) {
        this.errorCode = errorCode;
        this.message = message;
    }

    @Override
    public int describeContents() {

        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.errorCode);
        dest.writeString(this.message);
    }
}
