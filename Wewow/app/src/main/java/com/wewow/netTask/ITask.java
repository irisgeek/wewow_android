package com.wewow.netTask;

import org.json.JSONObject;


import retrofit.Callback;
import retrofit.http.GET;
import retrofit.http.Header;

/**
 * Created by iris on 17/3/14.
 */
public interface ITask {
    @GET("/banner")
    void banner(@Header("User-Agent") String headerContentType, Callback<JSONObject> callback);
}
