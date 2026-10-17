package com.example.carsharingapp.repository;

import com.example.carsharingapp.model.Rental;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import org.springframework.data.jpa.domain.Specification;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Sql(scripts = "classpath:database/insert-data.sql",
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
@Sql(scripts = "classpath:database/cleanup.sql",
        executionPhase = Sql.ExecutionPhase.AFTER_TEST_CLASS)
class RentalRepositoryTest {
    @Autowired
    RentalRepository rentalRepository;

    @Test
    @DisplayName("""
            Find rental by id
            """)
    void findById_ReturnsTrue() {
        boolean result = rentalRepository.findById(1L).isPresent();
        assertTrue(result);
    }

    @Test
    @DisplayName("""
            Find rentals by user id
            """)
    void findAllByUserId_ReturnsTrue() {
        List<Rental> resultList = rentalRepository.findAllByUserId(1L);
        assertFalse(resultList.isEmpty());
    }

    @Test
    @DisplayName("""
            Find specific rentals by id or/and status
            """)
    void findAllSpecific_ReturnsTrue() {
        Specification<Rental> spec = (root, query, cb) ->
                cb.equal(root.get("isActive"), true);
        List<Rental> resultList = rentalRepository.findAll(spec);
        assertFalse(resultList.isEmpty());
    }

    @Test
    @DisplayName("""
            Find all overdue rentals
            """)
    void findAllOverdue_ReturnsTrue() {
        List<Rental> resultList = rentalRepository.findAllOverdue(LocalDate.now());
        assertFalse(resultList.isEmpty());
    }
}
