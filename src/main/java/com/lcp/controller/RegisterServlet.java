package com.lcp.controller;

import com.lcp.dao.UserDao;
import com.lcp.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import java.io.IOException;
import java.util.Set;

@WebServlet("/register")
public class RegisterServlet extends HttpServlet {
    private UserDao userDao = new UserDao();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String username = request.getParameter("username");
        String password = request.getParameter("password");

        User user = new User();
        user.setUsername(username);
        user.setPassword(password);

        // Валидация
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();
        Set<ConstraintViolation<User>> violations = validator.validate(user);

        if (!violations.isEmpty()) {
            StringBuilder errors = new StringBuilder();
            for (ConstraintViolation<User> violation : violations) {
                errors.append(violation.getMessage()).append("<br>");
            }
            sendErrorResponse(response, errors.toString());
            return;
        }

        // Проверка уникальности имени пользователя
        if (userDao.findByUsername(username) != null) {
            sendErrorResponse(response, "Пользователь с таким именем уже существует.");
            return;
        }

        try {
            userDao.save(user);
            response.sendRedirect("index.html");
        } catch (Exception e) {
            sendErrorResponse(response, "Ошибка при регистрации: " + e.getMessage());
        }
    }

    private void sendErrorResponse(HttpServletResponse response, String message) throws IOException {
        response.setContentType("text/html");
        response.getWriter().write("<p style='color:red;'>" + message + "</p>");
    }
}