package com.cookandroid.ai_landaury;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.cookandroid.ai_landaury.chat.ChatFragment;
import com.cookandroid.ai_landaury.camera.CameraIntroActivity;
import com.cookandroid.ai_landaury.home.HomeFragment;
import com.cookandroid.ai_landaury.kakaomap.MapWebViewActivity;
import com.cookandroid.ai_landaury.mypage.MyPageFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.security.MessageDigest;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNav;
    private FloatingActionButton fabCamera;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 뷰 연결
        bottomNav = findViewById(R.id.bottomNavigation);
        fabCamera = findViewById(R.id.fabCamera);

        // 1. 초기 화면 설정 (HomeFragment)
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new HomeFragment())
                    .commit();
        }

        // 2. 하단 네비게이션 아이템 선택 이벤트
        bottomNav.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                selectedFragment = new HomeFragment();
            } else if (id == R.id.nav_map) {
                // 지도는 Activity로 이동 (프래그먼트 교체 아님)
                Intent intent = new Intent(MainActivity.this, MapWebViewActivity.class);
                startActivity(intent);
                return true; // 여기서 종료
            } else if (id == R.id.nav_placeholder) {
                // 🌟 가운데 빈 공간 클릭 시 아무 동작 안 함
                return false;
            } else if (id == R.id.nav_chat) {
                selectedFragment = new ChatFragment();
            } else if (id == R.id.nav_mypage) {
                selectedFragment = new MyPageFragment();
            }

            // 프래그먼트 교체 실행
            if (selectedFragment != null) {
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, selectedFragment)
                        .commit();
                return true;
            }
            return false;
        });

        // 3. 📸 중앙 카메라 버튼 클릭 이벤트
        fabCamera.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, CameraIntroActivity.class);
            startActivity(intent);
        });

        // 4. 키해시 로그 (카카오맵용)
        getAppKeyHash();
    }

    // 키해시 구하는 함수 분리
    private void getAppKeyHash() {
        try {
            PackageInfo info = getPackageManager().getPackageInfo(
                    getPackageName(),
                    PackageManager.GET_SIGNATURES
            );
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                String keyHash = Base64.encodeToString(md.digest(), Base64.NO_WRAP);
                Log.d("KeyHash", keyHash);
            }
        } catch (Exception e) {
            Log.e("KeyHash", "해시 키를 가져올 수 없습니다.", e);
        }
    }
}