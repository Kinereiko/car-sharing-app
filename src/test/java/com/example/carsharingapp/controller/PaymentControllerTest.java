package com.example.carsharingapp.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import com.example.carsharingapp.config.SecurityConfig;
import com.example.carsharingapp.dto.payment.PaymentDto;
import com.example.carsharingapp.dto.payment.PaymentRequestDto;
import com.example.carsharingapp.model.Payment;
import com.example.carsharingapp.security.JwtAuthenticationFilter;
import com.example.carsharingapp.security.JwtUtil;
import com.example.carsharingapp.service.payment.PaymentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(PaymentController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class})
class PaymentControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PaymentService paymentService;
    @MockBean
    private JwtUtil jwtUtil;
    @MockBean
    private UserDetailsService userDetailsService;

    private PaymentDto paymentDto(Long id, Payment.Status status) {
        PaymentDto dto = new PaymentDto();
        dto.setId(id);
        dto.setStatus(status);
        dto.setType(Payment.Type.PAYMENT);
        dto.setRentalId(1L);
        dto.setSessionId("sess_123");
        dto.setSessionUrl("https://checkout.stripe.com/sess_123");
        dto.setTotalPrice(BigDecimal.valueOf(150));
        return dto;
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    @DisplayName("POST /payments creates a payment and returns the Stripe session")
    void create_ValidRequest_ReturnsPayment() throws Exception {
        PaymentRequestDto request = new PaymentRequestDto();
        request.setRentalId(1L);
        request.setType(Payment.Type.PAYMENT);
        when(paymentService.create(any(PaymentRequestDto.class), any(Authentication.class)))
                .thenReturn(paymentDto(1L, Payment.Status.PENDING));

        mockMvc.perform(post("/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.type").value("PAYMENT"))
                .andExpect(jsonPath("$.sessionUrl").value("https://checkout.stripe.com/sess_123"))
                .andExpect(jsonPath("$.totalPrice").value(150));
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    @DisplayName("POST /payments without rentalId and type returns 400")
    void create_InvalidRequest_ReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.length()").value(2));

        verifyNoInteractions(paymentService);
    }

    @Test
    @DisplayName("POST /payments without authentication returns 401")
    void create_Anonymous_ReturnsUnauthorized() throws Exception {
        mockMvc.perform(post("/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"rentalId\": 1, \"type\": \"PAYMENT\"}"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(paymentService);
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    @DisplayName("GET /payments/{id} as MANAGER returns payments of the user")
    void findAllByUserId_Manager_ReturnsList() throws Exception {
        when(paymentService.getPayments(5L))
                .thenReturn(List.of(paymentDto(1L, Payment.Status.PAID),
                        paymentDto(2L, Payment.Status.PENDING)));

        mockMvc.perform(get("/payments/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].status").value("PAID"))
                .andExpect(jsonPath("$[1].status").value("PENDING"));
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    @DisplayName("GET /payments/{id} as CUSTOMER is forbidden")
    void findAllByUserId_User_ReturnsForbidden() throws Exception {
        mockMvc.perform(get("/payments/5"))
                .andExpect(status().isForbidden());

        verifyNoInteractions(paymentService);
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    @DisplayName("GET /payments/success marks the payment as paid")
    void handleSuccess_ValidSession_ReturnsPayment() throws Exception {
        when(paymentService.handleSuccess("sess_123"))
                .thenReturn(paymentDto(1L, Payment.Status.PAID));

        mockMvc.perform(get("/payments/success").param("sessionId", "sess_123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PAID"))
                .andExpect(jsonPath("$.sessionId").value("sess_123"));
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    @DisplayName("GET /payments/success without sessionId returns 400")
    void handleSuccess_MissingSessionId_ReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/payments/success"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(paymentService);
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    @DisplayName("GET /payments/cancel returns the cancel message")
    void handleCancel_ReturnsMessage() throws Exception {
        when(paymentService.handleCancel()).thenReturn("Payment was cancelled");

        mockMvc.perform(get("/payments/cancel"))
                .andExpect(status().isOk())
                .andExpect(content().string("Payment was cancelled"));
    }
}
