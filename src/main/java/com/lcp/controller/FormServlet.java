package com.lcp.controller;

import com.google.gson.Gson;
import com.lcp.util.HttpClientUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@WebServlet("/f")
public class FormServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html");
        request.getRequestDispatcher("/views/form.html").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Чтение JSON из тела запроса
        StringBuilder jsonBuilder = new StringBuilder();
        try (BufferedReader reader = request.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                jsonBuilder.append(line);
            }
        }
        String json = jsonBuilder.toString();

        // Отправка данных в микросервис на Go
        try {
            String url = "http://go-data-service:8081/forms";
            HttpClientUtil.post(url, json); // Отправляем JSON в микросервис

            // Успешный ответ клиенту
            Map<String, String> responseData = new HashMap<>();
            responseData.put("redirect", "/forms");
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(new Gson().toJson(responseData));
        } catch (Exception e) {
            // Ошибка
            Map<String, String> responseData = new HashMap<>();
            responseData.put("error", "Ошибка при создании формы: " + e.getMessage());
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(new Gson().toJson(responseData));
        }
    }
}