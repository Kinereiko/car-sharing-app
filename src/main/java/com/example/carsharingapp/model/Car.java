package com.example.carsharingapp.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;

@Entity
@Getter
@Setter
@Table(name = "cars")
public class Car {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String model;
    @Column(nullable = false)
    private String brand;
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private TypeName type;
    @Column(nullable = false)
    private int inventory;
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal dailyFee;
    public enum TypeName {
        SEDAN,
        SUV,
        HATCHBACK,
        UNIVERSAL
    }
}
