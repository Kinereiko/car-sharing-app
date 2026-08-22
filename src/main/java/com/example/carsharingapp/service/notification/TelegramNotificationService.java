package com.example.carsharingapp.service.notification;

import com.example.carsharingapp.dto.rental.RentalDto;
import com.example.carsharingapp.exception.NotificationException;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class TelegramNotificationService implements NotificationService {
    private final TelegramBot bot;
    private final String chatId;
    private static final String SEPARATOR = "\n*****\n";

    public TelegramNotificationService(
            @Value("${telegram.bot.token}") String botToken,
            @Value("${telegram.chat.id}") String chatId) {
        this.bot = new TelegramBot(botToken);
        this.chatId = chatId;
    }

    @Override
    public void sendNewRental(RentalDto dto) {
        SendMessage request = new SendMessage(chatId, dto.toString());
        SendResponse response = bot.execute(request);

        if (!response.isOk()) {
            throw new NotificationException("Message delivery failed: " + response.description());
        }
    }

    @Override
    public void sendOverdueRentals(List<RentalDto> dtos) {
        SendMessage request = new SendMessage(chatId, createMessage(dtos));
        SendResponse response = bot.execute(request);

        if (!response.isOk()) {
            throw new NotificationException("Message delivery failed: " + response.description());
        }
    }

    @Override
    public void sendMessage(String message) {
        SendMessage request = new SendMessage(chatId, message);
        SendResponse response = bot.execute(request);

        if (!response.isOk()) {
            throw new NotificationException("Message delivery failed: " + response.description());
        }
    }

    private String createMessage(List<RentalDto> dtos) {
        StringBuilder message = new StringBuilder();
        for (RentalDto dto : dtos) {
            message.append(dto.toString()).append(SEPARATOR);
        }
        message.replace(message.length() - 1 - SEPARATOR.length(),
                message.length() - 1, "");
        return message.toString();
    }
}
