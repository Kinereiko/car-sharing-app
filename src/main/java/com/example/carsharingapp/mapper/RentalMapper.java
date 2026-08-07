package com.example.carsharingapp.mapper;

import com.example.carsharingapp.config.MapperConfig;
import com.example.carsharingapp.dto.rental.RentalDto;
import com.example.carsharingapp.dto.rental.RentalInfoForUserDto;
import com.example.carsharingapp.model.Rental;
import com.example.carsharingapp.repository.CarRepository;
import org.mapstruct.*;

@Mapper(config = MapperConfig.class, uses = CarMapper.class)
public interface RentalMapper {
    @Mapping(target= "carDto", source = "car")
    @Mapping(target = "userId", source = "user.id")
    RentalDto toDto(Rental rental);

    @Mapping(target = "carDto", source = "car")
    RentalInfoForUserDto toUserInfoDto(Rental rental);
}
