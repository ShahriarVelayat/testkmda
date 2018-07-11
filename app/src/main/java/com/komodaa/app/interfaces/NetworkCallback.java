
package com.komodaa.app.interfaces;


import org.json.JSONObject;

import com.komodaa.app.models.Error;

/**
 * @author nevercom
 */
public interface NetworkCallback {

    /**
     * On Success
     *
     * @param status statusCode
     * @param data   actual Data returned by server
     */
    void onSuccess(int status, JSONObject data);

    /**
     * @param error object containing error info
     */
    void onFailure(int status, Error error);
}
