package com.cookandroid.ai_landaury.camera;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.cookandroid.ai_landaury.MainActivity;
import com.cookandroid.ai_landaury.R;
import com.cookandroid.ai_landaury.api.LaundryAdviceResponse;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.List;
import java.util.stream.Collectors;

public class ResultActivity extends AppCompatActivity {

    private TextView tvTitle, tvMaterial, tvColor, tvWash, tvCaution;
    private ImageView iv;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        iv = findViewById(R.id.ivResult);
        tvTitle = findViewById(R.id.tvItemName);
        tvMaterial = findViewById(R.id.tvMaterial);
        tvColor = findViewById(R.id.tvColor);
        tvWash = findViewById(R.id.tvWash);
        tvCaution = findViewById(R.id.tvCaution);

        Button btnSearchAgain = findViewById(R.id.btnSearchAgain);
        Button btnGoHome = findViewById(R.id.btnGoHome);
        ImageView backBtn = findViewById(R.id.btn_back);
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);

        backBtn.setOnClickListener(v -> {
            Intent intent = new Intent(ResultActivity.this, CameraIntroActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            overridePendingTransition(0, 0);
        });

        bottomNav.setSelectedItemId(R.id.nav_camera);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            Intent intent = new Intent(ResultActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

            if (id == R.id.nav_home) {
                intent.putExtra("navigate_to", "home");
            } else if (id == R.id.nav_chat) {
                intent.putExtra("navigate_to", "chat");
            } else if (id == R.id.nav_mypage) {
                intent.putExtra("navigate_to", "mypage");
            } else return false;

            startActivity(intent);
            overridePendingTransition(0, 0);
            return true;
        });

        Uri uri = getIntent().getData();
        if (uri != null) iv.setImageURI(uri);

        String err = getIntent().getStringExtra("error");
        LaundryAdviceResponse advice = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            advice = getIntent().getSerializableExtra("advice", LaundryAdviceResponse.class);
        } else {
            Object obj = getIntent().getSerializableExtra("advice");
            if (obj instanceof LaundryAdviceResponse) advice = (LaundryAdviceResponse) obj;
        }

        if (advice != null) {
            bindAdvice(advice);
        } else {
            tvTitle.setText("분석 실패");
            tvMaterial.setText("소재: -");
            tvColor.setText("색상: -");
            tvWash.setText("세탁 방법: " + (err != null ? err : "-"));
            tvCaution.setText("주의사항: -");
        }

        btnSearchAgain.setOnClickListener(v -> {
            Intent intent = new Intent(ResultActivity.this, CameraIntroActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            overridePendingTransition(0, 0);
        });

        btnGoHome.setOnClickListener(v -> {
            Intent i = new Intent(ResultActivity.this, MainActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            i.putExtra("navigate_to", "home");
            startActivity(i);
            finish();
        });
    }

    private void bindAdvice(LaundryAdviceResponse advice) {
        tvTitle.setText("분석된 의류");
        tvMaterial.setText("소재: " + nz(advice.getMaterial()));
        tvColor.setText("색상: " + nz(advice.getColor()));
        tvWash.setText("세탁 방법: " + nz(advice.getWashingMethod()));
        tvCaution.setText("주의사항: " + nz(advice.getCautions()));

        List<String> symbols = advice.getRecommendedSymbols();
        if (symbols != null && !symbols.isEmpty()) {
            String joined = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N
                    ? symbols.stream().collect(Collectors.joining(", "))
                    : joinLegacy(symbols);
        }
    }

    private String nz(String s) {
        return (s == null || s.trim().isEmpty()) ? "-" : s;
    }

    private String joinLegacy(List<String> list) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < list.size(); i++) {
            sb.append(list.get(i));
            if (i < list.size() - 1) sb.append(", ");
        }
        return sb.toString();
    }
}
