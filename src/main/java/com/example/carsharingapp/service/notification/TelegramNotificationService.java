package com.example.carsharingapp.service.notification;

import com.example.carsharingapp.dto.rental.RentalDto;
import com.example.carsharingapp.exception.NotificationException;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;
import org.springframework.stereotype.Service;

@Service
public class TelegramNotificationService implements NotificationService {
    private static final String BOT_TOKEN = "-";
    private static final String CHAT_ID = "-";
    private final TelegramBot bot = new TelegramBot(BOT_TOKEN);

    @Override
    public void sendMessage(RentalDto dto) {
        SendMessage request = new SendMessage(CHAT_ID, dto.toString());
        SendResponse response = bot.execute(request);

        if (!response.isOk()) {
            throw new NotificationException("Message delivery failed: " + response.description());
        }
    }
}
