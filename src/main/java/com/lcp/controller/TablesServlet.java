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

@WebServlet("/tables")
public class TablesServlet extends HttpServlet {
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
    
        List<String> tables = new ArrayList<>();
    
        // Получаем список таблиц из базы данных
        try (Connection conn = DriverManager.getConnection("jdbc:postgresql://localhost:5432/lcp", "postgres", "postgres");
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT table_name FROM information_schema.tables WHERE table_schema = 'public'")) {
    
            while (rs.next()) {
                tables.add(rs.getString("table_name"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    
        // Создаем контекст для Thymeleaf
        var application = JakartaServletWebApplication.buildApplication(getServletContext());
        var webExchange = application.buildExchange(request, response);
        WebContext context = new WebContext(webExchange);
    
        // Передаем данные в шаблон
        context.setVariable("tables", tables);
    
        // Обрабатываем шаблон и отправляем ответ
        templateEngine.process("tables", context, response.getWriter());
    }
}