package com.komodaa.app.utils;

import android.util.Log;

import com.komodaa.app.App;
import com.komodaa.app.models.User;
import com.onesignal.OneSignal;

/**
 * Created by nevercom on 10/12/16.
 */

public class UserUtils {

    private static final String USER_ID = "user_id";
    public static final String TOKEN = "token";


    public static User getUser() {
        User user = new User(JWTUtils.decode(App.getPrefs().getString(TOKEN, null)));

        Log.d("USER", "getUser: " + user.getJsonString());
        return user;
    }

    public static boolean isLoggedIn() {
        return getUser().getId() > 0;
    }

    public static void logout() {
        App.getPrefs().edit().putString(TOKEN, null).apply();
        OneSignal.deleteTag("userId");
    }
}
