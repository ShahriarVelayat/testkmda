package com.komodaa.app.models;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by nevercom on 2/11/18.
 */

public class UserActivity {


    private int objectType;
    private int objectId;
    private User user;
    private String activity;
    private String time;


    public static int TYPE_ITEM    = 1;
    public static int TYPE_REQUEST = 2;
    public static int TYPE_USER    = 3;
    public UserActivity(JSONObject json) {
        if (json == null) {
            return;
        }

        try {
            if (json.has("object_type")) {
                objectType = json.getInt("object_type");
            }
            if (json.has("object_id")) {
                objectId = json.getInt("object_id");
            }
            if (json.has("user")) {
                user = new User(json.getJSONObject("user"));
            }
            if (json.has("description")) {
                activity = json.getString("description");
            }
            if (json.has("time")) {
                time = json.getString("time");
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public int getObjectType() {
        return objectType;
    }

    public int getObjectId() {
        return objectId;
    }

    public User getUser() {
        return user;
    }

    public String getActivity() {
        return activity;
    }

    public String getTime() {
        return time;
    }
}
