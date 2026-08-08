package com.example.carsharingapp.dto.car;

import com.example.carsharingapp.model.Car;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CarInfoForUserDto {
    private String model;
    private String brand;
    private Car.TypeName type;
    private BigDecimal dailyFee;
}
