package com.example.carsharingapp.repository;

import com.example.carsharingapp.model.Role;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RoleRepository extends JpaRepository<Role, Long> {
    @Query(value = "SELECT * FROM roles WHERE role = CAST(:role AS role_type)", nativeQuery = true)
    Optional<Role> findByRole(@Param("role") String role);
}
