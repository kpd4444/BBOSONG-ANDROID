package com.cookandroid.ai_landaury.kakaomap;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.cookandroid.ai_landaury.R;
import com.kakao.vectormap.KakaoMap;
import com.kakao.vectormap.KakaoMapReadyCallback;
import com.kakao.vectormap.MapLifeCycleCallback;
import com.kakao.vectormap.MapView;
import com.kakao.vectormap.KakaoMapSdk;
import com.kakao.vectormap.camera.CameraUpdateFactory;
import com.kakao.vectormap.LatLng;

public class LaundryMapActivity extends AppCompatActivity {

    private MapView mapView;
    private KakaoMap kakaoMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        KakaoMapSdk.init(
                getApplicationContext(),
                getString(R.string.kakao_map_api_key)
        );

        setContentView(R.layout.fragment_laundry_map);

        mapView = findViewById(R.id.map_view);

        mapView.start(
                new MapLifeCycleCallback() {
                    @Override
                    public void onMapDestroy() {
                        Log.d("KakaoMap", "onMapDestroy");
                    }

                    @Override
                    public void onMapError(@NonNull Exception error) {
                        Log.e("KakaoMap", "onMapError", error);
                    }
                },
                new KakaoMapReadyCallback() {
                    @Override
                    public void onMapReady(@NonNull KakaoMap map) {
                        kakaoMap = map;
                        double lat = 37.3847, lng = 126.6541; // 인천 송도
                        kakaoMap.moveCamera(
                                CameraUpdateFactory.newCenterPosition(LatLng.from(lat, lng))
                        );
                        Log.d("KakaoMap", "Map ready & centered");
                    }
                }
        );
    }
}