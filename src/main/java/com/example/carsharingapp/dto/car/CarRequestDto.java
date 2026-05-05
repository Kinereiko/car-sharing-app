package com.example.carsharingapp.dto.car;

import com.example.carsharingapp.model.Car;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;

@Getter
@Setter
public class CarRequestDto {
    @NotBlank
    private String model;
    @NotBlank
    private String brand;
    @NotNull
    private Car.TypeName type;
    @NotNull
    private int inventory;
    @NotNull
    @Min(10)
    private BigDecimal dailyFee;
}
