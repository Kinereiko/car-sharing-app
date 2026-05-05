package com.example.carsharingapp.service.car;

import com.example.carsharingapp.dto.car.CarDto;
import com.example.carsharingapp.dto.car.CarRequestDto;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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
    public CarDto findById(CarRequestDto requestDto) {
        return null;
    }

    @Override
    public CarDto updateById(Long id, CarRequestDto requestDto) {
        return null;
    }

    @Override
    public void deleteById(Long id) {

    }
}
