package com.example.carsharingapp.dto.rental;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RentalReturnDto {
    @NotNull
    private Long id;
    private LocalDate actualReturnDate;
}
