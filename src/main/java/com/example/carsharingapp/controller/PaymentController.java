package com.example.carsharingapp.controller;

import com.example.carsharingapp.dto.payment.PaymentDto;
import com.example.carsharingapp.dto.payment.PaymentRequestDto;
import com.example.carsharingapp.service.payment.PaymentService;
import com.stripe.exception.StripeException;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/payments")
public class PaymentController {
    private final PaymentService paymentService;

    @PreAuthorize("hasRole('ROLE_MANAGER')")
    @GetMapping("/{id}")
    public List<PaymentDto> findAllByUserId(@PathVariable Long id) {
        return paymentService.getPayments(id);
    }

    @PostMapping
    public PaymentDto create(@RequestBody @Valid PaymentRequestDto requestDto,
                          Authentication authentication) throws StripeException {
        return paymentService.create(requestDto, authentication);
    }

    @GetMapping("/success")
    public PaymentDto handleSuccessPayment(@RequestParam String sessionId) throws StripeException {
        return paymentService.handleSuccess(sessionId);
    }


    @GetMapping("/cancel")
    public String handleCancelPayments() {
        return paymentService.handleCancel();
    }
}
