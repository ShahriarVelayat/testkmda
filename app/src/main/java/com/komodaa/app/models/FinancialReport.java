package com.komodaa.app.models;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by nevercom on 1/24/17.
 */

public class FinancialReport implements Parcelable {
    public static final Creator<FinancialReport> CREATOR = new Creator<FinancialReport>() {
        @Override
        public FinancialReport createFromParcel(Parcel source) {return new FinancialReport(source);}

        @Override public FinancialReport[] newArray(int size) {return new FinancialReport[size];}
    };
    private int id;
    private int amount;
    private String date;
    private int productId;
    private String statusText;
    private int statusCode;

    public FinancialReport() {
    }

    public FinancialReport(JSONObject json) {
        if (json == null) {
            return;
        }

        try {
            if (json.has("id")) {
                id = json.getInt("id");
            }
            if (json.has("product_id")) {
                productId = json.getInt("product_id");
            }
            if (json.has("amount")) {
                amount = json.getInt("amount");
            }
            if (json.has("date")) {
                date = json.getString("date");
            }
            if (json.has("status_text")) {
                statusText = json.getString("status_text");
            }
            if (json.has("status_code")) {
                statusCode = json.getInt("status_code");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    protected FinancialReport(Parcel in) {
        this.id = in.readInt();
        this.amount = in.readInt();
        this.date = in.readString();
        this.productId = in.readInt();
        this.statusText = in.readString();
        this.statusCode = in.readInt();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public String getStatusText() {
        return statusText;
    }

    public void setStatusText(String statusText) {
        this.statusText = statusText;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    @Override public int describeContents() { return 0; }

    @Override public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.id);
        dest.writeInt(this.amount);
        dest.writeString(this.date);
        dest.writeInt(this.productId);
        dest.writeString(this.statusText);
        dest.writeInt(this.statusCode);
    }
}
