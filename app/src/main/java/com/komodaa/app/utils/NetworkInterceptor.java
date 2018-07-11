
package com.komodaa.app.utils;


import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class NetworkInterceptor implements Interceptor {
    private String token;
    private String userId;

    public NetworkInterceptor(String token) {
        this.token = token;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request originalRequest = chain.request();
        Request manipulatedRequest = originalRequest.newBuilder()
                .addHeader("X-Authentication-Token", token)
                .build();
        return chain.proceed(manipulatedRequest);
    }

}
