package com.tugasakhir.elderlycare.api;

import com.tugasakhir.elderlycare.model.ButtonResponse;
import com.tugasakhir.elderlycare.model.LoginResponse;
import com.tugasakhir.elderlycare.model.SensorResponse;

import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface RetrofitAPI {

    @POST("/apps/caregiver/login")
    Call<LoginResponse> createPost(@Body RequestBody body);

    @GET("/apps/caregiver/get-sensor/{house_id}")
    Call<Object> getSensor(@Path("house_id") String house_id);

    @GET("/apps/caregiver/get-button/{house_id}")
    Call<Object> getBtn(@Path("house_id") String house_id);
}
