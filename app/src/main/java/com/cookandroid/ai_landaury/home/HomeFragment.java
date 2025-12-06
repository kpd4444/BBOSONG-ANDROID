package com.cookandroid.ai_landaury.home;

import android.content.Intent;
import android.content.SharedPreferences;
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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cookandroid.ai_landaury.R;
import com.cookandroid.ai_landaury.camera.ResultActivity;
import com.cookandroid.ai_landaury.weather.WeatherAdviceResponse;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Query;

public class HomeFragment extends Fragment {

    // 날씨 관련 뷰
    private ImageView imgWeather;
    private TextView tvWeatherTitle, tvWeatherDesc, tvGreeting;

    // Retrofit API
    private WeatherApi weatherApi;

    // 최근 분석 결과 리스트
    private RecyclerView recyclerRecent;
    private RecentResultAdapter adapter;
    private final List<RecentResultItem> recentList = new ArrayList<>();

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

        // [2] RecyclerView 설정 (최근 분석 결과)
        recyclerRecent = view.findViewById(R.id.recyclerRecent);
        recyclerRecent.setLayoutManager(
                new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false)
        );
        adapter = new RecentResultAdapter(getContext(), recentList);
        recyclerRecent.setAdapter(adapter);

        // 🌟 [중요 변경] 아이템 클릭 시 ResultActivity로 이동
        adapter.setOnItemClickListener(position -> {
            RecentResultItem item = recentList.get(position);

            Intent intent = new Intent(requireContext(), ResultActivity.class);
            // "저장된 데이터"임을 알림
            intent.putExtra("isSavedItem", true);
            // 상세 정보 전달
            intent.putExtra("name", item.getName());
            startActivity(intent);
        });

        // [3] Retrofit 초기화
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://10.0.2.2:8080") // 로컬 서버 (에뮬레이터용)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        weatherApi = retrofit.create(WeatherApi.class);

        // [4] 날씨 API 호출 (좌표 예시)
        loadWeatherAdvice(60.0, 127.0);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // 화면이 다시 보일 때마다 리스트 최신화 (사진 찍고 돌아왔을 때 대비)
        loadRecentResults();
    }

    /** SharedPreferences에서 최근 분석 결과 로드 (상세 필드 포함) */
    private void loadRecentResults() {
        if (getContext() == null) return;

        SharedPreferences prefs = requireContext().getSharedPreferences("recent_results", 0);
        String json = prefs.getString("results", "[]");

        recentList.clear();
        try {
            JSONArray arr = new JSONArray(json);

            for (int i = 0; i < arr.length(); i++) {
                JSONObject obj = arr.getJSONObject(i);

                // 기본 정보
                String name = obj.optString("name", "의류");
                String date = obj.optString("date", "-");
                String imgUri = obj.optString("imgUri", ""); // 썸네일 경로
                int imgResId = obj.optInt("imgResId", R.drawable.ic_clothes_placeholder);

                // 수정된 생성자로 객체 생성
                recentList.add(new RecentResultItem(
                        name, date, imgUri, imgResId
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        adapter.notifyDataSetChanged();
    }

    /** 날씨 API 호출 */
    private void loadWeatherAdvice(double nx, double ny) {
        weatherApi.getWeather(nx, ny).enqueue(new Callback<WeatherAdviceResponse>() {
            @Override
            public void onResponse(Call<WeatherAdviceResponse> call, Response<WeatherAdviceResponse> response) {
                if (!isAdded()) return; // 프래그먼트가 붙어있지 않으면 종료

                if (response.isSuccessful() && response.body() != null) {
                    WeatherAdviceResponse weather = response.body();
                    String summary = weather.getAdvice().getSummary();

                    tvWeatherTitle.setText("오늘의 세탁 추천");
                    tvWeatherDesc.setText(summary);
                    tvGreeting.setText("정지인님 반가워요.\n" + summary);

                    // 아이콘 설정
                    if (summary.contains("비") || summary.contains("눈")) {
                        imgWeather.setImageResource(R.drawable.ic_rainy);
                    } else if (summary.contains("맑")) {
                        imgWeather.setImageResource(R.drawable.ic_sunny);
                    } else {
                        imgWeather.setImageResource(R.drawable.ic_cloudy);
                    }
                } else {
                    tvWeatherDesc.setText("날씨 정보를 불러오지 못했습니다.");
                }
            }

            @Override
            public void onFailure(Call<WeatherAdviceResponse> call, Throwable t) {
                if (!isAdded()) return;
                tvWeatherDesc.setText("서버 연결에 실패했습니다.");
                // Toast.makeText(requireContext(), "Err: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Retrofit 인터페이스
    interface WeatherApi {
        @GET("/api/weather/current")
        Call<WeatherAdviceResponse> getWeather(
                @Query("nx") double nx,
                @Query("ny") double ny
        );
    }
}