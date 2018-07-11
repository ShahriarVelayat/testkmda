package com.komodaa.app.utils;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.komodaa.app.App;
import com.komodaa.app.activities.HomeActivity;
import com.komodaa.app.activities.MainActivity;
import com.onesignal.OSNotificationAction;
import com.onesignal.OSNotificationOpenResult;
import com.onesignal.OneSignal;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

public class OneSignalNotificationOpenHandler implements OneSignal.NotificationOpenedHandler {
    @Override
    public void notificationOpened(OSNotificationOpenResult result) {
        Log.d("Opened", "notificationOpened() called with: result = [" + result.stringify() + "]");
        OSNotificationAction.ActionType actionType = result.action.type;
        JSONObject data = result.notification.payload.additionalData;
        Log.d("NotifOpen", "notificationOpened: " + data);
        Context applicationContext = App.getInstance().getApplicationContext();
        Intent intent = new Intent(applicationContext, HomeActivity.class);
        if (data != null && data.has("target_activity")) {
            try {
                String activityName = data.getString("target_activity");
                Class<?> activityClass = Class.forName(activityName);

                intent = new Intent(applicationContext, activityClass);
                if (data.has("extras")) {
                    JSONObject extras = data.getJSONObject("extras");
                    Iterator<String> keysItr = extras.keys();
                    while (keysItr.hasNext()) {
                        String key = keysItr.next();
                        Object value = extras.get(key);
                        if (value instanceof Long) {
                            intent.putExtra(key, (long) value);
                        } else if (value instanceof Double) {
                            intent.putExtra(key, (double) value);
                        } else if (value instanceof Integer) {
                            intent.putExtra(key, (int) value);
                        } else if (value instanceof String) {
                            intent.putExtra(key, (String) value);
                        } else if (value instanceof Boolean) {
                            intent.putExtra(key, (boolean) value);
                        }

                    }

                }
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_NEW_TASK);
        applicationContext.startActivity(intent);
    }
}
