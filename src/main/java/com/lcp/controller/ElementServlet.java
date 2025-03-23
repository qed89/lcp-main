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
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;

@WebServlet(urlPatterns = {"/element/*", "/element/*/data"})
public class ElementServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String pathInfo = request.getPathInfo();
        String elementId = null;

        if (pathInfo != null && !pathInfo.equals("/")) {
            String[] pathParts = pathInfo.split("/");
            if (pathParts.length > 1) {
                elementId = pathParts[1];
            }
        }

        if (pathInfo != null && pathInfo.endsWith("/data")) {
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");

            if (elementId == null || elementId.isEmpty()) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Идентификатор элемента не указан");
                return;
            }

            try {
                // Запрос к микросервису на Go
                String url = "http://go-data-service:8081/elements/" + elementId;
                HttpResponse<String> httpResponse = HttpClientUtil.get(url);
                String jsonResponse = httpResponse.body();

                // Отправка JSON-ответа клиенту
                response.getWriter().write(jsonResponse);
            } catch (Exception e) {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Ошибка при получении элемента: " + e.getMessage());
            }
        } else {
            response.setContentType("text/html");
            response.setCharacterEncoding("UTF-8");
            request.getRequestDispatcher("/views/element.html").forward(request, response);
        }
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
            String url = "http://go-data-service:8081/elements";
            HttpClientUtil.post(url, json); // Отправляем JSON в микросервис

            // Успешный ответ клиенту
            Map<String, String> responseData = new HashMap<>();
            responseData.put("redirect", "/elements");
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(new Gson().toJson(responseData));
        } catch (Exception e) {
            // Ошибка
            Map<String, String> responseData = new HashMap<>();
            responseData.put("error", "Ошибка при сохранении элемента: " + e.getMessage());
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(new Gson().toJson(responseData));
        }
    }
}