package com.cookandroid.ai_landaury.camera;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
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

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
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

            if (id == R.id.nav_home) intent.putExtra("navigate_to", "home");
            else if (id == R.id.nav_chat) intent.putExtra("navigate_to", "chat");
            else if (id == R.id.nav_mypage) intent.putExtra("navigate_to", "mypage");
            else return false;

            startActivity(intent);
            overridePendingTransition(0, 0);
            return true;
        });

        // 입력 이미지(포토피커/카메라)
        Uri pickedUri = getIntent().getData();

        // 분석 결과 객체
        String err = getIntent().getStringExtra("error");
        LaundryAdviceResponse advice = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            advice = getIntent().getSerializableExtra("advice", LaundryAdviceResponse.class);
        } else {
            Object obj = getIntent().getSerializableExtra("advice");
            if (obj instanceof LaundryAdviceResponse) advice = (LaundryAdviceResponse) obj;
        }

        // 원본 이미지 복사 + 썸네일 생성 → 절대경로
        String fullPath = copyToInternal(pickedUri);            // 예: /data/.../files/pic_xxx.jpg
        String thumbPath = createThumbnail(fullPath, 256);      // 예: /data/.../cache/thumb_xxx.jpg

        // 화면 프리뷰: 썸네일/원본 중 가능한 것
        if (!thumbPath.isEmpty()) iv.setImageBitmap(BitmapFactory.decodeFile(thumbPath));
        else if (!fullPath.isEmpty()) iv.setImageBitmap(BitmapFactory.decodeFile(fullPath));

        if (advice != null) {
            bindAdvice(advice);

            // 표시용 이름 생성(소재/색상 기반)
            String displayName = buildDisplayName(advice);

            // 저장 (썸네일 경로를 imgUri에, 원본은 fullPath에 저장)
            saveResult(
                    displayName,
                    thumbPath,                         // imgUri(=썸네일 파일 경로, URI 아님)
                    R.drawable.ic_clothes_placeholder,
                    fullPath,                          // 상세용
                    nz(advice.getMaterial()),
                    nz(advice.getColor()),
                    nz(advice.getWashingMethod()),
                    nz(advice.getCautions()),
                    advice.getRecommendedSymbols()
            );
        } else {
            tvTitle.setText("분석 실패");
            tvMaterial.setText("소재: -");
            tvColor.setText("색상: -");
            tvWash.setText("세탁 방법: " + (err != null ? err : "-"));
            tvCaution.setText("주의사항: -");
            // 실패 저장은 필요 시 사용
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
    }

    private String nz(String s) {
        return (s == null || s.trim().isEmpty()) ? "-" : s;
    }

    private String buildDisplayName(LaundryAdviceResponse advice) {
        String material = nz(advice.getMaterial());
        String color = nz(advice.getColor());
        if (!"-".equals(material) && !"-".equals(color)) return material + " / " + color;
        if (!"-".equals(material)) return material;
        if (!"-".equals(color)) return color;
        return "의류";
    }

    /** 포토피커/카메라 URI → 내부 저장소 파일 복사. 실패 시 "" */
    private String copyToInternal(Uri source) {
        if (source == null) return "";
        try (InputStream in = getContentResolver().openInputStream(source)) {
            if (in == null) return "";
            File out = new File(getFilesDir(), "pic_" + System.currentTimeMillis() + ".jpg");
            try (FileOutputStream fos = new FileOutputStream(out)) {
                byte[] buf = new byte[8192];
                int n;
                while ((n = in.read(buf)) > 0) fos.write(buf, 0, n);
            }
            return out.getAbsolutePath(); // 절대경로 (URI 아님)
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    /** 원본 파일 경로로부터 썸네일 생성(가로 기준 px). 실패 시 "" */
    private String createThumbnail(String srcPath, int targetWidthPx) {
        try {
            if (srcPath == null || srcPath.isEmpty()) return "";
            BitmapFactory.Options opts = new BitmapFactory.Options();
            opts.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(srcPath, opts);
            int inSample = Math.max(1, opts.outWidth / Math.max(1, targetWidthPx));

            BitmapFactory.Options opts2 = new BitmapFactory.Options();
            opts2.inSampleSize = inSample;
            Bitmap bmp = BitmapFactory.decodeFile(srcPath, opts2);
            if (bmp == null) return "";

            File out = new File(getCacheDir(), "thumb_" + System.currentTimeMillis() + ".jpg");
            try (FileOutputStream fos = new FileOutputStream(out)) {
                bmp.compress(Bitmap.CompressFormat.JPEG, 88, fos);
            }
            return out.getAbsolutePath();
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    /** 최근 결과 저장: 리스트용 + 상세용 전체 필드 포함, 최대 5개 유지 */
    private void saveResult(String name,
                            String thumbPath,
                            int imgResId,
                            String fullPath,
                            String material,
                            String color,
                            String washingMethod,
                            String cautions,
                            List<String> symbols) {

        SharedPreferences prefs = getSharedPreferences("recent_results", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        try {
            String json = prefs.getString("results", "[]");
            JSONArray arr = new JSONArray(json);

            String date = new SimpleDateFormat("yyyy.MM.dd", Locale.getDefault()).format(new Date());

            JSONObject item = new JSONObject();
            // 리스트용
            item.put("name", name);
            item.put("date", date);
            item.put("imgUri", thumbPath);      // ✅ 파일 "경로" 저장 (URI 아님)
            item.put("imgResId", imgResId);
            // 상세용
            item.put("fullPath", fullPath);
            item.put("material", material);
            item.put("color", color);
            item.put("washingMethod", washingMethod);
            item.put("cautions", cautions);
            if (symbols != null) item.put("symbols", new JSONArray(symbols));

            // 최신 5개 유지
            JSONArray out = new JSONArray();
            out.put(item);
            for (int i = 0; i < Math.min(4, arr.length()); i++) out.put(arr.get(i));

            editor.putString("results", out.toString());
            editor.apply();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
