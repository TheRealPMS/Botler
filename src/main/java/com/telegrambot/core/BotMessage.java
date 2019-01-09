package com.telegrambot.core;

import com.telegrambot.net.HttpClient;

public class BotMessage {

    private static final String TELEGRAM_RESOURCE = "https://api.telegram.org/bot757130944:AAGLQzA69X4XpB3Flf8Ox3lajC8qUbXHkHo";
    private final int chat_id;
    private final String text;

    public BotMessage(int chat_id, String text) {
        this.chat_id = chat_id;
        this.text = text;
    }

    public int getChat_id() {
        return chat_id;
    }

    public String getText() {
        return text;
    }

    public void send() {
        HttpClient.POST(TELEGRAM_RESOURCE + "/sendMessage", this);
    }
}
