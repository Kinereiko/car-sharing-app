package com.example.carsharingapp.service.car;

import com.example.carsharingapp.dto.car.CarDto;
import com.example.carsharingapp.dto.car.CarRequestDto;
import java.util.List;

public interface CarService {
    CarDto save(CarRequestDto requestDto);

    List<CarDto> findAll(List<CarRequestDto> cars);

    CarDto findById(CarRequestDto requestDto);

    CarDto updateById(Long id, CarRequestDto requestDto);

    void deleteById(Long id);
}
