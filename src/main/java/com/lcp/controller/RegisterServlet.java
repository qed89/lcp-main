package com.lcp.controller;

import com.google.gson.Gson;
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
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@WebServlet("/register")
public class RegisterServlet extends HttpServlet {
    private UserDao userDao = new UserDao();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String username = request.getParameter("username");
        String password = request.getParameter("password");

        Map<String, String> responseData = new HashMap<>();

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
            responseData.put("error", errors.toString());
        } else if (userDao.findByUsername(username) != null) {
            responseData.put("error", "Пользователь с таким именем уже существует.");
        } else {
            try {
                userDao.save(user);
                responseData.put("redirect", "/index.html"); // Успешная регистрация, перенаправляем
            } catch (Exception e) {
                responseData.put("error", "Ошибка при регистрации: " + e.getMessage());
            }
        }

        // Отправляем JSON-ответ
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(new Gson().toJson(responseData));
    }
}