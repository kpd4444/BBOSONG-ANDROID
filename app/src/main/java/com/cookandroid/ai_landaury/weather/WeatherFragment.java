package com.cookandroid.ai_landaury.weather;

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

    private TextView emojiIcon;
    private ImageView weatherIcon;
    private TextView tvWeatherTitle;
    private TextView tvWeatherDesc;


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

        // 이모지 불러오기
        emojiIcon = view.findViewById(R.id.imgWeatherIconText);
        weatherIcon = view.findViewById(R.id.imgWeatherIcon);
        tvWeatherTitle = view.findViewById(R.id.tvWeatherTitle);
        tvWeatherDesc = view.findViewById(R.id.tvWeatherDesc);


        return view;
    }

    private void loadWeatherAdvice(double nx, double ny) {
        adviceText.setText("날씨 데이터를 불러오는 중...");

        weatherApi.getWeather(nx, ny).enqueue(new Callback<WeatherAdviceResponse>() {
            @Override
            public void onResponse(Call<WeatherAdviceResponse> call, Response<WeatherAdviceResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    WeatherAdviceResponse weather = response.body();

                    updateWeatherIcon(weather.getSky(), weather.getRainType());

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

    private void updateWeatherIcon(int sky, int rainType) {

        // PNG 이미지 사용 → 텍스트는 숨김
        emojiIcon.setVisibility(View.GONE);
        weatherIcon.setVisibility(View.VISIBLE);

        int iconRes;

        // ----------------------------
        // 1) 강수 형태 우선 판단
        // ----------------------------
        switch (rainType) {
            case 1: // 비
            case 2: // 비/눈
                iconRes = R.drawable.ic_rainy;
                tvWeatherTitle.setText("실내건조");
                tvWeatherDesc.setText("지금은 비가 오고 있어요.\n실내건조를 추천해요!");
                weatherIcon.setImageResource(iconRes);
                return;

            case 3: // 눈
                iconRes = R.drawable.ic_rainy; // 눈 아이콘 별도로 있으면 교체
                tvWeatherTitle.setText("실내건조");
                tvWeatherDesc.setText("지금은 눈이 내리고 있어요.\n실내건조를 추천해요!");
                weatherIcon.setImageResource(iconRes);
                return;
        }

        // ----------------------------
        // 2) 비/눈이 아니면 하늘 코드로 판단
        // ----------------------------
        switch (sky) {
            case 1: // 맑음
            default:
                iconRes = R.drawable.ic_sunny;
                tvWeatherTitle.setText("실외건조");
                tvWeatherDesc.setText("오늘은 맑아요!\n실외건조를 추천해요.");
                break;

            case 3: // 구름 많음
            case 4: // 흐림
                iconRes = R.drawable.ic_cloudy;
                tvWeatherTitle.setText("부분 실외건조");
                tvWeatherDesc.setText("오늘은 구름이 많아요.\n통풍이 좋은 곳에서 건조하세요.");
                break;
        }

        weatherIcon.setImageResource(iconRes);
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