package com.example.carsharingapp.service.notification;

import com.example.carsharingapp.dto.rental.RentalDto;
import com.example.carsharingapp.model.Rental;

public interface NotificationService {
    void sendMessage(RentalDto dto);
}
