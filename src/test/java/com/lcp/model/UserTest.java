package com.lcp.model;

import org.junit.jupiter.api.Test;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Set;

class UserTest {

    @Test
    void testUserGettersAndSetters() {
        User user = new User();
        Long id = 1L;
        String username = "testuser";
        String password = "password123";

        user.setId(id);
        user.setUsername(username);
        user.setPassword(password);

        assertEquals(id, user.getId());
        assertEquals(username, user.getUsername());
        assertEquals(password, user.getPassword());
    }

    @Test
    void testUserValidation() {
        User user = new User();

        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();
        Set<ConstraintViolation<User>> violations = null;

        // Имя пользователя меньше 3 символов
        user.setUsername("er");
        // Пароль короче 18 символов
        user.setPassword("er");
        violations = validator.validate(user);
        assertEquals(2, violations.size());
        
        // Имя пользователя больше 50 символов
        user.setUsername("more than 50 symbols symbols symbols symbols symbols");
        // Пароль больше 100 символов
        user.setPassword("more than 100 symbols symbols symbols symbols symbols symbols symbols symbols symbols symbols symbols");
        violations = validator.validate(user);
        assertEquals(2, violations.size());

        // Имя пользователя меньше 3 символов
        user.setUsername("");
        // Пароль короче 18 символов
        user.setPassword("");
        violations = validator.validate(user);
        assertEquals(2, violations.size());
    }
}