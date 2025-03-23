package com.lcp.controller;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.lcp.model.Element;
import com.lcp.util.HttpClientUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.net.http.HttpResponse;
import java.util.List;

@WebServlet(urlPatterns = {"/elements", "/elements-data"})
public class ElementsServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String path = request.getServletPath();

        if ("/elements".equals(path)) {
            // Отображение HTML-страницы
            response.setContentType("text/html");
            response.setCharacterEncoding("UTF-8");
            request.getRequestDispatcher("/views/elements.html").forward(request, response);
        } else if ("/elements-data".equals(path)) {
            // Получение данных для таблицы
            int page = Integer.parseInt(request.getParameter("page") != null ? request.getParameter("page") : "0");
            int pageSize = 10;

            try {
                // Запрос к микросервису на Go
                String url = "http://go-data-service:8081/elements?page=" + page + "&pageSize=" + pageSize;
                HttpResponse<String> httpResponse = HttpClientUtil.get(url);
                String jsonResponse = httpResponse.body();

                // Десериализация JSON в список элементов
                List<Element> elements = new Gson().fromJson(jsonResponse, new TypeToken<List<Element>>() {}.getType());

                // Формирование HTML-таблицы
                StringBuilder html = new StringBuilder();
                for (Element element : elements) {
                    html.append("<tr>")
                        .append("<td>").append(element.getName()).append("</td>")
                        .append("<td>").append(element.getLabel() != null ? element.getLabel() : "").append("</td>")
                        .append("<td><a href=\"/element/").append(element.getId()).append("\" class=\"btn btn-outline-secondary\">Просмотреть</a></td>")
                        .append("</tr>");
                }

                if (elements.isEmpty()) {
                    html.append("<tr><td colspan=\"3\" class=\"text-center\">Нет элементов</td></tr>");
                }

                // Отправка HTML-фрагмента
                response.setContentType("text/html");
                response.setCharacterEncoding("UTF-8");
                response.getWriter().write(html.toString());
            } catch (Exception e) {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Ошибка при получении списка элементов: " + e.getMessage());
            }
        }
    }
}