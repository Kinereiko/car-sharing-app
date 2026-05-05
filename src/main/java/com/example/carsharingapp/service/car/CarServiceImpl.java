package com.example.carsharingapp.service.car;

import com.example.carsharingapp.dto.car.CarDto;
import com.example.carsharingapp.dto.car.CarRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CarServiceImpl implements CarService {

    @Override
    public CarDto save(CarRequestDto requestDto) {
        return null;
    }

    @Override
    public List<CarDto> findAll(List<CarRequestDto> cars) {
        return null;
    }

    @Override
    public CarDto findByID(CarRequestDto requestDto) {
        return null;
    }

    @Override
    public CarDto updateByID(Long id, CarRequestDto requestDto) {
        return null;
    }

    @Override
    public void deleteById(Long id) {

    }
}
