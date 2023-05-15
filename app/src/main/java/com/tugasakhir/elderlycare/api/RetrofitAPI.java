package com.tugasakhir.elderlycare.api;

import com.tugasakhir.elderlycare.model.ButtonResponse;
import com.tugasakhir.elderlycare.model.LoginResponse;
import com.tugasakhir.elderlycare.model.SensorResponse;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;

public interface RetrofitAPI {

    @POST("/apps/caregiver/login")
    Call<LoginResponse> createPost(@Body RequestBody body);

    @GET("/apps/caregiver/get-sensor/{house_id}")
    Call<Object> getSensor(@Path("house_id") String house_id);

    @GET("/apps/caregiver/get-button/{house_id}")
    Call<Object> getBtn(@Path("house_id") String house_id);

    @GET("/point/coordinate/{house_id}")
    Call<Object> getPoint(@Path("house_id") String house_id);

    @GET("/apps/elder/data/{caregiver_id}")
    Call<Object> getElder(@Path("caregiver_id") String caregiver_id);

    @Multipart
    @POST("/elder/edit/img")
    Call<ResponseBody> updateElderImage(@Part("id") RequestBody elder_id,
                                        @Part("name") RequestBody name,
                                        @Part("address") RequestBody address,
                                        @Part("birthdate") RequestBody birthdate,
                                        @Part("house_id") RequestBody house_id,
                                        @Part("robot_id") RequestBody robot_id,
                                        @Part("watch_id") RequestBody watch_id,
                                        @Part MultipartBody.Part image);

    @Multipart
    @POST("/elder/edit")
    Call<ResponseBody> updateElder(@Part("id") RequestBody elder_id,
                                   @Part("name") RequestBody name,
                                   @Part("address") RequestBody address,
                                   @Part("birthdate") RequestBody birthdate,
                                   @Part("house_id") RequestBody house_id,
                                   @Part("robot_id") RequestBody robot_id,
                                   @Part("watch_id") RequestBody watch_id);

    @Multipart
    @POST("/elder/insert/img")
    Call<ResponseBody> addElderImage(@Part("id") RequestBody elder_id,
                                        @Part("name") RequestBody name,
                                        @Part("address") RequestBody address,
                                        @Part("birthdate") RequestBody birthdate,
                                        @Part("house_id") RequestBody house_id,
                                        @Part("robot_id") RequestBody robot_id,
                                        @Part("watch_id") RequestBody watch_id,
                                        @Part MultipartBody.Part image);


    @Multipart
    @POST("/elder/insert")
    Call<ResponseBody> addElder(@Part("id") RequestBody elder_id,
                                     @Part("name") RequestBody name,
                                     @Part("address") RequestBody address,
                                     @Part("birthdate") RequestBody birthdate,
                                     @Part("house_id") RequestBody house_id,
                                     @Part("robot_id") RequestBody robot_id,
                                     @Part("watch_id") RequestBody watch_id);

    @Multipart
    @POST("/elder/delete")
    Call<ResponseBody> deleteElder(@Part("caregiver_id") RequestBody caregiver_id,
                                   @Part("elder_id") RequestBody elder_id);

    @Multipart
    @POST("/caregiver/signup")
    Call<ResponseBody> caregiverSignup(@Part("name") RequestBody name,
                                       @Part("username") RequestBody username,
                                       @Part("password") RequestBody password,
                                       @Part("email") RequestBody email,
                                       @Part("phone") RequestBody phone,
                                       @Part("address") RequestBody robot_id);

    @Multipart
    @POST("/caregiver/edit")
    Call<ResponseBody> caregiverEdit(@Part("id") RequestBody caregiver_id,
                                   @Part("name") RequestBody name,
                                   @Part("username") RequestBody username,
                                   @Part("email") RequestBody email,
                                   @Part("phone") RequestBody phone,
                                   @Part("address") RequestBody address);


    @Multipart
    @POST("/caregiver/edit/password")
    Call<ResponseBody> caregiverEditPassword(@Part("id") RequestBody caregiver_id,
                                             @Part("username") RequestBody name,
                                             @Part("passwordOld") RequestBody passwordOld,
                                             @Part("passwordNew") RequestBody passwordNew);

    @Multipart
    @POST("/caregiver/elder")
    Call<ResponseBody> existingElder(@Part("caregiver_id") RequestBody caregiver_id,
                                             @Part("elder_name") RequestBody name,
                                             @Part("elder_id") RequestBody elder_id);
}
