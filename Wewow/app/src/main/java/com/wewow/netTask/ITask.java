package com.wewow.netTask;

import com.squareup.okhttp.RequestBody;

import org.json.JSONObject;


import retrofit.Callback;
import retrofit.http.Field;
import retrofit.http.GET;
import retrofit.http.Header;
import retrofit.http.Multipart;
import retrofit.http.POST;
import retrofit.http.Part;
import retrofit.http.Query;

/**
 * Created by iris on 17/3/14.
 */
public interface ITask {


    //home page
    @GET("/banner")
    void banner(@Header("User-Agent") String headerContentType, Callback<JSONObject> callback);

    @GET("/index_collection_categorys")
    void indexCollectionCategorys(@Header("User-Agent") String headerContentType, Callback<JSONObject> callback);

    @GET("/new")
    void latestInstite(@Header("User-Agent") String headerContentType, Callback<JSONObject> callback);

    @GET("/discovery_info")
    void hotArtistisAndInstitutes(@Header("User-Agent") String headerContentType, Callback<JSONObject> callback);

    //check cache updated or not
    @GET("/update_at")
    void updateAt(@Header("User-Agent") String headerContentType, Callback<JSONObject> callback);

    //artist list
    @GET("/artists_mini")
    void allArtists(@Header("User-Agent") String headerContentType, @Query("user_id") String userId,
                    @Query("page") int page, Callback<JSONObject> callback);


    //artist detail
    @GET("/artist_mini")
    void artistDetail(@Header("User-Agent") String headerContentType, @Query("user_id") String userId,
                      @Query("artist_id") String artistId, Callback<JSONObject> callback);

    @GET("/user_followed_mini")
    void artistsSubscribed(@Header("User-Agent") String headerContentType, @Query("user_id") String userId,
                           @Query("page") int page, Callback<JSONObject> callback);


    @POST("/follow")
    void followArtist(@Header("User-Agent") String headerContentType, @Query("user_id") String userId,
                      @Query("artist_id") String artistId, @Query("token") String token, @Query("follow") int follow, Callback<JSONObject> callback);

    @GET("/index_category_collections")
    void categoryArtistsAndInstitutes(@Header("User-Agent") String headerContentType, @Query("collection_category_id") String id,
                                      Callback<JSONObject> callback);

    //feedback
    @GET("/feedbacks")
    void feedbacks(@Header("User-Agent") String headerContentType, @Query("user_id") String id, @Query("page") int page,
                   Callback<JSONObject> callback);

    @POST("/feedback")
    void feedbackText(@Header("User-Agent") String headerContentType, @Query("user_id") String id,
                      @Query("token") String token, @Query("content") String content,
                      @Query("content_type") String content_type, @Query("status") String status,
                      Callback<JSONObject> callback);

    @POST("/feedback")
    void feedbackImage(@Header("User-Agent") String headerContentType, @Query("user_id") String id,
                       @Query("token") String token, @Query("content") String content,
                       @Query("content_type") String content_type, @Query("image_width") String image_width,
                       @Query("image_height") String image_height, @Query("status") String status,
                       Callback<JSONObject> callback);


    @Multipart
    @POST("/feedback")
    void uploadImage(@Header("User-Agent") String headerContentType, @Query("user_id") String id,
                     @Query("token") String token, @Query("content") String content,
                     @Query("content_type") String content_type, @Query("image_width") String image_width,
                     @Query("image_height") String image_height, @Query("status") String status,
                     @Part("fileName") String description,
                     @Part("file\"; filename=\"image.png\"") RequestBody imgs, Callback<JSONObject> callback);


    //search
    @GET("/hotwords")
    void getHotSearchWords(@Header("User-Agent") String headerContentType, Callback<JSONObject> callback);

    @GET("/search_mini")
    void search(@Header("User-Agent") String headerContentType, @Query("keywords") String keywords, Callback<JSONObject> callback);

    //new version notification
    @GET("/notification")
    void notification(@Header("User-Agent") String headerContentType, Callback<JSONObject> callback);

    //get ads
    @GET("/ads")
    void ads(@Header("User-Agent") String headerContentType, Callback<JSONObject> callback);

    @GET("/user_notification")
    void user_notification(@Header("User-Agent") String headerContentType, @Query("user_id") String user_id, Callback<JSONObject> callback);


    @POST("/artist_read")
    void artist_read(@Header("User-Agent") String headerContentType, @Query("user_id") String id,
                     @Query("token") String token, @Query("artist_id") String artist_id, @Query("read") String read,
                     Callback<JSONObject> callback);


    @GET("/subject")
    void subject(@Header("User-Agent") String headerContentType, @Query("subject_id") String subjectId,
                 Callback<JSONObject> callback);


}



