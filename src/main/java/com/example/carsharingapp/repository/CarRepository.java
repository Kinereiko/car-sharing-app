package com.example.carsharingapp.repository;

import com.example.carsharingapp.model.Car;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CarRepository extends JpaRepository<Car, Long> {
}
