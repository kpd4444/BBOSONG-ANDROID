package com.cookandroid.ai_landaury.kakaomap;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.cookandroid.ai_landaury.R;
import com.kakao.vectormap.KakaoMap;
import com.kakao.vectormap.MapView;
import com.kakao.vectormap.MapPoint;
import com.kakao.vectormap.label.Label;
import com.kakao.vectormap.label.LabelLayer;

import org.json.JSONArray;
import org.json.JSONObject;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class LaundryMapFragment extends Fragment {

    private static final String BASE_URL = "http://10.0.2.2:8080"; // 에뮬레이터 -> 로컬 서버 접속용 IP
    private MapView mapView;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_laundry_map, container, false);
        mapView = view.findViewById(R.id.mapView);

        // 지도 로드 이벤트
        mapView.setMapViewEventListener((KakaoMap kakaoMap) -> {
            Log.d("Map", "✅ 카카오맵 로드 완료");

            // 위치 권한 확인
            if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(requireActivity(),
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 100);
                return;
            }

            mapView.setCurrentLocationTrackingMode(MapView.CurrentLocationTrackingMode.TrackingModeOnWithoutHeading);

            // 테스트용 좌표 (송도)
            double x = 126.6541;
            double y = 37.3847;

            requestLaundryData(kakaoMap, x, y);
        });

        return view;
    }

    private void requestLaundryData(KakaoMap kakaoMap, double x, double y) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(ScalarsConverterFactory.create())
                .client(new OkHttpClient())
                .build();

        RetrofitService service = retrofit.create(RetrofitService.class);

        service.getLaundries(x, y).enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        JSONObject json = new JSONObject(response.body());
                        JSONArray docs = json.getJSONArray("documents");

                        LabelLayer labelLayer = kakaoMap.getLabelManager().getLayer();

                        for (int i = 0; i < docs.length(); i++) {
                            JSONObject place = docs.getJSONObject(i);
                            double px = place.getDouble("x");
                            double py = place.getDouble("y");
                            String name = place.getString("place_name");

                            labelLayer.addLabel(MapPoint.mapPointWithGeoCoord(py, px), name);
                        }

                        Log.d("Map", "✅ 세탁소 마커 " + docs.length() + "개 표시 완료");

                    } catch (Exception e) {
                        Log.e("Map", "❌ JSON 파싱 오류", e);
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                Log.e("Map", "❌ 백엔드 통신 실패: " + t.getMessage());
            }
        });
    }
}
