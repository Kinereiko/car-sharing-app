package com.example.carsharingapp.service.notification;

import com.example.carsharingapp.dto.rental.RentalDto;
import java.util.List;

public interface NotificationService {
    void sendNewRental(RentalDto dto);

    void sendOverdueRentals(List<RentalDto> rentals);

    void sendMessage(String message);
}
