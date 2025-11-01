package com.cookandroid.ai_landaury.camera;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.cookandroid.ai_landaury.MainActivity;
import com.cookandroid.ai_landaury.R;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CameraIntroActivity extends AppCompatActivity {

    private Uri photoUri;
    private File photoFile;

    private final ActivityResultLauncher<String[]> permissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {});

    private final ActivityResultLauncher<Intent> cameraLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK) {
                    if (photoUri != null) {
                        goLoading(photoUri);
                    } else {
                        Toast.makeText(this, "사진 URI를 불러올 수 없습니다.", Toast.LENGTH_SHORT).show();
                    }
                }
            });

    private final ActivityResultLauncher<String> galleryLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) goLoading(uri);
            });

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_intro);

        Button btnStart = findViewById(R.id.btnStartCamera);
        Button btnPick = findViewById(R.id.btnPickGallery);
        ImageView backBtn = findViewById(R.id.btn_back);

        btnStart.setOnClickListener(v -> startCameraFlow());
        btnPick.setOnClickListener(v -> startGalleryFlow());

        backBtn.setOnClickListener(v -> {
            Intent i = new Intent(CameraIntroActivity.this, MainActivity.class);
            i.putExtra("navigate_to", "home");
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(i);
            overridePendingTransition(0, 0);
        });

        ensurePermissions();
    }

    private void ensurePermissions() {
        boolean cam = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED;

        String readPerm = Build.VERSION.SDK_INT >= 33
                ? Manifest.permission.READ_MEDIA_IMAGES
                : Manifest.permission.READ_EXTERNAL_STORAGE;

        boolean read = ContextCompat.checkSelfPermission(this, readPerm)
                == PackageManager.PERMISSION_GRANTED;

        if (!cam || !read) {
            permissionLauncher.launch(new String[]{Manifest.permission.CAMERA, readPerm});
        }
    }

    private void startCameraFlow() {
        try {
            photoFile = createImageFile();
        } catch (IOException e) {
            Toast.makeText(this, "임시 파일 생성 실패", Toast.LENGTH_SHORT).show();
            return;
        }

        if (photoFile == null) {
            Toast.makeText(this, "파일 생성 실패", Toast.LENGTH_SHORT).show();
            return;
        }

        photoUri = FileProvider.getUriForFile(
                this,
                getPackageName() + ".fileprovider",
                photoFile
        );

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
        cameraLauncher.launch(intent);
    }

    private void startGalleryFlow() {
        galleryLauncher.launch("image/*");
    }

    private void goLoading(Uri uri) {
        if (uri == null) {
            Toast.makeText(this, "이미지 URI가 없습니다.", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent i = new Intent(this, LoadingActivity.class);
        i.setData(uri);
        if (photoFile != null) i.putExtra("photo_path", photoFile.getAbsolutePath());
        startActivity(i);
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File storageDir = getCacheDir();
        File images = new File(storageDir, "images");
        if (!images.exists()) images.mkdirs();
        return File.createTempFile("IMG_" + timeStamp + "_", ".jpg", images);
    }
}
