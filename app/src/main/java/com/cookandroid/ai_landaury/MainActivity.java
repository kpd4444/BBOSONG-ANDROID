package com.cookandroid.ai_landaury;

//import androidx.appcompat.app.AppCompatActivity;
//import android.content.Intent;
//import android.os.Bundle;
//import android.widget.TextView;
//import com.google.android.material.bottomnavigation.BottomNavigationView;
//import java.text.SimpleDateFormat;
//import java.util.Date;
//import java.util.Locale;
//import com.cookandroid.ai_landaury.camera.CameraIntroActivity;
//
//public class MainActivity extends AppCompatActivity {
//
//    private TextView tvDate;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
//
//        tvDate = findViewById(R.id.tvDate);
//        BottomNavigationView bottomNavigation = findViewById(R.id.bottomNavigation);
//
//        // 오늘 날짜 세팅
//        String today = new SimpleDateFormat("yyyy.MM.dd.EEE", Locale.KOREAN).format(new Date());
//        tvDate.setText(today);
//
//        // 하단 네비게이션 클릭 이벤트
//        bottomNavigation.setOnItemSelectedListener(item -> {
//            int id = item.getItemId();
//
//            if (id == R.id.nav_home) {
//                // 홈
//            } else if (id == R.id.nav_map) {
//                // 지도 화면 이동
//            } else if (id == R.id.nav_camera) {
//                // 📸 카메라 인트로 화면 이동
//                Intent intent = new Intent(MainActivity.this, CameraIntroActivity.class);
//                startActivity(intent);
//            } else if (id == R.id.nav_chat) {
//                Intent intent = new Intent(MainActivity.this, com.cookandroid.ai_landaury.chat.ChatActivity.class);
//                startActivity(intent);
//            } else if (id == R.id.nav_mypage) {
//                Intent intent = new Intent(MainActivity.this, com.cookandroid.ai_landaury.mypage.MyPageActivity.class);
//                startActivity(intent);
//            }
//            return true;
//        });
//    }
//    @Override
//    protected void onNewIntent(Intent intent) {
//        super.onNewIntent(intent);
//
//        if (intent != null && "home".equals(intent.getStringExtra("navigate_to"))) {
//            BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);
//            bottomNav.setSelectedItemId(R.id.nav_home);
//        }
//    }
//}
import static android.os.Build.VERSION_CODES.R;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.cookandroid.ai_landaury.chat.ChatFragment;
import com.cookandroid.ai_landaury.camera.CameraFragment;
import com.cookandroid.ai_landaury.mypage.MyPageFragment;
import com.cookandroid.ai_landaury.home.HomeFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // 하단 네비 포함된 기본 레이아웃

        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);

        // ✅ 앱 처음 실행 시 홈 프래그먼트 띄움
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new HomeFragment())
                    .commit();
        }

        // ✅ 네비게이션 탭 선택 이벤트
        bottomNav.setOnItemSelectedListener(item -> {
            Fragment selected = null;

            int id = item.getItemId();
            if (id == R.id.nav_home) selected = new HomeFragment();
            else if (id == R.id.nav_camera) selected = new CameraFragment();
            else if (id == R.id.nav_chat) selected = new ChatFragment();
            else if (id == R.id.nav_mypage) selected = new MyPageFragment();
            else if (id == R.id.nav_map) selected = new com.cookandroid.ai_landaury.kakaomap.LaundryMapFragment();

            if (selected != null) {
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, selected)
                        .commit();
                return true;
            }
            return false;
        });
    }
}