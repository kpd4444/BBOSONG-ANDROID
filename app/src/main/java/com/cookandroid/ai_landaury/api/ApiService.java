package com.cookandroid.ai_landaury.api;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface ApiService {

    // 연결 테스트용
    @GET("/api/analyze/ping")
    Call<String> ping();

    // 이미지 업로드용
    @Multipart
    @POST("/api/analyze/image")
    Call<LaundryAdviceResponse> uploadImage(
            @Part MultipartBody.Part file
    );
}