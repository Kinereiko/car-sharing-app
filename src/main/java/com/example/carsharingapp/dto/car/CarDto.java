package com.example.carsharingapp.dto.car;

import com.example.carsharingapp.model.Car;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class CarDto {
    private Long id;
    private String model;
    private String brand;
    private Car.TypeName type;
    private int inventory;
    private BigDecimal dailyFee;
}
