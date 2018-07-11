package com.komodaa.app.models;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by nevercom on 10/6/16.
 */

public class Attribute implements Parcelable {

    public static final Creator<Attribute> CREATOR = new Creator<Attribute>() {
        @Override public Attribute createFromParcel(Parcel source) {return new Attribute(source);}

        @Override public Attribute[] newArray(int size) {return new Attribute[size];}
    };
    private int id;
    private String name;
    private String value;
    private String inputType;

    public Attribute() {}

    public Attribute(JSONObject json) {
        if (json == null) {
            return;
        }

        try {
            if (json.has("id")) {
                id = json.getInt("id");
            }
            if (json.has("name")) {
                name = json.getString("name");
            }
            if (json.has("value")) {
                value = json.getString("value");
            }
            if (json.has("input_type")) {
                inputType = json.getString("input_type");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    protected Attribute(Parcel in) {
        this.id = in.readInt();
        this.name = in.readString();
        this.value = in.readString();
        this.inputType = in.readString();
    }

    public String getInputType() {
        return inputType;
    }

    public void setInputType(String inputType) {
        this.inputType = inputType;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override public int describeContents() { return 0; }

    @Override public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.id);
        dest.writeString(this.name);
        dest.writeString(this.value);
        dest.writeString(this.inputType);
    }
}
