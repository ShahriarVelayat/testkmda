package com.komodaa.app;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.komodaa.app.activities.ChatActivity;
import com.komodaa.app.interfaces.NetworkCallback;
import com.komodaa.app.models.Error;
import com.komodaa.app.utils.ApiClient;
import com.komodaa.app.utils.OneSignalNotificationOpenHandler;
import com.komodaa.app.utils.OneSignalNotificationReceivedHandler;
import com.komodaa.app.utils.UserUtils;
import com.komodaa.app.utils.Utils;
import com.onesignal.OneSignal;

import org.json.JSONException;
import org.json.JSONObject;

import io.fabric.sdk.android.Fabric;

public class App extends Application implements Application.ActivityLifecycleCallbacks {
    private static final String TAG = "Komodaa";
    public static final String MESSAGES_LAST_SEEN = "messages_lastSeen";
    private static SharedPreferences prefs;
    private static App INSTANCE;
    private static boolean isChatActivityRunning = false;

    @Override public void onCreate() {
        super.onCreate();
        INSTANCE = this;
        Fabric.with(this, new Crashlytics());
        OneSignal.startInit(this)
                 .setNotificationOpenedHandler(new OneSignalNotificationOpenHandler())
                 .setNotificationReceivedHandler(new OneSignalNotificationReceivedHandler())
                 .inFocusDisplaying(OneSignal.OSInFocusDisplayOption.Notification)
                 .init();

        prefs = getSharedPreferences(getPackageName() + "prefs",
                Context.MODE_PRIVATE);
        //Utils.overrideFont(this, "SERIF", "fonts/IRANSans.ttf");
        JSONObject data = new JSONObject();
        try {
            data.put("app_version", BuildConfig.VERSION_CODE);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        ApiClient.makeRequest(this, "GET", "/all", data, new NetworkCallback() {
            @Override public void onSuccess(int status, JSONObject data) {
                Log.d(TAG, "onSuccess: ");
                getPrefs().edit().putString("config", data.toString()).apply();

            }

            @Override public void onFailure(int status, Error error) {
                Log.d(TAG, "onFailure: ");
            }
        });

        if (UserUtils.isLoggedIn()) {
            String email = UserUtils.getUser().getEmail();
            if (!TextUtils.isEmpty(email)) {
                OneSignal.syncHashedEmail(email);
            }
            boolean isPushSent = prefs.getBoolean("is_push_id_sent", false);
            if (!isPushSent) {
                sendPushId(getApplicationContext());

                prefs.edit().putBoolean("is_push_id_sent", true).apply();
            }
        }
        registerActivityLifecycleCallbacks(this);
    }

    public static synchronized SharedPreferences getPrefs() {
        return prefs;
    }

    public static App getInstance() {
        return INSTANCE;
    }

    public static void sendPushId(final Context c) {

        /*
         * Here we retrieve the OneSignal ID and GCM registration ID associated with this user
         * and send it to the server
         */

        OneSignal.idsAvailable((userId, registrationId) -> {
            try {
                JSONObject data = new JSONObject();
                data.put("one_signal_id", userId);
                data.put("gcm_registration_id", registrationId);

                ApiClient.makeRequest(c, "POST", "/push_register", data, new NetworkCallback() {
                    @Override public void onSuccess(int status, JSONObject data) {

                    }

                    @Override public void onFailure(int status, Error error) {

                    }
                });

            } catch (JSONException e) {
                e.printStackTrace();
            }
        });

    }

    public static boolean isChatScreenRunning() {
        return isChatActivityRunning;
    }

    @Override public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        if (activity instanceof ChatActivity) {
            isChatActivityRunning = true;
        }
    }

    @Override public void onActivityStarted(Activity activity) {
        if (activity instanceof ChatActivity) {
            isChatActivityRunning = true;
        }
    }

    @Override public void onActivityResumed(Activity activity) {
        if (activity instanceof ChatActivity) {
            isChatActivityRunning = true;
        }
    }

    @Override public void onActivityPaused(Activity activity) {
        if (activity instanceof ChatActivity) {
            isChatActivityRunning = false;
        }
    }

    @Override public void onActivityStopped(Activity activity) {
        if (activity instanceof ChatActivity) {
            isChatActivityRunning = false;
        }
    }

    @Override public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

    }

    @Override public void onActivityDestroyed(Activity activity) {
        if (activity instanceof ChatActivity) {
            isChatActivityRunning = false;
        }
    }
}
