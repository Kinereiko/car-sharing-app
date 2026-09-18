package com.example.carsharingapp.dto.payment;

import com.example.carsharingapp.model.Payment;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PaymentRequestDto {
    @NotNull
    private Long rentalId;
    @NotNull
    private Payment.Type type;
}
