package com.wikiwalks.wikiwalks;

import com.google.gson.JsonElement;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface RetrofitRequests {
    @POST("/setname")
    Call<JsonElement> setName(@Body RequestBody body);

    @POST("/paths/")
    Call<JsonElement> getPaths(@Query("n") double north,
                               @Query("e") double east,
                               @Query("s") double south,
                               @Query("w") double west,
                               @Body RequestBody body);

    @POST("/paths/{id}/group_walks/new")
    Call<JsonElement> add_group_walk(@Path("id") int id, @Body RequestBody body
    );

    @POST("/paths/{Path_id}/group_walks/{id}/attend")
    Call<JsonElement> toggle_group_walk_attendance(@Path("Path_id") int Path_id, @Path("id") int id, @Body RequestBody body
    );
    @POST("/paths/{id}")
    Call<JsonElement> updatePath(@Path("id") int id);

    @POST("/paths/{id}/edit")
    Call<JsonElement> editPath(@Path("id") int id,
                               @Body RequestBody body);

    @POST("/paths/{id}/walk")
    Call<JsonElement> walkPath(@Path("id") int id);

    @POST("/routes/new")
    Call<JsonElement> newRoute(@Body RequestBody body);

    @POST("/routes/{id}/delete")
    Call<JsonElement> deleteRoute(@Path("id") int id,
                                  @Body RequestBody body);

    @POST("/pois/new")
    Call<JsonElement> newPoI(@Body RequestBody body);

    @POST("/pois/{id}/edit")
    Call<JsonElement> editPoI(@Path("id") int id,
                              @Body RequestBody body);

    @POST("/pois/{id}/delete")
    Call<JsonElement> deletePoI(@Path("id") int id,
                                @Body RequestBody body);

    @POST("/paths/{id}/reviews")
    Call<JsonElement> getPathReviews(@Path("id") int id,
                                     @Query("page") int page,
                                     @Body RequestBody body);

    @POST("/paths/{id}/reviews/new")
    Call<JsonElement> newPathReview(@Path("id") int id,
                                    @Body RequestBody body);

    @POST("/paths/{id}/reviews/{review_id}/edit")
    Call<JsonElement> editPathReview(@Path("id") int id,
                                     @Path("review_id") int reviewId,
                                     @Body RequestBody body);

    @POST("/paths/{id}/reviews/{review_id}/delete")
    Call<JsonElement> deletePathReview(@Path("id") int id,
                                       @Path("review_id") int reviewId,
                                       @Body RequestBody body);

    @POST("/pois/{id}/reviews")
    Call<JsonElement> getPoIReviews(@Path("id") int id,
                                    @Query("page") int page,
                                    @Body RequestBody body);


    @POST("/pois/{id}/reviews/new")
    Call<JsonElement> newPoIReview(@Path("id") int id,
                                   @Body RequestBody body);

    @POST("/pois/{id}/reviews/{review_id}/edit")
    Call<JsonElement> editPoIReview(@Path("id") int id,
                                    @Path("review_id") int reviewId,
                                    @Body RequestBody body);

    @POST("/pois/{id}/reviews/{review_id}/delete")
    Call<JsonElement> deletePoIReview(@Path("id") int id,
                                      @Path("review_id") int reviewId,
                                      @Body RequestBody body);

    @POST("/paths/{id}/pictures")
    Call<JsonElement> getPathPictures(@Path("id") int id,
                                      @Query("page") int page,
                                      @Body RequestBody body);

    @Multipart
    @POST("/paths/{id}/pictures/new")
    Call<JsonElement> newPathPicture(@Path("id") int id,
                                     @Part MultipartBody.Part image,
                                     @Part("device_id") RequestBody deviceId,
                                     @Part("description") RequestBody description);

    @POST("/paths/{id}/pictures/{picture_id}/edit")
    Call<JsonElement> editPathPicture(@Path("id") int id,
                                      @Path("picture_id") int pictureId,
                                      @Body RequestBody body);

    @POST("/paths/{id}/pictures/{picture_id}/delete")
    Call<JsonElement> deletePathPicture(@Path("id") int id,
                                        @Path("picture_id") int pictureId,
                                        @Body RequestBody body);

    @POST("/pois/{id}/pictures")
    Call<JsonElement> getPoIPictures(@Path("id") int id,
                                     @Query("page") int page,
                                     @Body RequestBody body);

    @Multipart
    @POST("/pois/{id}/pictures/new")
    Call<JsonElement> newPoIPicture(@Path("id") int id,
                                    @Part MultipartBody.Part image,
                                    @Part("device_id") RequestBody deviceId,
                                    @Part("description") RequestBody description);

    @POST("/pois/{id}/pictures/{picture_id}/edit")
    Call<JsonElement> editPoIPicture(@Path("id") int id,
                                     @Path("picture_id") int pictureId,
                                     @Body RequestBody body);

    @POST("/pois/{id}/pictures/{picture_id}/delete")
    Call<JsonElement> deletePoIPicture(@Path("id") int id,
                                       @Path("picture_id") int pictureId,
                                       @Body RequestBody body);
}
