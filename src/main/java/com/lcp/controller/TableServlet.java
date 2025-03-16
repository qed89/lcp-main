package com.lcp.controller;

import com.lcp.config.ThymeleafConfig;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.web.servlet.JakartaServletWebApplication;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

@WebServlet("/t/*")
public class TableServlet extends HttpServlet {
    private TemplateEngine templateEngine;

    @Override
    public void init() throws ServletException {
        // Инициализация Thymeleaf
        ThymeleafConfig.initialize();
        templateEngine = ThymeleafConfig.getTemplateEngine();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Устанавливаем кодировку ответа
        response.setCharacterEncoding("UTF-8");
        response.setContentType("text/html; charset=UTF-8");

        String tableName = request.getPathInfo().substring(1);

        List<String> headers = new ArrayList<>();
        List<List<Object>> rows = new ArrayList<>();

        // Получаем данные из таблицы
        try (Connection conn = DriverManager.getConnection("jdbc:postgresql://localhost:5432/lcp", "postgres", "postgres");
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
        }

        // Создаем контекст для Thymeleaf
        var application = JakartaServletWebApplication.buildApplication(getServletContext());
        var webExchange = application.buildExchange(request, response);
        WebContext context = new WebContext(webExchange);

        // Передаем данные в шаблон
        context.setVariable("tableName", tableName);
        context.setVariable("headers", headers);
        context.setVariable("rows", rows);

        // Обрабатываем шаблон и отправляем ответ
        templateEngine.process("table", context, response.getWriter());
    }
}