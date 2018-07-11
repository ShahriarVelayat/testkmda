
package com.komodaa.app.utils;

/**
 * Created by nevercom on 6/23/15.
 *
 * @author nevercom
 */

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;

import com.komodaa.app.App;
import com.komodaa.app.interfaces.NetworkCallback;
import com.komodaa.app.models.Error;
import com.komodaa.app.models.User;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.CacheControl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ApiClient {

    private static final String TAG = "ApiClientDebug";
    public static final String API_BASE_ADDRESS = "https://komodaa.com/api/v2";
    public static final String API_BASE_ADDRESS_NO_SECURE = "http://komodaa.com/api/v2";
    public static final int ERROR_NETWORK_NOT_AVAILABLE = -1;
    public static final int ERROR_JSON_EXCEPTION = -200;

    private static String constructGetParams(JSONObject data) {

        if (data != null) {
            List<String> dataList = new ArrayList<>();
            Iterator<String> iter = data.keys();
            while (iter.hasNext()) {
                String key = iter.next();
                try {
                    dataList.add(key + "=" + Uri.encode(data.get(key).toString()));
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
            dataList.add("dummyget=" + Uri.encode(System.currentTimeMillis() + ""));
            return TextUtils.join("&", dataList);
        }
        return "";
    }

    public static void makeRequest(final Context context, final String method, final String route, final JSONObject data,
                                   final NetworkCallback callback) {

        if (!Utils.isNetworkAvailable(context)) {
            CallbackExecutor.sendOnFailure(callback, 400, new Error(ERROR_NETWORK_NOT_AVAILABLE,
                    "اینرنت در دسترس نیست"));
            return;
        }
        new Thread(() -> {
            final SharedPreferences prefs = App.getPrefs();

            OkHttpClient client = null;
            try {
                // Create a trust manager that does not validate certificate chains
                final TrustManager[] trustAllCerts = new TrustManager[]{
                        new X509TrustManager() {
                            @Override
                            public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                            }

                            @Override
                            public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                            }

                            @Override
                            public X509Certificate[] getAcceptedIssuers() {
                                return new X509Certificate[0];
                            }
                        }
                };

                // Install the all-trusting trust manager
                final SSLContext sslContext = SSLContext.getInstance("SSL");
                sslContext.init(null, trustAllCerts, new SecureRandom());
                // Create an ssl socket factory with our all-trusting manager
                final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
                client = new OkHttpClient.Builder()
                        .connectTimeout(15, TimeUnit.SECONDS)
                        .writeTimeout(15, TimeUnit.SECONDS)
                        .readTimeout(30, TimeUnit.SECONDS)
                        .hostnameVerifier((hostname, session) -> true)
                        .sslSocketFactory(sslSocketFactory)
                        .build();

            } catch (Exception e) {
                client = new OkHttpClient.Builder()
                        .connectTimeout(15, TimeUnit.SECONDS)
                        .writeTimeout(15, TimeUnit.SECONDS)
                        .readTimeout(30, TimeUnit.SECONDS)
                        .hostnameVerifier((hostname, session) -> true)
                        .build();
            }
            String url = API_BASE_ADDRESS + route;
            // Apparently, HTTPS doesn't work well on older APIs, So we're falling back to the HTTP version
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                url = API_BASE_ADDRESS_NO_SECURE + route;
            }
            Log.i(TAG, url);
            Request request = null;
            String tokenString = prefs.getString(UserUtils.TOKEN, "");
            Log.d(TAG, "Token: " + tokenString);
            if (method.equalsIgnoreCase("POST")) {
                RequestBody body = RequestBody.create(
                        MediaType.parse("application/json; charset=utf-8"),
                        data != null ? data.toString() : "");
                request = new Request.Builder()
                        .url(url)
                        .cacheControl(CacheControl.FORCE_NETWORK)
                        .header("X-Authentication-Token", tokenString)
                        .post(body)
                        .build();
            } else if (method.equalsIgnoreCase("GET")) {
                String requestStr = constructGetParams(data);
                String path = data != null && data.length() >= 1
                        ? url + "?" + requestStr
                        : url + "?dummyget=" + Uri.encode(System.currentTimeMillis() + "");
                Log.i(TAG, path);
                request = new Request.Builder()
                        .url(path)
                        .cacheControl(CacheControl.FORCE_NETWORK)
                        .header("X-Authentication-Token", tokenString)
                        .build();

            } else if (method.equalsIgnoreCase("DELETE")) {
                String requestStr = constructGetParams(data);
                String path = data != null && data.length() >= 1 ? url + "?" + requestStr : url;
                Log.i(TAG, path);
                request = new Request.Builder()
                        .url(path)
                        .delete()
                        .cacheControl(CacheControl.FORCE_NETWORK)
                        .header("X-Authentication-Token", tokenString)
                        .build();

            } else if (method.equalsIgnoreCase("PUT")) {
                RequestBody body = RequestBody.create(
                        MediaType.parse("application/json; charset=utf-8"),
                        data != null ? data.toString() : "");
                request = new Request.Builder()
                        .url(url)
                        .cacheControl(CacheControl.FORCE_NETWORK)
                        .header("X-Authentication-Token", tokenString)
                        .put(body)
                        .build();

            }
            try {

                Response response = client.newCall(request).execute();

                final int status = response.code();
                Log.d(TAG, "Status: " + status);
                String responseStr = response.body().string();
                Log.i(TAG, "Response: " + response);
                Log.i(TAG, "Response: " + responseStr);
                //Log.i(TAG, "Response Headers: " + response.headers().toString());

//                    String tokenHeader = response.header("token");
//                    if (!TextUtils.isEmpty(tokenHeader) && tokenHeader.length() > 25) {
//                        prefs.edit().putString(UserUtils.TOKEN, tokenHeader).apply();
//                    }
                JSONObject json = new JSONObject(responseStr);
                if ((status >= 200) && (status < 300)) {


                    if (json.has("token")) {
                        String token = json.getString("token");
                        try {
                            User newUser = new User(JWTUtils.decode(token));
                            User currentUser = UserUtils.getUser();
                            if (currentUser.getId() != newUser.getId() && (!route.equalsIgnoreCase("/login") || !route.equalsIgnoreCase("/signup"))) {
                                Log.d(TAG, String.format(Locale.US, "Token Mismatch, old User ID: %d, New User ID: %d", currentUser.getId(), newUser.getId()));
                                JSONObject logData = new JSONObject();
                                logData.put("oldUserToken", prefs.getString(UserUtils.TOKEN, null));
                                logData.put("newUserToken", token);
                                logData.put("oldUserId", currentUser.getId());
                                logData.put("newUserId", newUser.getId());
                                logData.put("route", route);
                                logData.put("responseString", responseStr);
                                logData.put("requestData", data != null ? data.toString() : "");

                                makeRequest(context, "POST", "/app_log", logData, new NetworkCallback() {
                                    @Override
                                    public void onSuccess(int status, JSONObject data1) {

                                    }

                                    @Override public void onFailure(int status, Error error) {

                                    }
                                });
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        prefs.edit().putString(UserUtils.TOKEN, token).apply();
                    }

                    CallbackExecutor.sendOnSuccess(callback, status, json);

                } else if (status != 304) {
                    int errorCode = json.has("errorCode") ? json.getInt("errorCode") : 0;
                    String errorMessage = json.has("errorMessage") ? json.getString("errorMessage") : "";
                    CallbackExecutor.sendOnFailure(callback, status, new Error(errorCode,
                            errorMessage));
                    new Handler(Looper.getMainLooper()).post(() -> {
                        if (status == 401
                                && !route.equalsIgnoreCase("/login")
                                && !route.equalsIgnoreCase("/signup")
                                && !route.equalsIgnoreCase("/push_register")
                                ) {

                            UserUtils.logout();
                            Utils.displayLoginErrorDialog(context);
                        }
                    });

                }

            } catch (JSONException | IOException e) {
                e.printStackTrace();
                CallbackExecutor.sendOnFailure(callback, 400, new Error(ERROR_JSON_EXCEPTION,
                        "Internal Error: " + e.getMessage()));

                //Log.d(TAG, "Exception JSON", e);

            }

        }).start();

    }
}
