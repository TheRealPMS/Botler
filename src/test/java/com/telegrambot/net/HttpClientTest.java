package com.telegrambot.net;

import com.telegrambot.core.BotMessage;
import org.junit.Test;

import static org.junit.Assert.*;

public class HttpClientTest {

    @Test
    public void POST() {
        HttpClient.POST(
                "https://api.telegram.org/bot757130944:AAGLQzA69X4XpB3Flf8Ox3lajC8qUbXHkHo/sendMessage",
                new BotMessage(-364537563, "Ruhe auf den billigen Plätzen")
        );
    }
}