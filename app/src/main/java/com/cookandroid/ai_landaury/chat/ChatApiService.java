package com.cookandroid.ai_landaury.chat;

import com.cookandroid.ai_landaury.chat.ChatMessageResponse;
import com.cookandroid.ai_landaury.chat.ChatStartResponse;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface ChatApiService {

    @POST("/api/chat/start")
    Call<ChatStartResponse> startConversation();

    @Multipart
    @POST("/api/chat/send")
    Call<ChatMessageResponse> sendMessage(
            @Part MultipartBody.Part conversationId,
            @Part MultipartBody.Part text,
            @Part MultipartBody.Part file // 나중에 이미지 전송도 가능하게
    );
}
