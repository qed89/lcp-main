package com.lcp.controller;

import com.google.gson.Gson;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Map;

@WebServlet("/create-table")
public class CreateTableServlet extends HttpServlet {
    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Устанавливаем кодировку запроса
        request.setCharacterEncoding("UTF-8");

        // Чтение JSON из тела запроса
        StringBuilder jsonBuilder = new StringBuilder();
        try (BufferedReader reader = request.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                jsonBuilder.append(line);
            }
        }
        String json = jsonBuilder.toString();

        // Парсинг JSON с использованием Gson
        Gson gson = new Gson();
        Map<String, Object> data = gson.fromJson(json, Map.class);

        // Извлечение данных из JSON
        String tableName = (String) data.get("tableName");
        ArrayList attributes = (ArrayList) data.get("attributes");

        // Создание таблицы в базе данных
        try (Connection conn = DriverManager.getConnection("jdbc:postgresql://localhost:5432/lcp", "postgres", "postgres");
             Statement stmt = conn.createStatement()) {

            StringBuilder sql = new StringBuilder("CREATE TABLE " + tableName + " (id SERIAL PRIMARY KEY");
            for (Object attribute : attributes) {
                Map<String, String> attr = (Map<String, String>) attribute;
                sql.append(", ").append(attr.get("name")).append(" ").append(attr.get("type"));
            }
            sql.append(")");

            stmt.executeUpdate(sql.toString());
            response.getWriter().write("{\"success\": true}");
        } catch (Exception e) {
            response.getWriter().write("{\"success\": false, \"message\": \"" + e.getMessage() + "\"}");
        }
    }
}