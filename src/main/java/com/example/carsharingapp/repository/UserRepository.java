package com.example.carsharingapp.repository;

import com.example.carsharingapp.model.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByEmail(String email);

    @EntityGraph(attributePaths = {"roles"})
    Optional<User> findWithRolesByEmail(String email);
}
