package com.example.carsharingapp.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import com.example.carsharingapp.dto.payment.PaymentDto;
import com.example.carsharingapp.dto.payment.PaymentRequestDto;
import com.example.carsharingapp.mapper.PaymentMapper;
import com.example.carsharingapp.model.Car;
import com.example.carsharingapp.model.Payment;
import com.example.carsharingapp.model.Rental;
import com.example.carsharingapp.model.User;
import com.example.carsharingapp.repository.PaymentRepository;
import com.example.carsharingapp.repository.RentalRepository;
import com.example.carsharingapp.service.payment.PaymentServiceImpl;
import com.stripe.exception.ApiConnectionException;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import jakarta.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
public class PaymentServiceTest {
    private static final Long VALID_ID = 1L;
    private static final Long INVALID_ID = 100L;
    private static final int INITIAL_INVENTORY = 2;
    private static final int RENTED_DAYS = 2;
    private static final int OVERDUE_DAYS = 2;
    private static final String DOMAIN = "http://localhost:8080";
    private static final String SESSION_ID = "cs_test_123";
    private static final String SESSION_URL = "https://checkout.stripe.com/pay/cs_test_123";
    private static final String PAID_STATUS = "paid";
    private static final String UNPAID_STATUS = "unpaid";
    private static final BigDecimal DAILY_FEE = BigDecimal.valueOf(48.50);

    @InjectMocks
    private PaymentServiceImpl paymentService;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private PaymentMapper paymentMapper;

    @Mock
    private RentalRepository rentalRepository;

    @Mock
    private Authentication authentication;

    @Mock
    private Session session;

    @Test
    @DisplayName("""
            Verify that getPayments returns all payments of the user as DTOs
            """)
    public void getPayments_UserHasPayments_ReturnsDtoList() {
        Payment first = createTestPayment(VALID_ID, Payment.Status.PENDING);
        Payment second = createTestPayment(2L, Payment.Status.PAID);
        PaymentDto firstDto = createTestPaymentDto(first);
        PaymentDto secondDto = createTestPaymentDto(second);

        when(paymentRepository.findAllByRentalUserId(VALID_ID)).thenReturn(List.of(first, second));
        when(paymentMapper.toDto(first)).thenReturn(firstDto);
        when(paymentMapper.toDto(second)).thenReturn(secondDto);

        List<PaymentDto> actual = paymentService.getPayments(VALID_ID);

        assertEquals(List.of(firstDto, secondDto), actual);
        verify(paymentRepository).findAllByRentalUserId(VALID_ID);
        verify(paymentMapper).toDto(first);
        verify(paymentMapper).toDto(second);
    }

    @Test
    @DisplayName("""
            Verify that getPayments returns an empty list when the user has no payments
            """)
    public void getPayments_UserHasNoPayments_ReturnsEmptyList() {
        when(paymentRepository.findAllByRentalUserId(INVALID_ID)).thenReturn(List.of());

        List<PaymentDto> actual = paymentService.getPayments(INVALID_ID);

        assertTrue(actual.isEmpty());
        verify(paymentRepository).findAllByRentalUserId(INVALID_ID);
        verifyNoInteractions(paymentMapper);
    }

    @Test
    @DisplayName("""
            Verify that create saves a pending payment with the rental price
            and creates a Stripe session with the correct parameters
            """)
    public void create_PaymentType_SavesPendingPaymentWithRentalPrice() throws StripeException {
        ReflectionTestUtils.setField(paymentService, "domain", DOMAIN);
        PaymentRequestDto requestDto = createTestPaymentRequestDto(Payment.Type.PAYMENT);
        Rental rental = createTestRental(null);
        PaymentDto expected = new PaymentDto();
        expected.setId(VALID_ID);

        when(rentalRepository.findById(VALID_ID)).thenReturn(Optional.of(rental));
        when(session.getId()).thenReturn(SESSION_ID);
        when(session.getUrl()).thenReturn(SESSION_URL);
        when(paymentRepository.save(any(Payment.class))).thenAnswer(inv -> inv.getArgument(0));
        when(paymentMapper.toDto(any(Payment.class))).thenReturn(expected);

        try (MockedStatic<Session> sessionStatic = mockStatic(Session.class)) {
            sessionStatic.when(() -> Session.create(any(SessionCreateParams.class)))
                    .thenReturn(session);

            PaymentDto actual = paymentService.create(requestDto, authentication);

            assertSame(expected, actual);

            ArgumentCaptor<SessionCreateParams> paramsCaptor =
                    ArgumentCaptor.forClass(SessionCreateParams.class);
            sessionStatic.verify(() -> Session.create(paramsCaptor.capture()));
            SessionCreateParams params = paramsCaptor.getValue();
            long expectedCents = DAILY_FEE.multiply(BigDecimal.valueOf(RENTED_DAYS))
                    .multiply(BigDecimal.valueOf(100)).longValue();
            SessionCreateParams.LineItem lineItem = params.getLineItems().get(0);
            assertEquals(SessionCreateParams.Mode.PAYMENT, params.getMode());
            assertEquals(expectedCents, lineItem.getPriceData().getUnitAmount());
            assertEquals("usd", lineItem.getPriceData().getCurrency());
            assertEquals("Car model: Mazda 3",
                    lineItem.getPriceData().getProductData().getName());
            assertTrue(params.getSuccessUrl().startsWith(DOMAIN + "/payments/success"));
            assertTrue(params.getSuccessUrl().contains("{CHECKOUT_SESSION_ID}"));
            assertEquals(DOMAIN + "/payments/cancel", params.getCancelUrl());
        }

        ArgumentCaptor<Payment> paymentCaptor = ArgumentCaptor.forClass(Payment.class);
        verify(paymentRepository).save(paymentCaptor.capture());
        Payment saved = paymentCaptor.getValue();
        assertSame(rental, saved.getRental());
        assertEquals(Payment.Status.PENDING, saved.getStatus());
        assertEquals(Payment.Type.PAYMENT, saved.getType());
        assertEquals(SESSION_ID, saved.getSessionId());
        assertEquals(SESSION_URL, saved.getSessionUrl());
        assertEquals(0, new BigDecimal("97.00").compareTo(saved.getTotalPrice()));
    }

    @Test
    @DisplayName("""
            Verify that create adds a fine for overdue days when the payment type is FINE
            """)
    public void create_FineType_AddsFineForOverdueDays() throws StripeException {
        ReflectionTestUtils.setField(paymentService, "domain", DOMAIN);
        PaymentRequestDto requestDto = createTestPaymentRequestDto(Payment.Type.FINE);
        Rental rental = createTestRental(LocalDate.now().plusDays(RENTED_DAYS + OVERDUE_DAYS));

        when(rentalRepository.findById(VALID_ID)).thenReturn(Optional.of(rental));
        when(session.getId()).thenReturn(SESSION_ID);
        when(session.getUrl()).thenReturn(SESSION_URL);
        when(paymentRepository.save(any(Payment.class))).thenAnswer(inv -> inv.getArgument(0));
        when(paymentMapper.toDto(any(Payment.class))).thenReturn(new PaymentDto());

        try (MockedStatic<Session> sessionStatic = mockStatic(Session.class)) {
            sessionStatic.when(() -> Session.create(any(SessionCreateParams.class)))
                    .thenReturn(session);

            paymentService.create(requestDto, authentication);
        }

        ArgumentCaptor<Payment> paymentCaptor = ArgumentCaptor.forClass(Payment.class);
        verify(paymentRepository).save(paymentCaptor.capture());
        Payment saved = paymentCaptor.getValue();
        // 48.50 * 2 rented days + 48.50 * 2 overdue days * 1.5 fine multiplier
        assertEquals(Payment.Type.FINE, saved.getType());
        assertEquals(0, new BigDecimal("242.50").compareTo(saved.getTotalPrice()));
    }

    @Test
    @DisplayName("""
            Verify that create throws EntityNotFoundException when the rental does not exist
            """)
    public void create_RentalNotFound_ThrowsEntityNotFoundException() {
        PaymentRequestDto requestDto = createTestPaymentRequestDto(Payment.Type.PAYMENT);
        requestDto.setRentalId(INVALID_ID);

        when(rentalRepository.findById(INVALID_ID)).thenReturn(Optional.empty());

        try (MockedStatic<Session> sessionStatic = mockStatic(Session.class)) {
            EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                    () -> paymentService.create(requestDto, authentication));

            assertEquals("Can't find rental with id: " + INVALID_ID, exception.getMessage());
            sessionStatic.verifyNoInteractions();
        }
        verifyNoInteractions(paymentRepository, paymentMapper);
    }

    @Test
    @DisplayName("""
            Verify that create does not save a payment when Stripe fails to create a session
            """)
    public void create_StripeFails_ThrowsStripeExceptionAndDoesNotSave() {
        ReflectionTestUtils.setField(paymentService, "domain", DOMAIN);
        PaymentRequestDto requestDto = createTestPaymentRequestDto(Payment.Type.PAYMENT);
        Rental rental = createTestRental(null);

        when(rentalRepository.findById(VALID_ID)).thenReturn(Optional.of(rental));

        try (MockedStatic<Session> sessionStatic = mockStatic(Session.class)) {
            sessionStatic.when(() -> Session.create(any(SessionCreateParams.class)))
                    .thenThrow(new ApiConnectionException("Stripe is unavailable"));

            assertThrows(StripeException.class,
                    () -> paymentService.create(requestDto, authentication));
        }
        verify(paymentRepository, never()).save(any(Payment.class));
        verifyNoInteractions(paymentMapper);
    }

    @Test
    @DisplayName("""
            Verify that handleSuccess marks the payment as PAID when the Stripe session is paid
            """)
    public void handleSuccess_PaidSession_SetsStatusPaid() throws StripeException {
        Payment payment = createTestPayment(VALID_ID, Payment.Status.PENDING);
        PaymentDto expected = createTestPaymentDto(payment);
        expected.setStatus(Payment.Status.PAID);

        when(session.getPaymentStatus()).thenReturn(PAID_STATUS);
        when(paymentRepository.findBySessionId(SESSION_ID)).thenReturn(Optional.of(payment));
        when(paymentRepository.save(payment)).thenReturn(payment);
        when(paymentMapper.toDto(payment)).thenReturn(expected);

        try (MockedStatic<Session> sessionStatic = mockStatic(Session.class)) {
            sessionStatic.when(() -> Session.retrieve(SESSION_ID)).thenReturn(session);

            PaymentDto actual = paymentService.handleSuccess(SESSION_ID);

            assertEquals(expected, actual);
            assertEquals(Payment.Status.PAID, payment.getStatus());
        }
        verify(paymentRepository).findBySessionId(SESSION_ID);
        verify(paymentRepository).save(payment);
        verify(paymentMapper).toDto(payment);
    }

    @Test
    @DisplayName("""
            Verify that handleSuccess throws an exception and keeps the payment unchanged
            when the Stripe session is not paid
            """)
    public void handleSuccess_UnpaidSession_ThrowsRuntimeException() {
        when(session.getPaymentStatus()).thenReturn(UNPAID_STATUS);

        try (MockedStatic<Session> sessionStatic = mockStatic(Session.class)) {
            sessionStatic.when(() -> Session.retrieve(SESSION_ID)).thenReturn(session);

            RuntimeException exception = assertThrows(RuntimeException.class,
                    () -> paymentService.handleSuccess(SESSION_ID));

            assertEquals("Payment not completed", exception.getMessage());
        }
        verifyNoInteractions(paymentRepository, paymentMapper);
    }

    @Test
    @DisplayName("""
            Verify that handleSuccess throws EntityNotFoundException
            when the session is paid but no payment has this sessionId
            """)
    public void handleSuccess_PaymentNotFound_ThrowsEntityNotFoundException() {
        when(session.getPaymentStatus()).thenReturn(PAID_STATUS);
        when(paymentRepository.findBySessionId(SESSION_ID)).thenReturn(Optional.empty());

        try (MockedStatic<Session> sessionStatic = mockStatic(Session.class)) {
            sessionStatic.when(() -> Session.retrieve(SESSION_ID)).thenReturn(session);

            EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                    () -> paymentService.handleSuccess(SESSION_ID));

            assertEquals("Can't find payment with sessionId: " + SESSION_ID,
                    exception.getMessage());
        }
        verify(paymentRepository, never()).save(any(Payment.class));
        verifyNoInteractions(paymentMapper);
    }

    @Test
    @DisplayName("""
            Verify that handleCancel returns the cancellation message
            """)
    public void handleCancel_ReturnsCancellationMessage() {
        String actual = paymentService.handleCancel();

        assertEquals("Payment was cancelled. Session is available for 24 hours.", actual);
        verifyNoInteractions(paymentRepository, paymentMapper, rentalRepository);
    }

    private PaymentRequestDto createTestPaymentRequestDto(Payment.Type type) {
        PaymentRequestDto requestDto = new PaymentRequestDto();
        requestDto.setRentalId(VALID_ID);
        requestDto.setType(type);
        return requestDto;
    }

    private Payment createTestPayment(Long id, Payment.Status status) {
        Payment payment = new Payment();
        payment.setId(id);
        payment.setStatus(status);
        payment.setType(Payment.Type.PAYMENT);
        payment.setRental(createTestRental(null));
        payment.setSessionId(SESSION_ID);
        payment.setSessionUrl(SESSION_URL);
        payment.setTotalPrice(new BigDecimal("97.00"));
        return payment;
    }

    private PaymentDto createTestPaymentDto(Payment payment) {
        PaymentDto paymentDto = new PaymentDto();
        paymentDto.setId(payment.getId());
        paymentDto.setStatus(payment.getStatus());
        paymentDto.setType(payment.getType());
        paymentDto.setRentalId(payment.getRental().getId());
        paymentDto.setSessionId(payment.getSessionId());
        paymentDto.setSessionUrl(payment.getSessionUrl());
        paymentDto.setTotalPrice(payment.getTotalPrice());
        return paymentDto;
    }

    private Rental createTestRental(LocalDate actualReturnDate) {
        Rental rental = new Rental();
        rental.setId(VALID_ID);
        rental.setCar(createTestCar());
        rental.setUser(createTestUser());
        rental.setRentalDate(LocalDate.now());
        rental.setReturnDate(LocalDate.now().plusDays(RENTED_DAYS));
        rental.setActualReturnDate(actualReturnDate);
        rental.setActive(true);
        return rental;
    }

    private Car createTestCar() {
        Car car = new Car();
        car.setId(VALID_ID);
        car.setModel("Mazda 3");
        car.setBrand("Mazda");
        car.setType(Car.TypeName.SEDAN);
        car.setInventory(INITIAL_INVENTORY);
        car.setDailyFee(DAILY_FEE);
        return car;
    }

    private User createTestUser() {
        User user = new User();
        user.setId(VALID_ID);
        user.setFirstName("Test");
        user.setLastName("Tester");
        user.setEmail("test@gmail.com");
        user.setPassword("test1test");
        return user;
    }
}
