package com.example.carsharingapp.service.schedule;

import com.example.carsharingapp.mapper.RentalMapper;
import com.example.carsharingapp.model.Rental;
import com.example.carsharingapp.repository.RentalRepository;
import com.example.carsharingapp.service.notification.NotificationService;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ScheduleServiceImpl implements ScheduleService {
    private final RentalRepository rentalRepository;
    private final RentalMapper rentalMapper;
    private final NotificationService notificationService;

    private static final String MESSAGE = "No rentals overdue today!";
    private static final String DAILY_CHECKING_TIME = "0 49 22 * * *";

    @Scheduled(cron = DAILY_CHECKING_TIME)
    @Override
    public void checkOverdueRentals() {
        List<Rental> rentals = rentalRepository.findAllOverdue(LocalDate.now());
        if (rentals.isEmpty()) {
            notificationService.sendMessage(MESSAGE);
        } else {
            notificationService.sendOverdueRentals(rentals.stream()
                    .map(rentalMapper::toDto)
                    .toList());
        }
    }
}
