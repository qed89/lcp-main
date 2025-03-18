package com.lcp.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

@WebServlet(urlPatterns = {"/tables", "/tables-data"})
public class TablesServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String path = request.getServletPath();

        if ("/tables".equals(path)) {
            // Возвращаем HTML-страницу
            response.setContentType("text/html");
            response.setCharacterEncoding("UTF-8");
            request.getRequestDispatcher("/views/tables.html").forward(request, response);
        } else if ("/tables-data".equals(path)) {
            // Возвращаем HTML-фрагмент для таблицы
            response.setCharacterEncoding("UTF-8");
            response.setContentType("text/html; charset=UTF-8");

            List<String> tables = new ArrayList<>();

            try (Connection conn = DriverManager.getConnection("jdbc:postgresql://postgres:5432/lcp", "postgres", "postgres");
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT table_name FROM information_schema.tables WHERE table_schema = 'public'")) {

                while (rs.next()) {
                    tables.add(rs.getString("table_name"));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            // Формируем HTML-фрагмент для строк таблицы
            StringBuilder html = new StringBuilder();
            for (String table : tables) {
                html.append("<tr>")
                    .append("<td>").append(table).append("</td>")
                    .append("<td><a href=\"/t/").append(table).append("\" class=\"btn btn-outline-secondary\">Просмотреть</a></td>")
                    .append("</tr>");
            }

            response.getWriter().write(html.toString());
        }
    }
}