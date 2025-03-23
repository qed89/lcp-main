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

@WebServlet(urlPatterns = {"/t/*", "/t/*/data"})
public class TableServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String pathInfo = request.getPathInfo();
        String tableName = null;

        if (pathInfo != null && !pathInfo.equals("/")) {
            String[] pathParts = pathInfo.split("/");
            if (pathParts.length > 1) {
                tableName = pathParts[1];
            }
        }

        if (tableName == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Имя таблицы не указано");
            return;
        }

        if (pathInfo != null && pathInfo.endsWith("/data")) {
            // Получение данных таблицы
            try {
                // Запрос к микросервису на Go
                String url = "http://go-data-service:8081/tables/" + tableName;
                HttpResponse<String> httpResponse = HttpClientUtil.get(url);
                String jsonResponse = httpResponse.body();

                // Отправка JSON-ответа клиенту
                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");
                response.getWriter().write(jsonResponse);
            } catch (Exception e) {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Ошибка при получении данных таблицы: " + e.getMessage());
            }
        } else {
            // Отображение HTML-страницы
            response.setContentType("text/html");
            response.setCharacterEncoding("UTF-8");
            request.setAttribute("tableName", tableName);
            request.getRequestDispatcher("/views/table.html").forward(request, response);
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
            String url = "http://go-data-service:8081/tables";
            HttpClientUtil.post(url, json); // Отправляем JSON в микросервис

            // Успешный ответ клиенту
            Map<String, String> responseData = new HashMap<>();
            responseData.put("redirect", "/tables");
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(new Gson().toJson(responseData));
        } catch (Exception e) {
            // Ошибка
            Map<String, String> responseData = new HashMap<>();
            responseData.put("error", "Ошибка при создании таблицы: " + e.getMessage());
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(new Gson().toJson(responseData));
        }
    }
}