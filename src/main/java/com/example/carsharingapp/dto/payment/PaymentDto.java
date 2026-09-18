package com.example.carsharingapp.dto.payment;

import com.example.carsharingapp.model.Payment;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PaymentDto {
    private Long id;
    private Payment.Status status;
    private Payment.Type type;
    private Long rentalId;
    private String sessionUrl;
    private String sessionId;
    private BigDecimal totalPrice;
}
