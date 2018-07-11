package com.komodaa.app.utils;

import android.util.Base64;
import android.util.Log;

import java.io.UnsupportedEncodingException;

/**
 * Created by nevercom on 10/12/16.
 */

public class JWTUtils {

    public static String decode(String jwtEncodedString) {
        Log.d("JWT", "decode() called with: jwtEncodedString = [" + jwtEncodedString + "]");
        if (jwtEncodedString == null) {
            return null;
        }
        try {
            String[] split = jwtEncodedString.split("\\.");
            return getJson(split[1]);
        } catch (UnsupportedEncodingException e) {
            return null;
        }
    }

    private static String getJson(String strEncoded) throws UnsupportedEncodingException {
        byte[] decodedBytes = Base64.decode(strEncoded, Base64.NO_WRAP);
        return new String(decodedBytes, "UTF-8");
    }
}
