package com.example.carsharingapp.service.car;

import com.example.carsharingapp.dto.car.CarDto;
import com.example.carsharingapp.dto.car.CarRequestDto;
import com.example.carsharingapp.dto.car.CarShortDto;
import com.example.carsharingapp.mapper.CarMapper;
import com.example.carsharingapp.model.Car;
import com.example.carsharingapp.repository.CarRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CarServiceImpl implements CarService {
    private final CarRepository carRepository;
    private final CarMapper carMapper;

    @Override
    public CarDto save(CarRequestDto requestDto) {
        if (requestDto == null) {
            throw new NullPointerException("Request dto must not be null");
        }
        Car car = carMapper.toModel(requestDto);
        return carMapper.toDto(carRepository.save(car));
    }

    @Override
    public List<CarShortDto> findAll(Pageable pageable) {
        return carRepository.findAll(pageable).stream()
                .map(carMapper::toShortDto)
                .toList();
    }

    @Override
    public CarDto findById(Long id) {
        Car car = carRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Can't find car with id: " + id));
        return carMapper.toDto(car);
    }

    @Override
    public CarDto updateById(Long id, CarRequestDto requestDto) {
        Car car = carMapper.toModel(requestDto);
        car.setId(id);
        return carMapper.toDto(carRepository.save(car));
    }

    @Override
    public void deleteById(Long id) {
        carRepository.deleteById(id);
    }
}
