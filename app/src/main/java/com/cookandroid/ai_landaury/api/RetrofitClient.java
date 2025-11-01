package com.cookandroid.ai_landaury.api;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;
import okhttp3.OkHttpClient;
import java.util.concurrent.TimeUnit;
public class RetrofitClient {

    private static final String BASE_URL = "http://10.0.2.2:8080"; // 로컬 테스트용
    private static Retrofit retrofit = null;

    public static Retrofit getClient() {
        if (retrofit == null) {

            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(100, TimeUnit.SECONDS)   // 연결 타임아웃
                    .readTimeout(100, TimeUnit.SECONDS)      // 읽기 타임아웃
                    .writeTimeout(100, TimeUnit.SECONDS)     // 쓰기 타임아웃
                    .build();


            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(ScalarsConverterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(client)
                    .build();
        }
        return retrofit;
    }
}
