package com.komodaa.app.utils;

import android.util.Log;

import com.onesignal.OSNotification;
import com.onesignal.OneSignal;

/**
 * Created by nevercom on 3/2/17.
 */

public class OneSignalNotificationReceivedHandler implements OneSignal.NotificationReceivedHandler {
    @Override public void notificationReceived(OSNotification notification) {

        Log.d("Received", "notificationReceived() called with: notification = [" + notification.stringify() + "]");
    }
}
