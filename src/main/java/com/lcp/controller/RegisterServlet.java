package com.lcp.controller;

import com.google.gson.Gson;
import com.lcp.util.HttpClientUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@WebServlet("/register")
public class RegisterServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String username = request.getParameter("username");
        String password = request.getParameter("password");

        Map<String, String> responseData = new HashMap<>();

        if (username == null || username.isEmpty() || password == null || password.isEmpty()) {
            responseData.put("error", "Имя пользователя и пароль не могут быть пустыми.");
        } else {
            try {
                Map<String, String> registerData = new HashMap<>();
                registerData.put("username", username);
                registerData.put("password", password);

                String url = "http://go-data-service:8081/register";
                String jsonResponse = HttpClientUtil.post(url, new Gson().toJson(registerData));

                if (jsonResponse.contains("error")) {
                    responseData.put("error", "Ошибка при регистрации: " + jsonResponse);
                } else {
                    responseData.put("redirect", "/index.html");
                }
            } catch (Exception e) {
                responseData.put("error", "Ошибка при регистрации: " + e.getMessage());
            }
        }

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(new Gson().toJson(responseData));
    }
}