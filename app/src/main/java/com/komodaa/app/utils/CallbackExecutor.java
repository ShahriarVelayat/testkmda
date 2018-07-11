
package com.komodaa.app.utils;

import android.os.Handler;
import android.os.Looper;

import com.komodaa.app.interfaces.NetworkCallback;
import com.komodaa.app.models.Error;

import org.json.JSONObject;

class CallbackExecutor {
    static void sendOnSuccess(final NetworkCallback callback, final int status,
                              final JSONObject json) {
        new Handler(Looper.getMainLooper()).post(() -> callback.onSuccess(status, json));
    }

    static void sendOnFailure(final NetworkCallback callback, final int status, final Error error) {
        new Handler(Looper.getMainLooper()).post(() -> callback.onFailure(status, error));
    }
}
