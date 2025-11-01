package com.cookandroid.ai_landaury.camera;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.cookandroid.ai_landaury.R;
import com.cookandroid.ai_landaury.api.ApiService;
import com.cookandroid.ai_landaury.api.LaundryAdviceResponse;
import com.cookandroid.ai_landaury.api.RetrofitClient;

import java.io.File;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoadingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);

        Uri uri = getIntent().getData();
        String path = getIntent().getStringExtra("photo_path");

        if (path == null && uri != null) {
            path = com.cookandroid.ai_landaury.FileUtils.getPath(this, uri);
        }

        if (path == null) { finish(); return; }

        File file = new File(path);
        RequestBody req = RequestBody.create(MediaType.parse("image/*"), file);
        MultipartBody.Part body = MultipartBody.Part.createFormData("file", file.getName(), req);

        ApiService api = RetrofitClient.getClient().create(ApiService.class);
        api.uploadImage(body).enqueue(new Callback<LaundryAdviceResponse>() {
            @Override
            public void onResponse(Call<LaundryAdviceResponse> call, Response<LaundryAdviceResponse> response) {
                Intent i = new Intent(LoadingActivity.this, ResultActivity.class);
                i.setData(uri);
                if (response.isSuccessful() && response.body() != null) {
                    i.putExtra("advice", response.body());
                } else {
                    i.putExtra("error", "응답 실패: " + response.code());
                }
                startActivity(i);
                finish();
            }

            @Override
            public void onFailure(Call<LaundryAdviceResponse> call, Throwable t) {
                Intent i = new Intent(LoadingActivity.this, ResultActivity.class);
                i.setData(uri);
                i.putExtra("error", t.getMessage());
                startActivity(i);
                finish();
            }
        });
    }
}
