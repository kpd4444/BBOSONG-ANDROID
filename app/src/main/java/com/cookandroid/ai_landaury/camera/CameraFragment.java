package com.cookandroid.ai_landaury.camera;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.cookandroid.ai_landaury.R;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CameraFragment extends Fragment {

    private Uri photoUri;
    private File photoFile;

    private final ActivityResultLauncher<String[]> permissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {});

    private final ActivityResultLauncher<Intent> cameraLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == getActivity().RESULT_OK && photoUri != null) {
                    goLoading(photoUri);
                }
            });

    private final ActivityResultLauncher<String> galleryLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) goLoading(uri);
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_camera, container, false);

        Button btnStart = v.findViewById(R.id.btnStartCamera);
        Button btnPick = v.findViewById(R.id.btnPickGallery);

        btnStart.setOnClickListener(view -> startCameraFlow());
        btnPick.setOnClickListener(view -> startGalleryFlow());

        ensurePermissions();
        return v;
    }

    private void ensurePermissions() {
        boolean cam = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED;
        String readPerm = Build.VERSION.SDK_INT >= 33 ? Manifest.permission.READ_MEDIA_IMAGES
                : Manifest.permission.READ_EXTERNAL_STORAGE;
        boolean read = ContextCompat.checkSelfPermission(requireContext(), readPerm)
                == PackageManager.PERMISSION_GRANTED;

        if (!cam || !read) {
            permissionLauncher.launch(new String[]{Manifest.permission.CAMERA, readPerm});
        }
    }

    private void startCameraFlow() {
        try {
            photoFile = createImageFile();
        } catch (IOException e) {
            Toast.makeText(requireContext(), "임시 파일 생성 실패", Toast.LENGTH_SHORT).show();
            return;
        }
        photoUri = FileProvider.getUriForFile(
                requireContext(), requireContext().getPackageName() + ".fileprovider", photoFile);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
        cameraLauncher.launch(intent);
    }

    private void startGalleryFlow() {
        galleryLauncher.launch("image/*");
    }

    private void goLoading(Uri uri) {
        Intent i = new Intent(requireContext(), LoadingActivity.class);
        i.setData(uri);
        startActivity(i);
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File storageDir = requireContext().getCacheDir();
        File images = new File(storageDir, "images");
        if (!images.exists()) images.mkdirs();
        return File.createTempFile("IMG_" + timeStamp + "_", ".jpg", images);
    }
}
