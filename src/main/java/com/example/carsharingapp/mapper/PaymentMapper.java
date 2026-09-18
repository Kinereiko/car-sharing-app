package com.example.carsharingapp.mapper;

import com.example.carsharingapp.config.MapperConfig;
import com.example.carsharingapp.dto.payment.PaymentDto;
import com.example.carsharingapp.model.Payment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = MapperConfig.class, uses = RentalMapper.class)
public interface PaymentMapper {
    @Mapping(target = "rentalId", source = "rental.id")
    PaymentDto toDto(Payment payment);
}
