package com.example.carsharingapp.dto.rental;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class RentalReturnDto {
    @NotNull
    private Long id;
    private LocalDate actualReturnDate;
}
