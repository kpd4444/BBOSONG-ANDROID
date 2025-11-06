package com.cookandroid.ai_landaury.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.cookandroid.ai_landaury.R;
import com.cookandroid.ai_landaury.weather.WeatherAdviceResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Query;

public class HomeFragment extends Fragment {

    private ImageView imgWeather;
    private TextView tvWeatherTitle, tvWeatherDesc, tvGreeting;
    private WeatherApi weatherApi;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // [1] XML 뷰 연결
        imgWeather = view.findViewById(R.id.imgWeatherIcon);
        tvWeatherTitle = view.findViewById(R.id.tvWeatherTitle);
        tvWeatherDesc = view.findViewById(R.id.tvWeatherDesc);
        tvGreeting = view.findViewById(R.id.tvGreeting);

        // [2] Retrofit 초기화
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://10.0.2.2:8080") // ✅ 에뮬레이터에서 로컬 서버 접근
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        weatherApi = retrofit.create(WeatherApi.class);

        // [3] 날씨 API 호출 (서울 좌표 예시)
        loadWeatherAdvice(60.0, 127.0);

        return view;
    }

    private void loadWeatherAdvice(double nx, double ny) {
        weatherApi.getWeather(nx, ny).enqueue(new Callback<WeatherAdviceResponse>() {
            @Override
            public void onResponse(Call<WeatherAdviceResponse> call, Response<WeatherAdviceResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    WeatherAdviceResponse weather = response.body();

                    String summary = weather.getAdvice().getSummary();
                    List<String> details = weather.getAdvice().getAdviceList();

                    // [1] 대표 요약문을 UI에 표시
                    tvWeatherTitle.setText("오늘의 추천 세탁 🌤️");
                    tvWeatherDesc.setText(summary);
                    tvGreeting.setText("오늘의 세탁/건조 추천:\n" + summary);

                    // [2] 날씨 상태에 따라 아이콘 자동 변경 (간단한 분기)
                    if (summary.contains("비") || summary.contains("눈")) {
                        imgWeather.setImageResource(R.drawable.ic_rainy);
                    } else if (summary.contains("맑")) {
                        imgWeather.setImageResource(R.drawable.ic_sunny);
                    } else {
                        imgWeather.setImageResource(R.drawable.ic_cloudy);
                    }

                } else {
                    tvWeatherDesc.setText("서버 응답이 올바르지 않습니다.");
                }
            }

            @Override
            public void onFailure(Call<WeatherAdviceResponse> call, Throwable t) {
                tvWeatherDesc.setText("날씨 정보를 불러오지 못했습니다.");
                Toast.makeText(getContext(), "서버 연결 실패: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ✅ Retrofit 인터페이스 정의
    interface WeatherApi {
        @GET("/api/weather/current")
        Call<WeatherAdviceResponse> getWeather(
                @Query("nx") double nx,
                @Query("ny") double ny
        );
    }
}
