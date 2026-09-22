package com.example.carsharingapp.controller;

import com.example.carsharingapp.dto.car.CarDto;
import com.example.carsharingapp.dto.car.CarRequestDto;
import com.example.carsharingapp.dto.car.CarShortDto;
import com.example.carsharingapp.service.car.CarService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Car management", description = "Endpoints for management cars")
@RequiredArgsConstructor
@RestController
@RequestMapping("/cars")
public class CarController {
    private final CarService carService;

    @GetMapping
    @Operation(summary = "Get all cars", description = "Get a list of all available cars")
    public List<CarShortDto> findAll(Pageable pageable) {
        return carService.findAll(pageable);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get the car", description = "Get the car by id")
    public CarDto findById(@PathVariable Long id) {
        return carService.findById(id);
    }

    @PreAuthorize("hasRole('ROLE_MANAGER')")
    @PostMapping
    @Operation(summary = "Create a car", description = "Create a new car")
    public CarDto save(@RequestBody @Valid CarRequestDto requestDto) {
        return carService.save(requestDto);
    }

    @PreAuthorize("hasRole('ROLE_MANAGER')")
    @PutMapping("/{id}")
    @Operation(summary = "Update the car", description = "Update the information about car by id")
    public CarDto update(@PathVariable Long id,
                         @RequestBody @Valid CarRequestDto requestDto) {
        return carService.updateById(id, requestDto);
    }

    @PreAuthorize("hasRole('ROLE_MANAGER')")
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete the car", description = "Delete the car by id")
    public void delete(@PathVariable Long id) {
        carService.deleteById(id);
    }
}
