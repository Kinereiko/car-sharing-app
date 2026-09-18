package com.example.carsharingapp.service.payment;

import com.example.carsharingapp.dto.payment.PaymentDto;
import com.example.carsharingapp.dto.payment.PaymentRequestDto;
import com.example.carsharingapp.mapper.PaymentMapper;
import com.example.carsharingapp.model.Payment;
import com.example.carsharingapp.model.Rental;
import com.example.carsharingapp.model.User;
import com.example.carsharingapp.repository.PaymentRepository;
import com.example.carsharingapp.repository.RentalRepository;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import jakarta.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {
    private final PaymentRepository paymentRepository;
    private final PaymentMapper paymentMapper;
    private final RentalRepository rentalRepository;

    @Value("${stripe.secret.key}")
    private String stripeSecretKey;
    @Value("${app.domain}")
    private String domain;
    private static final double FINE_MULTIPLIER = 1.5;

    @Override
    public List<PaymentDto> getPayments(Long userId) {
        return paymentRepository.findAllByRentalUserId(userId)
                .stream()
                .map(paymentMapper::toDto)
                .toList();
    }

    @Override
    public PaymentDto create(PaymentRequestDto requestDto,
                             Authentication authentication) throws StripeException {
        Rental rental = rentalRepository.findById(requestDto.getRentalId())
                .orElseThrow(() -> new EntityNotFoundException("Can't find rental with id: "
                        + requestDto.getRentalId()));

        BigDecimal amount = calculateAmount(rental, requestDto.getType());

        String successUrl = createUrl(domain, UrlType.SUCCESS);
        String cancelUrl = createUrl(domain, UrlType.CANCEL);

        SessionCreateParams params = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl(successUrl)
                .setCancelUrl(cancelUrl)
                .addLineItem(SessionCreateParams.LineItem.builder()
                        .setQuantity(1L)
                        .setPriceData(SessionCreateParams.LineItem.PriceData.builder()
                                .setCurrency("usd")
                                .setUnitAmount(amount.multiply(BigDecimal.valueOf(100)).longValue())
                                .setProductData(SessionCreateParams.LineItem.PriceData
                                        .ProductData.builder()
                                        .setName("Car model: " + rental.getCar().getModel())
                                        .build())
                                .build())
                        .build())
                .build();

        Session session = Session.create(params);

        Payment payment = createPayment(rental, Payment.Status.PENDING,
                requestDto.getType(), session, amount);

        return paymentMapper.toDto(paymentRepository.save(payment));
    }

    private String createUrl(String domain, UrlType type) {
        if (type.equals(UrlType.SUCCESS)) {
            return UriComponentsBuilder.fromUriString(domain)
                    .path("/payments/success")
                    .queryParam("sessionId", "{CHECKOUT_SESSION_ID}")
                    .build(false)
                    .toUriString();
        }
        return UriComponentsBuilder.fromUriString(domain)
                .path("/payments/cancel")
                .toUriString();
    }

    @Override
    public PaymentDto handleSuccess(String sessionId) throws StripeException {
        Session session = Session.retrieve(sessionId);

        if ("paid".equals(session.getPaymentStatus())) {
            Payment payment = paymentRepository.findBySessionId(sessionId)
                    .orElseThrow(() ->
                            new EntityNotFoundException("Can't find payment with sessionId: "
                            + sessionId));
            payment.setStatus(Payment.Status.PAID);
            return paymentMapper.toDto(paymentRepository.save(payment));
        }
        throw new RuntimeException("Payment not completed");
    }

    @Override
    public String handleCancel() {
        return "Payment was cancelled. Session is available for 24 hours.";
    }

    private User getUserFromAuthentication(Authentication authentication) {
        return (User) authentication.getPrincipal();
    }

    private BigDecimal calculateAmount(Rental rental, Payment.Type type) {
        long days = ChronoUnit.DAYS.between(rental.getRentalDate(), rental.getReturnDate());
        BigDecimal amount = rental.getCar().getDailyFee().multiply(BigDecimal.valueOf(days));
        if (type.equals(Payment.Type.FINE)) {
            long overdueDays = ChronoUnit.DAYS.between(rental.getReturnDate(),
                    rental.getActualReturnDate());
            amount = amount.add(rental.getCar().getDailyFee()
                    .multiply(BigDecimal.valueOf(overdueDays))
                    .multiply(BigDecimal.valueOf(FINE_MULTIPLIER)));
        }
        return amount;
    }

    private Payment createPayment(Rental rental, Payment.Status status,
                                  Payment.Type type, Session session, BigDecimal amount) {
        Payment payment = new Payment();
        payment.setRental(rental);
        payment.setStatus(status);
        payment.setType(type);
        payment.setSessionUrl(session.getUrl());
        payment.setSessionId(session.getId());
        payment.setTotalPrice(amount);
        return payment;
    }

    private enum UrlType {
        SUCCESS, CANCEL
    }
}
