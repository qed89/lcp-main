package com.lcp.controller;

import com.lcp.util.ApiError;
import com.lcp.util.HttpClientUtil;

import com.google.gson.Gson;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.net.http.HttpResponse;
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
                HttpResponse<String> httpResponse = HttpClientUtil.post(url, new Gson().toJson(registerData));

                // Обрабатываем ответ в зависимости от кода состояния
                if (httpResponse.statusCode() == 201) {
                    responseData.put("redirect", "/");
                } else if (httpResponse.statusCode() == 409) {
                    System.out.print("ASE 1 = " + httpResponse.body());
                    ApiError error = new Gson().fromJson(httpResponse.body(), ApiError.class);
                    responseData.put("error", error.getMessage());
                } else {
                    responseData.put("error", "Ошибка при регистрации: " + httpResponse.body());
                }
            } catch (Exception e) {
                responseData.put("error", "Ошибка при регистрации: " + e.getMessage());
            }
        }
        
        response.setContentType("text/html");
        response.setCharacterEncoding("UTF-8");
        if (responseData.containsKey("error")) {
            response.getWriter().write("<div class='alert alert-danger'>" + responseData.get("error") + "</div>");
        } else if (responseData.containsKey("redirect")) {
            response.getWriter().write("<script>window.location.href = '" + responseData.get("redirect") + "';</script>");
        }
    }
}