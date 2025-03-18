package com.lcp.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

@WebServlet(urlPatterns = {"/t/*", "/t/*/data"})
public class TableServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String pathInfo = request.getPathInfo(); // Получаем полный путь, например, "/users" или "/users/data"
        String tableName = null;

        if (pathInfo != null) {
            // Извлекаем имя таблицы из пути
            String[] pathParts = pathInfo.split("/");
            if (pathParts.length > 1) {
                tableName = pathParts[1]; // Имя таблицы находится на второй позиции
            }
        }

        if (tableName == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Имя таблицы не указано");
            return;
        }

        if (pathInfo.endsWith("/data")) {
            // Возвращаем HTML-фрагмент с данными таблицы
            response.setCharacterEncoding("UTF-8");
            response.setContentType("text/html; charset=UTF-8");

            List<String> headers = new ArrayList<>();
            List<List<Object>> rows = new ArrayList<>();

            try (Connection conn = DriverManager.getConnection("jdbc:postgresql://postgres:5432/lcp", "postgres", "postgres");
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT * FROM " + tableName)) {

                // Получаем заголовки
                for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
                    headers.add(rs.getMetaData().getColumnName(i));
                }

                // Получаем строки
                while (rs.next()) {
                    List<Object> row = new ArrayList<>();
                    for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
                        row.add(rs.getObject(i));
                    }
                    rows.add(row);
                }
            } catch (Exception e) {
                e.printStackTrace();
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Ошибка при получении данных таблицы");
                return;
            }

            // Формируем HTML-фрагмент
            StringBuilder html = new StringBuilder();
            if (!rows.isEmpty()) {
                html.append("<table class=\"table table-striped\">")
                    .append("<thead><tr>");
                for (String header : headers) {
                    html.append("<th>").append(header).append("</th>");
                }
                html.append("</tr></thead><tbody>");
                for (List<Object> row : rows) {
                    html.append("<tr>");
                    for (Object value : row) {
                        html.append("<td>").append(value).append("</td>");
                    }
                    html.append("</tr>");
                }
                html.append("</tbody></table>");
            } else {
                html.append("<p>Таблица пуста.</p>");
            }

            response.getWriter().write(html.toString());
        } else {
            // Возвращаем HTML-страницу
            response.setContentType("text/html");
            response.setCharacterEncoding("UTF-8");
            request.setAttribute("tableName", tableName);
            request.getRequestDispatcher("/views/table.html").forward(request, response);
        }
    }
    
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
        @SuppressWarnings("unchecked")
        Map<String, Object> data = gson.fromJson(json, Map.class);

        // Извлечение данных из JSON
        String tableName = (String) data.get("tableName");
        @SuppressWarnings("unchecked")
        ArrayList<Map<String, String>> attributes = (ArrayList<Map<String, String>>) data.get("attributes");

        // Создание таблицы в базе данных
        try (Connection conn = DriverManager.getConnection("jdbc:postgresql://postgres:5432/lcp", "postgres", "postgres");
                Statement stmt = conn.createStatement()) {

            StringBuilder sql = new StringBuilder("CREATE TABLE " + tableName + " (id SERIAL PRIMARY KEY");
            for (Map<String, String> attribute : attributes) {
                sql.append(", ").append(attribute.get("name")).append(" ").append(attribute.get("type"));
            }
            sql.append(")");

            stmt.executeUpdate(sql.toString());

            // Успешный ответ
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("success", true);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(gson.toJson(responseData));
        } catch (Exception e) {
            // Ошибка
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("success", false);
            responseData.put("message", e.getMessage());
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(gson.toJson(responseData));
        }
    }
}