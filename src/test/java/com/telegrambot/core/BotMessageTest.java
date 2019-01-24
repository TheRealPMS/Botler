package com.telegrambot.core;

import org.junit.Test;

import static org.junit.Assert.*;

public class BotMessageTest {

    @Test
    public void send() {
        new BotMessage(-364537563, "Was geht ab?").send();
    }
}