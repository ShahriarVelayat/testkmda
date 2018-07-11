package com.komodaa.app.utils;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.content.LocalBroadcastManager;

import com.komodaa.app.App;
import com.onesignal.NotificationExtenderService;
import com.onesignal.OSNotificationReceivedResult;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by nevercom on 3/2/17.
 */

public class OneSignalNotificationExtenderService extends NotificationExtenderService {
    @Override
    protected boolean onNotificationProcessing(OSNotificationReceivedResult notification) {
        JSONObject data = notification.payload.additionalData;
        if (data == null || data.length() < 1) {
            return false;
        }
        boolean isQuiet = false;
        try {
            isQuiet = data.has("quiet") && data.getBoolean("quiet");

            if (data.has("chat_sender_id")) {
                Intent intent = new Intent("new_message_arrived");
                intent.putExtra("user_id", data.getInt("chat_sender_id"));
                LocalBroadcastManager.getInstance(this).sendBroadcast(intent);

                if (App.isChatScreenRunning()) {
                    isQuiet = true;
                }
            }
            if (data.has("komodaa_settings_update")) {
                SharedPreferences prefs = App.getPrefs();
                SharedPreferences.Editor editor = prefs.edit();
                JSONArray settings = data.getJSONArray("komodaa_settings_update");
                for (int i = 0; i < settings.length(); i++) {
                    JSONObject entry = settings.getJSONObject(i);

                    String key = entry.getString("key");
                    String type = entry.getString("type");

                    switch (type.toLowerCase()) {
                        case "int":
                            editor.putInt(key, entry.getInt("value"));
                            break;
                        case "string":
                            editor.putString(key, entry.getString("value"));
                            break;
                        case "boolean":
                            editor.putBoolean(key, entry.getBoolean("value"));
                            break;
                        case "long":
                            editor.putLong(key, entry.getLong("value"));
                            break;
                    }
                }
                editor.apply();
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return isQuiet;
    }


}
