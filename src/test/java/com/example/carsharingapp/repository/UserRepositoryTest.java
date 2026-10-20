package com.example.carsharingapp.repository;

import com.example.carsharingapp.model.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.Optional;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Sql(scripts = "classpath:database/insert-data.sql",
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
@Sql(scripts = "classpath:database/cleanup.sql",
        executionPhase = Sql.ExecutionPhase.AFTER_TEST_CLASS)
public class UserRepositoryTest {
    @Autowired
    UserRepository userRepository;

    @Test
    @DisplayName("""
            Check if user exists return true
            """)
    void checkByEmail_ReturnsTrue() {
        boolean result = userRepository.existsByEmail("admin@gmail.com");
        assertTrue(result);
    }

    @Test
    @DisplayName("""
            Check if user exist and has role
            """)
    void checkUserHasRole_ReturnsTrue() {
        Optional<User> user = userRepository.findWithRolesByEmail("admin@gmail.com");
        boolean result = user.get().getRoles().isEmpty();
        assertFalse(result);
    }
}
