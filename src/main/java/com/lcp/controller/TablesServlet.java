package com.lcp.controller;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.lcp.util.HttpClientUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.net.http.HttpResponse;
import java.util.List;

@WebServlet(urlPatterns = {"/tables", "/tables-data"})
public class TablesServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String path = request.getServletPath();

        if ("/tables".equals(path)) {
            // Отображение HTML-страницы
            response.setContentType("text/html");
            response.setCharacterEncoding("UTF-8");
            request.getRequestDispatcher("/views/tables.html").forward(request, response);
        } else if ("/tables-data".equals(path)) {
            // Получение данных для таблицы
            try {
                // Запрос к микросервису на Go
                String url = "http://go-data-service:8081/tables";
                HttpResponse<String> httpResponse = HttpClientUtil.get(url);
                String jsonResponse = httpResponse.body();

                // Десериализация JSON в список таблиц
                List<String> tables = new Gson().fromJson(jsonResponse, new TypeToken<List<String>>() {}.getType());

                // Формирование HTML-таблицы
                String htmlTable = buildHtmlTable(tables);

                // Отправка HTML-фрагмента
                response.setContentType("text/html");
                response.setCharacterEncoding("UTF-8");
                response.getWriter().write(htmlTable);
            } catch (Exception e) {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Ошибка при получении списка таблиц: " + e.getMessage());
            }
        }
    }
    
    private String buildHtmlTable(List<String> tables) {
        StringBuilder html = new StringBuilder();
        for (String table : tables) {
            html.append("<tr>")
                .append("<td>").append(table).append("</td>")
                .append("<td><a href=\"/t/").append(table).append("\" class=\"btn btn-outline-secondary\">Просмотреть</a></td>")
                .append("</tr>");
        }
    
        if (tables.isEmpty()) {
            html.append("<tr><td colspan=\"2\" class=\"text-center\">Нет таблиц</td></tr>");
        }
    
        return html.toString();
    }
}