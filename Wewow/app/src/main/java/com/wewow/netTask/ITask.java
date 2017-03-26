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

    @GET("/index_collection_categorys")
    void indexCollectionCategorys(@Header("User-Agent") String headerContentType, Callback<JSONObject> callback);

    @GET("/new")
    void latestInstite(@Header("User-Agent") String headerContentType, Callback<JSONObject> callback);

    @GET("/discovery_info")
    void hotArtistisAndInstitutes(@Header("User-Agent") String headerContentType, Callback<JSONObject> callback);

    @GET("/update_at")
    void updateAt(@Header("User-Agent") String headerContentType, Callback<JSONObject> callback);


}
