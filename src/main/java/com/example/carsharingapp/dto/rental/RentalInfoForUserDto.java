package com.example.carsharingapp.dto.rental;

import com.example.carsharingapp.dto.car.CarInfoForUserDto;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RentalInfoForUserDto {
    private LocalDate rentalDate;
    private LocalDate returnDate;
    private LocalDate actualReturnDate;
    private CarInfoForUserDto carDto;
}
