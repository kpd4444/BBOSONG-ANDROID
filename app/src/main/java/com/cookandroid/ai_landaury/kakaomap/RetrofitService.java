package com.cookandroid.ai_landaury.kakaomap;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface RetrofitService {

    // 세탁소 검색 요청 (백엔드)
    @GET("/api/map/laundry")
    Call<String> getLaundries(
            @Query("x") double x,
            @Query("y") double y
    );
}
