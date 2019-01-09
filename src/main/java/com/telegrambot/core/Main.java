package com.telegrambot.core;

import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;

import static com.telegrambot.core.MafiaBot.availableRoles;

public class Main {
    public static void main(String[] args) {
        ApiContextInitializer.init();
        TelegramBotsApi botsApi = new TelegramBotsApi();
        availableRoles.add("Mafia");
        availableRoles.add("Bürger");
        availableRoles.add("Leibwächter");
        availableRoles.add("Detektiv");
        availableRoles.add("Hexe");
        availableRoles.add("Amor");
        availableRoles.add("Terrorist");
        availableRoles.add("Drogendealer");

        try {
            botsApi.registerBot(new MafiaBot());
        } catch (TelegramApiRequestException e) {
            e.printStackTrace();
        }
    }
}
