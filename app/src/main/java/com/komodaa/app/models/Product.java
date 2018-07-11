package com.komodaa.app.models;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by nevercom on 8/23/16.
 */
public class Product implements Parcelable {

    private String buyerName;
    private String sellDate;
    private int buyerId;
    private int totalCost;
    private int id;
    private int userId;
    private int categoryId;
    private int cityId;
    private boolean brandNew;
    private boolean homemade;
    private boolean featured;
    private String description;
    private int price;
    private String date;
    private int status;
    private List<Media> media;
    private List<Attribute> attributes;
    private int commentsCount;
    private int paymentId;
    private User user;
    private int discountPercent;
    private boolean rated;

    public Product() {
    }

    public Product(JSONObject json) {
        if (json == null) {
            return;
        }

        try {
            if (json.has("product_id")) {
                id = json.getInt("product_id");
            }
            if (json.has("user_id")) {
                userId = json.getInt("user_id");
            }

            if (json.has("category_id")) {
                categoryId = json.getInt("category_id");
            }
            if (json.has("city_id")) {
                cityId = json.getInt("city_id");
            }
            if (json.has("new")) {
                brandNew = json.getInt("new") > 0;
            }
            if (json.has("is_homemade")) {
                homemade = json.getInt("is_homemade") > 0;
            }
            if (json.has("featured")) {
                featured = json.getInt("featured") > 0;
            }
            if (json.has("description")) {
                description = json.getString("description");
            }
            if (json.has("price")) {
                price = json.getInt("price");
            }
            if (json.has("date_added")) {
                date = json.getString("date_added");
            }
            if (json.has("status")) {
                status = json.getInt("status");
            }

            if (json.has("media")) {
                JSONArray medias = json.getJSONArray("media");
                List<Media> mList = new ArrayList<>();
                for (int j = 0; j < medias.length(); j++) {
                    mList.add(new Media(medias.getJSONObject(j)));
                }

                media = mList;
            }


            if (json.has("attributes")) {
                JSONArray attribs = json.getJSONArray("attributes");
                for (int i = 0; i < attribs.length(); i++) {
                    addAttribute(new Attribute(attribs.getJSONObject(i)));
                }
            }

            if (json.has("payment_id")) {
                paymentId = json.getInt("payment_id");
            }

            if (json.has("total_cost")) {
                totalCost = json.getInt("total_cost");
            } else {
                totalCost = price;
            }
            if (json.has("comments_count")) {
                commentsCount = json.getInt("comments_count");
            }
            if (json.has("user")) {
                user = new User(json.getJSONObject("user"));
            }
            if (json.has("buyer_id")) {
                buyerId = json.getInt("buyer_id");
            }
            if (json.has("sell_date")) {
                sellDate = json.getString("sell_date");
            }
            if (json.has("buyer_name")) {
                buyerName = json.getString("buyer_name");
            }
            if (json.has("discount_percent")) {
                discountPercent = json.getInt("discount_percent");
            }
            if (json.has("has_rated")) {
                rated = json.getBoolean("has_rated");
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public boolean hasRated() {
        return rated;
    }

    public void setRated(boolean rated) {
        this.rated = rated;
    }

    public int getDiscountPercent() {
        return discountPercent;
    }

    public void setDiscountPercent(int discountPercent) {
        this.discountPercent = discountPercent;
    }

    public boolean isHomemade() {
        return homemade;
    }

    public void setHomemade(boolean homemade) {
        this.homemade = homemade;
    }

    public String getBuyerName() {
        return buyerName;
    }

    public void setBuyerName(String buyerName) {
        this.buyerName = buyerName;
    }

    public String getSellDate() {
        return sellDate;
    }

    public void setSellDate(String sellDate) {
        this.sellDate = sellDate;
    }

    public int getBuyerId() {
        return buyerId;
    }

    public void setBuyerId(int buyerId) {
        this.buyerId = buyerId;
    }

    public boolean isFeatured() {
        return featured;
    }

    public void setFeatured(boolean featured) {
        this.featured = featured;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public int getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(int paymentId) {
        this.paymentId = paymentId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    public int getCityId() {
        return cityId;
    }

    public void setCityId(int cityId) {
        this.cityId = cityId;
    }

    public boolean isBrandNew() {
        return brandNew;
    }

    public void setBrandNew(boolean brandNew) {
        this.brandNew = brandNew;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public List<Media> getMedia() {
        return media;
    }

    public void setMedia(List<Media> media) {
        this.media = media;
    }

    public void addMedia(Media m) {
        if (this.media == null) {
            this.media = new ArrayList<>();
        }
        media.add(m);
    }

    public int getTotalCost() {
        return totalCost;
    }

    public void setTotalCost(int totalCost) {
        this.totalCost = totalCost;
    }

    public List<Attribute> getAttributes() {
        return attributes;
    }

    public void setAttributes(List<Attribute> attributes) {
        this.attributes = attributes;
    }

    public void addAttribute(Attribute attribute) {
        if (attributes == null) {
            attributes = new ArrayList<>();
        }
        attributes.add(attribute);
    }

    public int getCommentsCount() {
        return commentsCount;
    }

    public void setCommentsCount(int commentsCount) {
        this.commentsCount = commentsCount;
    }

    public Media getCover() {
        if (media == null || media.size() < 1) {
            return new Media();
        }
        for (Media m : media) {
            if (m.isCover()) {
                return m;
            }
        }
        return media.get(0);
    }

    @Override public int describeContents() {
        return 0;
    }

    @Override public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.buyerName);
        dest.writeString(this.sellDate);
        dest.writeInt(this.buyerId);
        dest.writeInt(this.totalCost);
        dest.writeInt(this.id);
        dest.writeInt(this.userId);
        dest.writeInt(this.categoryId);
        dest.writeInt(this.cityId);
        dest.writeByte(this.brandNew ? (byte) 1 : (byte) 0);
        dest.writeByte(this.homemade ? (byte) 1 : (byte) 0);
        dest.writeByte(this.featured ? (byte) 1 : (byte) 0);
        dest.writeString(this.description);
        dest.writeInt(this.price);
        dest.writeString(this.date);
        dest.writeInt(this.status);
        dest.writeTypedList(this.media);
        dest.writeTypedList(this.attributes);
        dest.writeInt(this.commentsCount);
        dest.writeInt(this.paymentId);
        dest.writeParcelable(this.user, flags);
        dest.writeInt(this.discountPercent);
        dest.writeByte(this.rated ? (byte) 1 : (byte) 0);
    }

    protected Product(Parcel in) {
        this.buyerName = in.readString();
        this.sellDate = in.readString();
        this.buyerId = in.readInt();
        this.totalCost = in.readInt();
        this.id = in.readInt();
        this.userId = in.readInt();
        this.categoryId = in.readInt();
        this.cityId = in.readInt();
        this.brandNew = in.readByte() != 0;
        this.homemade = in.readByte() != 0;
        this.featured = in.readByte() != 0;
        this.description = in.readString();
        this.price = in.readInt();
        this.date = in.readString();
        this.status = in.readInt();
        this.media = in.createTypedArrayList(Media.CREATOR);
        this.attributes = in.createTypedArrayList(Attribute.CREATOR);
        this.commentsCount = in.readInt();
        this.paymentId = in.readInt();
        this.user = in.readParcelable(User.class.getClassLoader());
        this.discountPercent = in.readInt();
        this.rated = in.readByte() != 0;
    }

    public static final Creator<Product> CREATOR = new Creator<Product>() {
        @Override public Product createFromParcel(Parcel source) {
            return new Product(source);
        }

        @Override public Product[] newArray(int size) {
            return new Product[size];
        }
    };
}
