package com.tugasakhir.elderlycare;

import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface RetrofitAPI {

    @POST("/apps/caregiver/login")
    Call<LoginResponse> createPost(@Body RequestBody body);

}
