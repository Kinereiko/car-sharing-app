package com.example.carsharingapp.controller;

import com.example.carsharingapp.dto.payment.PaymentDto;
import com.example.carsharingapp.dto.payment.PaymentRequestDto;
import com.example.carsharingapp.service.payment.PaymentService;
import com.stripe.exception.StripeException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Payment management", description = "Endpoints for management payments")
@RequiredArgsConstructor
@RestController
@RequestMapping("/payments")
public class PaymentController {
    private final PaymentService paymentService;

    @PreAuthorize("hasRole('ROLE_MANAGER')")
    @GetMapping("/{id}")
    @Operation(summary = "Get payments", description = "Get all user's payments by user id")
    public List<PaymentDto> findAllByUserId(@PathVariable Long id) {
        return paymentService.getPayments(id);
    }

    @PostMapping
    @Operation(summary = "Create a payment",
            description = "Create a payment and redirect to Stripe API to pay. "
                    + "Use card 4242 4242 4242 4242 for testing success payment.")
    public PaymentDto create(@RequestBody @Valid PaymentRequestDto requestDto,
                          Authentication authentication) throws StripeException {
        return paymentService.create(requestDto, authentication);
    }

    @GetMapping("/success")
    @Operation(summary = "Handle success",
            description = "Handle success of payment operation (if user paid for rent)")
    public PaymentDto handleSuccessPayment(@RequestParam String sessionId) throws StripeException {
        return paymentService.handleSuccess(sessionId);
    }


    @GetMapping("/cancel")
    @Operation(summary = "Handle cancel",
            description = "Handle cancel of payment operation (if user didn't pay)")
    public String handleCancelPayments() {
        return paymentService.handleCancel();
    }
}
