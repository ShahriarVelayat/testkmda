package com.komodaa.app.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by nevercom on 10/6/16.
 */

public class Category implements Parcelable {
    private int id;
    private String name;
    private List<Attribute> attribs;

    public List<Attribute> getAttributes() {
        return attribs;
    }

    public void setAttributes(List<Attribute> attribs) {
        this.attribs = attribs;
    }

    public void addAttribute(Attribute attrib) {
        if (attribs == null) {
            attribs = new ArrayList<>();
        }
        attribs.add(attrib);
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
        dest.writeTypedList(this.attribs);
    }

    public Category() {}

    protected Category(Parcel in) {
        this.id = in.readInt();
        this.name = in.readString();
        this.attribs = in.createTypedArrayList(Attribute.CREATOR);
    }

    public static final Parcelable.Creator<Category> CREATOR = new Parcelable.Creator<Category>() {
        @Override public Category createFromParcel(Parcel source) {return new Category(source);}

        @Override public Category[] newArray(int size) {return new Category[size];}
    };
}
