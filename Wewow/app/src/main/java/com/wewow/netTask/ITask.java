package com.wewow.netTask;

import com.squareup.okhttp.RequestBody;

import org.json.JSONObject;


import retrofit.Callback;
import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
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
                      @Query("artist_id") String artistId, @Query("page") int page, Callback<JSONObject> callback);

    @GET("/user_followed_mini")
    void artistsSubscribed(@Header("User-Agent") String headerContentType, @Query("user_id") String userId,
                           @Query("page") int page, Callback<JSONObject> callback);


    @POST("/follow")
    @FormUrlEncoded
    void followArtist(@Header("User-Agent") String headerContentType, @Field("user_id") String userId,
                      @Field("artist_id") String artistId, @Field("token") String token, @Field("follow") int follow, Callback<JSONObject> callback);

    @GET("/index_category_collections")
    void categoryArtistsAndInstitutes(@Header("User-Agent") String headerContentType, @Query("collection_category_id") String id,@Query("page") int page,
                                      Callback<JSONObject> callback);

    //feedback
    @GET("/feedbacks")
    void feedbacks(@Header("User-Agent") String headerContentType, @Query("user_id") String id, @Query("page") int page,
                   Callback<JSONObject> callback);

    @POST("/feedback")
    @FormUrlEncoded
    void feedbackText(@Header("User-Agent") String headerContentType, @Field("user_id") String id,
                      @Field("token") String token, @Field("content") String content,
                      @Field("content_type") String content_type, @Field("status") String status,
                      Callback<JSONObject> callback);

    @POST("/feedback")
    @FormUrlEncoded
    void feedbackImage(@Header("User-Agent") String headerContentType, @Field("user_id") String id,
                       @Field("token") String token, @Field("content") String content,
                       @Field("content_type") String content_type, @Field("image_width") String image_width,
                       @Field("image_height") String image_height, @Field("status") String status,
                       Callback<JSONObject> callback);

    //search
    @GET("/hotwords")
    void getHotSearchWords(@Header("User-Agent") String headerContentType, Callback<JSONObject> callback);

    @GET("/search_mini")
    void search(@Header("User-Agent") String headerContentType, @Query("keywords") String keywords, Callback<JSONObject> callback);

    //new version notification
    @GET("/notification")
    void notification(@Header("User-Agent") String headerContentType,  @Query("current_version") String current_version, @Query("channel") int channel,Callback<JSONObject> callback);

    //get ads
    @GET("/ads")
    void ads(@Header("User-Agent") String headerContentType, Callback<JSONObject> callback);

    @GET("/user_notification")
    void user_notification(@Header("User-Agent") String headerContentType, @Query("user_id") String user_id, Callback<JSONObject> callback);


    @POST("/artist_read")
    @FormUrlEncoded
    void artist_read(@Header("User-Agent") String headerContentType, @Field("user_id") String id,
                     @Field("token") String token, @Field("artist_id") String artist_id, @Field("read") String read,
                     Callback<JSONObject> callback);


    @GET("/subject")
    void subject(@Header("User-Agent") String headerContentType, @Query("subject_id") String subjectId,
                 Callback<JSONObject> callback);

    @GET("/gen_token")
    void getTokenToUploadFiles(@Header("User-Agent") String headerContentType,
                 Callback<JSONObject> callback);

    @POST("/youzanlogin")
    @FormUrlEncoded
    void youzanLogin(@Header("User-Agent") String headerContentType, @Field("user_id") String id,
                               Callback<JSONObject> callback);

    @GET("/share_count")
    void shareCount(@Header("User-Agent") String headerContentType, @Query("item_type") String itemType,
                    @Query("item_id") String itemId,
                    @Query("share_type") String shareType,
                    @Query("share") String share,
                               Callback<JSONObject> callback);


}



