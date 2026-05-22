package com.example.carsharingapp.repository;

import com.example.carsharingapp.model.Car;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface CarRepository extends JpaRepository<Car, Long>,
        PagingAndSortingRepository<Car, Long> {
}
