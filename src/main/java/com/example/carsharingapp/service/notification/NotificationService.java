package com.example.carsharingapp.service.notification;

import com.example.carsharingapp.dto.rental.RentalDto;

public interface NotificationService {
    void sendNewRental(RentalDto dto);

    void sendMessage(String message);
}
