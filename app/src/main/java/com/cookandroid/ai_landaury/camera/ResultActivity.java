package com.cookandroid.ai_landaury.camera;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.cookandroid.ai_landaury.MainActivity;
import com.cookandroid.ai_landaury.R;
import com.cookandroid.ai_landaury.api.LaundryAdviceResponse;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

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

        backBtn.setOnClickListener(v -> finish());

        // 🌟 [핵심 변경] 저장된 아이템인지 확인
        boolean isSavedItem = getIntent().getBooleanExtra("isSavedItem", false);

        if (isSavedItem) {
            // [CASE 1] 리스트에서 클릭해서 들어온 경우 (저장된 정보 표시)
            String name = getIntent().getStringExtra("name");
            String material = getIntent().getStringExtra("material");
            String color = getIntent().getStringExtra("color");
            String washingMethod = getIntent().getStringExtra("washingMethod");
            String cautions = getIntent().getStringExtra("cautions");
            String imageUri = getIntent().getStringExtra("imageUri");

            // 텍스트 세팅
            tvTitle.setText(name != null ? name : "상세 정보");
            tvMaterial.setText(material != null ? material : "-");
            tvColor.setText(color != null ? color : "-");
            tvWash.setText(washingMethod != null ? washingMethod : "-");
            tvCaution.setText(cautions != null ? cautions : "-");

            // 이미지 세팅
            if (imageUri != null && !imageUri.isEmpty()) {
                String path = imageUri;
                if (path.startsWith("file://")) path = Uri.parse(path).getPath();

                File imgFile = new File(path);
                if (imgFile.exists()) {
                    iv.setImageBitmap(BitmapFactory.decodeFile(imgFile.getAbsolutePath()));
                } else {
                    iv.setImageResource(R.drawable.ic_clothes_placeholder);
                }
            } else {
                iv.setImageResource(R.drawable.ic_clothes_placeholder);
            }

            // 저장된 건 '다시 검색' 버튼 대신 '닫기' 기능으로 변경
            btnSearchAgain.setText("닫기");
            btnSearchAgain.setOnClickListener(v -> finish());

        } else {
            // [CASE 2] 카메라/갤러리에서 새로 분석해서 들어온 경우 (기존 로직)
            Uri pickedUri = getIntent().getData();
            String err = getIntent().getStringExtra("error");

            LaundryAdviceResponse advice = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                advice = getIntent().getSerializableExtra("advice", LaundryAdviceResponse.class);
            } else {
                Object obj = getIntent().getSerializableExtra("advice");
                if (obj instanceof LaundryAdviceResponse) advice = (LaundryAdviceResponse) obj;
            }

            String fullPath = copyToInternal(pickedUri);
            String thumbPath = createThumbnail(fullPath, 256);

            if (!thumbPath.isEmpty()) iv.setImageBitmap(BitmapFactory.decodeFile(thumbPath));
            else if (!fullPath.isEmpty()) iv.setImageBitmap(BitmapFactory.decodeFile(fullPath));

            if (advice != null) {
                bindAdvice(advice);
                String displayName = buildDisplayName(advice);

                // 결과 저장
                saveResult(
                        displayName,
                        thumbPath,
                        R.drawable.ic_clothes_placeholder,
                        fullPath,
                        nz(advice.getMaterial()),
                        nz(advice.getColor()),
                        nz(advice.getWashingMethod()),
                        nz(advice.getCautions()),
                        advice.getRecommendedSymbols()
                );
            } else {
                tvTitle.setText("분석 실패");
                tvMaterial.setText("-");
                tvColor.setText("-");
                tvWash.setText(" " + (err != null ? err : "-"));
                tvCaution.setText("-");
            }

            btnSearchAgain.setOnClickListener(v -> {
                Intent intent = new Intent(ResultActivity.this, CameraIntroActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
            });
        }

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
        tvMaterial.setText(nz(advice.getMaterial()));
        tvColor.setText(nz(advice.getColor()));
        tvWash.setText(nz(advice.getWashingMethod()));
        tvCaution.setText(nz(advice.getCautions()));
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
            return out.getAbsolutePath();
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

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

    private void saveResult(String name, String thumbPath, int imgResId, String fullPath,
                            String material, String color, String washingMethod,
                            String cautions, List<String> symbols) {

        SharedPreferences prefs = getSharedPreferences("recent_results", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        try {
            String json = prefs.getString("results", "[]");
            JSONArray arr = new JSONArray(json);

            String date = new SimpleDateFormat("yyyy.MM.dd", Locale.getDefault()).format(new Date());

            JSONObject item = new JSONObject();
            item.put("name", name);
            item.put("date", date);
            item.put("imgUri", thumbPath);
            item.put("imgResId", imgResId);

            // 상세 정보 저장
            item.put("fullPath", fullPath);
            item.put("material", material);
            item.put("color", color);
            item.put("washingMethod", washingMethod);
            item.put("cautions", cautions);
            if (symbols != null) item.put("symbols", new JSONArray(symbols));

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