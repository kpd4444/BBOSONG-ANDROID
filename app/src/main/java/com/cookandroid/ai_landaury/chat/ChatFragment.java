package com.cookandroid.ai_landaury.chat;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.cookandroid.ai_landaury.R;
import com.cookandroid.ai_landaury.api.RetrofitClient;

import java.io.File;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatFragment extends Fragment {

    private TextView tvChat;
    private EditText etInput;
    private Button btnSend, btnPickImage;
    private ScrollView scrollView;

    private String conversationId = null;
    private ChatApiService chatApi;
    private Uri selectedImageUri = null;

    private final ActivityResultLauncher<Intent> imagePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == requireActivity().RESULT_OK && result.getData() != null) {
                    selectedImageUri = result.getData().getData();
                    tvChat.append("📷 이미지 선택됨\n");
                }
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_chat, container, false);

        tvChat = v.findViewById(R.id.tvChat);
        etInput = v.findViewById(R.id.etInput);
        btnSend = v.findViewById(R.id.btnSend);
        btnPickImage = v.findViewById(R.id.btnPickImage);
        scrollView = v.findViewById(R.id.scrollView);

        chatApi = RetrofitClient.getClient().create(ChatApiService.class);

        // 대화 시작
        chatApi.startConversation().enqueue(new Callback<ChatStartResponse>() {
            @Override
            public void onResponse(Call<ChatStartResponse> call, Response<ChatStartResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    conversationId = response.body().getConversationId();
                    tvChat.append("🧺 뽀송이 챗봇에 오신 걸 환영해요!\n\n");
                }
            }

            @Override
            public void onFailure(Call<ChatStartResponse> call, Throwable t) {
                tvChat.append("❌ 서버 연결 실패\n");
            }
        });

        // 갤러리에서 이미지 선택
        btnPickImage.setOnClickListener(vv -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            imagePickerLauncher.launch(intent);
        });

        // 메시지 전송
        btnSend.setOnClickListener(vv -> sendMessage());

        return v;
    }

    private void sendMessage() {
        String text = etInput.getText().toString().trim();
        if (text.isEmpty() && selectedImageUri == null) return;

        tvChat.append("👤 나: " + (text.isEmpty() ? "[이미지]" : text) + "\n");
        etInput.setText("");

        MultipartBody.Part convPart = MultipartBody.Part.createFormData("conversationId", conversationId != null ? conversationId : "");
        MultipartBody.Part textPart = MultipartBody.Part.createFormData("text", text);

        MultipartBody.Part filePart = null;
        if (selectedImageUri != null) {
            String path = com.cookandroid.ai_landaury.FileUtils.getPath(requireContext(), selectedImageUri);
            if (path != null) {
                File file = new File(path);
                RequestBody req = RequestBody.create(MediaType.parse("image/*"), file);
                filePart = MultipartBody.Part.createFormData("file", file.getName(), req);
            }
        }

        chatApi.sendMessage(convPart, textPart, filePart).enqueue(new Callback<ChatMessageResponse>() {
            @Override
            public void onResponse(Call<ChatMessageResponse> call, Response<ChatMessageResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    tvChat.append("🤖 뽀송이: " + response.body().getAssistantMessage() + "\n\n");
                    scrollView.post(() -> scrollView.fullScroll(View.FOCUS_DOWN));
                } else {
                    tvChat.append("⚠️ 서버 응답 실패\n");
                }
                selectedImageUri = null;
            }

            @Override
            public void onFailure(Call<ChatMessageResponse> call, Throwable t) {
                tvChat.append("❌ 오류: " + t.getMessage() + "\n");
                selectedImageUri = null;
            }
        });
    }
}
