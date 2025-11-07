package com.cookandroid.ai_landaury.home;

import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
<<<<<<< Updated upstream
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.cookandroid.ai_landaury.R;

public class HomeFragment extends Fragment {
=======
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cookandroid.ai_landaury.R;
import com.cookandroid.ai_landaury.weather.WeatherAdviceResponse;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import retrofit2.*;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Query;

public class HomeFragment extends Fragment {

    private ImageView imgWeather;
    private TextView tvWeatherTitle, tvWeatherDesc, tvGreeting;
    private WeatherApi weatherApi;

    private RecyclerView recyclerRecent;
    private RecentResultAdapter adapter;
    private final List<RecentResultItem> recentList = new ArrayList<>();
    private JSONArray lastRawArray = new JSONArray(); // 클릭 시 상세필드 꺼내 쓰려고 보관

>>>>>>> Stashed changes
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
<<<<<<< Updated upstream
        return inflater.inflate(R.layout.fragment_home, container, false);
=======

        View view = inflater.inflate(R.layout.fragment_home, container, false);

        imgWeather = view.findViewById(R.id.imgWeatherIcon);
        tvWeatherTitle = view.findViewById(R.id.tvWeatherTitle);
        tvWeatherDesc = view.findViewById(R.id.tvWeatherDesc);
        tvGreeting = view.findViewById(R.id.tvGreeting);

        recyclerRecent = view.findViewById(R.id.recyclerRecent);
        recyclerRecent.setLayoutManager(
                new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false)
        );
        adapter = new RecentResultAdapter(getContext(), recentList);
        recyclerRecent.setAdapter(adapter);

        // 클릭 → 해당 인덱스의 상세 JSON으로 모달 표시
        adapter.setOnItemClickListener(this::showDetailByIndex);

        // 최근 분석 결과
        loadRecentResults();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://10.0.2.2:8080")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        weatherApi = retrofit.create(WeatherApi.class);
        loadWeatherAdvice(60.0, 127.0);

        return view;
    }

    private void loadRecentResults() {
        SharedPreferences prefs = requireContext().getSharedPreferences("recent_results", 0);
        String json = prefs.getString("results", "[]");

        recentList.clear();
        try {
            JSONArray arr = new JSONArray(json);
            lastRawArray = arr; // 보관

            for (int i = 0; i < arr.length(); i++) {
                JSONObject obj = arr.getJSONObject(i);

                String name = obj.optString("name", "의류");
                String date = obj.optString("date", "-");
                String imagePath = obj.optString("imgUri", "");         // 썸네일 절대경로
                int imgResId = obj.optInt("imgResId", R.drawable.ic_clothes_placeholder);

                recentList.add(new RecentResultItem(name, date, imagePath, imgResId));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        adapter.notifyDataSetChanged();
    }

    // 인덱스로 상세 JSON 조회 → 모달 구성
    private void showDetailByIndex(int position) {
        try {
            if (position < 0 || position >= lastRawArray.length()) return;
            JSONObject obj = lastRawArray.getJSONObject(position);

            String name = obj.optString("name", "의류");
            String date = obj.optString("date", "-");
            String fullPath = obj.optString("fullPath", "");
            String material = obj.optString("material", "-");
            String color = obj.optString("color", "-");
            String washingMethod = obj.optString("washingMethod", "-");
            String cautions = obj.optString("cautions", "-");

            String symbolsText = "";
            if (obj.has("symbols")) {
                JSONArray syms = obj.optJSONArray("symbols");
                if (syms != null) {
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < syms.length(); i++) {
                        if (i > 0) sb.append(", ");
                        sb.append(syms.optString(i));
                    }
                    symbolsText = sb.toString();
                }
            }

            BottomSheetDialog dialog = new BottomSheetDialog(requireContext());
            LinearLayout root = new LinearLayout(requireContext());
            root.setOrientation(LinearLayout.VERTICAL);
            int pad = dp(16);
            root.setPadding(pad, pad, pad, pad);

            ImageView iv = new ImageView(requireContext());
            iv.setLayoutParams(new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, dp(220)));
            iv.setScaleType(ImageView.ScaleType.CENTER_CROP);

            if (fullPath != null && !fullPath.isEmpty()) {
                String p = fullPath.startsWith("file://") ? Uri.parse(fullPath).getPath() : fullPath;
                File f = new File(p);
                if (f.exists()) iv.setImageBitmap(BitmapFactory.decodeFile(f.getAbsolutePath()));
                else iv.setImageResource(R.drawable.ic_clothes_placeholder);
            } else {
                iv.setImageResource(R.drawable.ic_clothes_placeholder);
            }

            TextView tName = makeTitle(name);
            TextView tDate = makeSub(date);

            TextView tMat = makeLine("소재", material);
            TextView tCol = makeLine("색상", color);
            TextView tWash = makeLine("세탁 방법", washingMethod);
            TextView tCau = makeLine("주의사항", cautions);
            TextView tSym = makeLine("추천 심볼", symbolsText.isEmpty() ? "-" : symbolsText);

            root.addView(iv);
            root.addView(tName);
            root.addView(tDate);
            root.addView(tMat);
            root.addView(tCol);
            root.addView(tWash);
            root.addView(tCau);
            root.addView(tSym);

            dialog.setContentView(root);
            dialog.show();

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "상세를 표시할 수 없습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    private TextView makeTitle(String s) {
        TextView tv = new TextView(requireContext());
        tv.setText(s);
        tv.setTextSize(16);
        tv.setTypeface(tv.getTypeface(), android.graphics.Typeface.BOLD);
        tv.setPadding(0, dp(12), 0, dp(4));
        return tv;
    }

    private TextView makeSub(String s) {
        TextView tv = new TextView(requireContext());
        tv.setText(s);
        tv.setTextSize(12);
        tv.setTextColor(0xFF777777);
        tv.setPadding(0, 0, 0, dp(8));
        return tv;
    }

    private TextView makeLine(String label, String value) {
        TextView tv = new TextView(requireContext());
        tv.setText(label + " : " + (value == null || value.trim().isEmpty() ? "-" : value));
        tv.setTextSize(14);
        tv.setPadding(0, dp(6), 0, 0);
        return tv;
    }

    private int dp(int v) {
        float d = getResources().getDisplayMetrics().density;
        return Math.round(v * d);
    }

    private void loadWeatherAdvice(double nx, double ny) {
        weatherApi.getWeather(nx, ny).enqueue(new Callback<WeatherAdviceResponse>() {
            @Override
            public void onResponse(Call<WeatherAdviceResponse> call, Response<WeatherAdviceResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    WeatherAdviceResponse weather = response.body();
                    String summary = weather.getAdvice().getSummary();
                    tvWeatherTitle.setText("오늘의 추천 세탁 🌤️");
                    tvWeatherDesc.setText(summary);
                    tvGreeting.setText("오늘의 세탁/건조 추천:\n" + summary);

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

    interface WeatherApi {
        @GET("/api/weather/current")
        Call<WeatherAdviceResponse> getWeather(@Query("nx") double nx, @Query("ny") double ny);
>>>>>>> Stashed changes
    }
}
