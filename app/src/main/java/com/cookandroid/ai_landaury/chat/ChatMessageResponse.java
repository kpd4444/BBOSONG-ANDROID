package com.cookandroid.ai_landaury.chat;

import java.io.Serializable;

public class ChatMessageResponse implements Serializable {
    private String conversationId;
    private String assistantMessage;

    public String getConversationId() { return conversationId; }
    public String getAssistantMessage() { return assistantMessage; }
}
