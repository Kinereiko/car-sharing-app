package com.example.carsharingapp.service.car;

import com.example.carsharingapp.dto.car.CarDto;
import com.example.carsharingapp.dto.car.CarRequestDto;
import com.example.carsharingapp.dto.car.CarShortDto;
import java.util.List;
import org.springframework.data.domain.Pageable;

public interface CarService {
    CarDto save(CarRequestDto requestDto);

    List<CarShortDto> findAll(Pageable pageable);

    CarDto findById(Long id);

    CarDto updateById(Long id, CarRequestDto requestDto);

    void deleteById(Long id);
}
