package com.cookandroid.ai_landaury.weather;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.cookandroid.ai_landaury.R;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Query;

public class WeatherFragment extends Fragment {

    private TextView adviceText;
    private WeatherApi weatherApi;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_weather, container, false);
        adviceText = view.findViewById(R.id.textWeatherAdvice);

        // ✅ Retrofit 초기화
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://10.0.2.2:8080") // ⚠️ 에뮬레이터용 localhost
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        weatherApi = retrofit.create(WeatherApi.class);

        // ✅ 서울 좌표(예시): nx=60, ny=127
        loadWeatherAdvice(60.0, 127.0);

        return view;
    }

    private void loadWeatherAdvice(double nx, double ny) {
        adviceText.setText("날씨 데이터를 불러오는 중...");

        weatherApi.getWeather(nx, ny).enqueue(new Callback<WeatherAdviceResponse>() {
            @Override
            public void onResponse(Call<WeatherAdviceResponse> call, Response<WeatherAdviceResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    WeatherAdviceResponse weather = response.body();

                    StringBuilder adviceBuilder = new StringBuilder();
                    adviceBuilder.append("🌡 온도: ").append(weather.getTemperature()).append("°C\n")
                            .append("💧 습도: ").append(weather.getHumidity()).append("%\n")
                            .append("☔ 강수확률: ").append(weather.getRainProbability()).append("%\n\n")
                            .append("🧺 세탁/건조 추천:\n")
                            .append(weather.getAdvice().getSummary()).append("\n");

                    for (String detail : weather.getAdvice().getAdviceList()) {
                        adviceBuilder.append("• ").append(detail).append("\n");
                    }

                    adviceText.setText(adviceBuilder.toString());
                } else {
                    adviceText.setText("날씨 정보를 불러올 수 없습니다.");
                }
            }

            @Override
            public void onFailure(Call<WeatherAdviceResponse> call, Throwable t) {
                adviceText.setText("서버 연결 실패: " + t.getMessage());
                Toast.makeText(getContext(), "서버 연결 실패", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ✅ Retrofit API 인터페이스
    interface WeatherApi {
        @GET("/api/weather/current")
        Call<WeatherAdviceResponse> getWeather(
                @Query("nx") double nx,
                @Query("ny") double ny
        );
    }
}