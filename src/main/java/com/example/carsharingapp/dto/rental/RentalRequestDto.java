package com.example.carsharingapp.dto.rental;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class RentalRequestDto {
    @NotNull
    private Long carId;
    @NotNull
    @Min(1)
    @Max(10)
    private int rentedDays;
}
