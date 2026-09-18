package com.example.carsharingapp.repository;

import com.example.carsharingapp.model.Payment;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    @EntityGraph(attributePaths = {"rental"})
    @Query("SELECT p FROM Payment p JOIN p.rental r WHERE r.user.id = :userId")
    List<Payment> findAllByRentalUserId(Long userId);

    @EntityGraph(attributePaths = {"rental"})
    Optional<Payment> findBySessionId(String sessionId);
}
