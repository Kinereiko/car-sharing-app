package com.example.carsharingapp.dto.rental;

import com.example.carsharingapp.dto.car.CarInfoForUserDto;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class RentalInfoForUserDto {
    private LocalDate rentalDate;
    private LocalDate returnDate;
    private LocalDate actualReturnDate;
    private CarInfoForUserDto carDto;
}
