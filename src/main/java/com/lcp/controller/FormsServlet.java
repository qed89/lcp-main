package com.lcp.controller;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.lcp.model.Form;
import com.lcp.util.HttpClientUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.net.http.HttpResponse;
import java.util.List;

@WebServlet("/forms")
public class FormsServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        int page = Integer.parseInt(request.getParameter("page") != null ? request.getParameter("page") : "0");
        int pageSize = 10;

        try {
            // Формируем URL для запроса к микросервису
            String url = System.getenv("MICROSERVICE_URL") + "/forms?page=" + page + "&pageSize=" + pageSize;
            HttpResponse<String> httpResponse = HttpClientUtil.get(url);
            String jsonResponse = httpResponse.body();

            // Десериализация JSON в список форм
            List<Form> forms = new Gson().fromJson(jsonResponse, new TypeToken<List<Form>>() {}.getType());

            // Передача данных в JSP
            request.setAttribute("forms", forms);
            request.getRequestDispatcher("/views/forms.html").forward(request, response);
        } catch (Exception e) {
            // Логируем ошибку
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Ошибка при получении списка форм: " + e.getMessage());
        }
    }
}