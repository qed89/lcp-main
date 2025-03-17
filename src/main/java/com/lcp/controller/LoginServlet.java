package com.lcp.controller;

import com.google.gson.Gson;
import com.lcp.dao.UserDao;
import com.lcp.model.User;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {
    private UserDao userDao = new UserDao();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String username = request.getParameter("username");
        String password = request.getParameter("password");

        Map<String, String> responseData = new HashMap<>();

        if (username == null || username.isEmpty() || password == null || password.isEmpty()) {
            responseData.put("error", "Имя пользователя и пароль не могут быть пустыми.");
        } else {
            try {
                User user = userDao.findByUsername(username);
                if (user != null && userDao.checkPassword(password, user.getPassword())) {
                    request.getSession().setAttribute("user", user);
                    responseData.put("redirect", "/forms"); // Успешный вход, перенаправляем
                } else {
                    responseData.put("error", "Неверное имя пользователя или пароль.");
                }
            } catch (Exception e) {
                responseData.put("error", "Ошибка при входе: " + e.getMessage());
            }
        }

        // Отправляем JSON-ответ
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(new Gson().toJson(responseData));
    }
}