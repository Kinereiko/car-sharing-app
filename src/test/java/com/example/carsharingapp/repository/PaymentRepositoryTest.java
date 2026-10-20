package com.example.carsharingapp.repository;

import com.example.carsharingapp.model.Payment;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.List;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Sql(scripts = "classpath:database/insert-data.sql",
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
@Sql(scripts = "classpath:database/cleanup.sql",
        executionPhase = Sql.ExecutionPhase.AFTER_TEST_CLASS)
public class PaymentRepositoryTest {
    @Autowired
    PaymentRepository paymentRepository;

    @Test
    @DisplayName("""
            Find payment by user id of rental
            """)
    void findAllByUserId_ReturnsTrue() {
        List<Payment> resultList = paymentRepository.findAllByRentalUserId(1L);
        assertFalse(resultList.isEmpty());
    }

    @Test
    @DisplayName("""
            Find payment by session id
            """)
    void findBySessionId_ReturnsTrue() {
        boolean result = paymentRepository.findBySessionId("1234").isPresent();
        assertTrue(result);
    }
}
