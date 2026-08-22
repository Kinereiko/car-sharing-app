package com.example.carsharingapp.repository;

import com.example.carsharingapp.model.Rental;
import jakarta.annotation.Nullable;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

public interface RentalRepository extends JpaRepository<Rental,
        Long>, JpaSpecificationExecutor<Rental> {

    @EntityGraph(attributePaths = {"car"})
    Optional<Rental> findById(Long id);

    @EntityGraph(attributePaths = {"car"})
    @Query("SELECT r FROM Rental r JOIN r.car c JOIN r.user u WHERE u.id = :id")
    List<Rental> findAllByUserId(Long id);

    @EntityGraph(attributePaths = {"car"})
    List<Rental> findAll(@Nullable Specification<Rental> spec);

    @EntityGraph(attributePaths = {"car"})
    @Query("SELECT r FROM Rental r JOIN r.car c JOIN r.user u "
            + "WHERE r.actualReturnDate IS NULL AND r.returnDate < :todayDate")
    List<Rental> findAllOverdue(LocalDate todayDate);
}
