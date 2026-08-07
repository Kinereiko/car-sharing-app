package com.example.carsharingapp.dto.car;

import com.example.carsharingapp.model.Car;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class CarInfoForUserDto {
    private String model;
    private String brand;
    private Car.TypeName type;
    private BigDecimal dailyFee;
}
