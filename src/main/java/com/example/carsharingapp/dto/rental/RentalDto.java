package com.example.carsharingapp.dto.rental;

import com.example.carsharingapp.dto.car.CarDto;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class RentalDto {
    private Long id;
    private LocalDate rentalDate;
    private LocalDate returnDate;
    private LocalDate actualReturnDate;
    private CarDto carDto;
    private Long userId;
}
