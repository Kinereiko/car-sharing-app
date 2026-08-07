package com.example.carsharingapp.dto.rental;

import com.example.carsharingapp.dto.car.CarDto;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;

@Getter
@Setter
public class RentalDto {
    private Long id;
    private LocalDate rentalDate;
    private LocalDate returnDate;
    private LocalDate actualReturnDate;
    private CarDto carDto;
    private Long userId;
}
