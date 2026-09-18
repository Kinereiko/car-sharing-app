package com.example.carsharingapp.service.payment;

import com.example.carsharingapp.dto.payment.PaymentDto;
import com.example.carsharingapp.dto.payment.PaymentRequestDto;
import com.stripe.exception.StripeException;
import java.util.List;
import org.springframework.security.core.Authentication;

public interface PaymentService {
    List<PaymentDto> getPayments(Long userId);

    PaymentDto create(PaymentRequestDto requestDto,
                      Authentication authentication) throws StripeException;

    PaymentDto handleSuccess(String sessionId) throws StripeException;

    String handleCancel();
}
