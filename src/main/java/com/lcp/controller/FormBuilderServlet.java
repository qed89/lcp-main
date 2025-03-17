package com.lcp.controller;

import com.lcp.model.Form;
import com.lcp.model.User;
import com.lcp.service.FormService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@WebServlet("/formbuilder")
public class FormBuilderServlet extends HttpServlet {
    private FormService formService = new FormService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html");
        request.getRequestDispatcher("/views/formbuilder.html").include(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String name = request.getParameter("name");
        User user = (User) request.getSession().getAttribute("user");

        Form form = new Form();
        form.setId(UUID.randomUUID());
        form.setName(name);
        form.setCreatedDate(java.time.LocalDate.now().toString());
        form.setUser(user);

        Map<String, String> responseData = new HashMap<>();

        try {
            formService.saveForm(form);
            responseData.put("redirect", "/forms"); // Успешное создание формы, перенаправляем
        } catch (Exception e) {
            responseData.put("error", "Ошибка при создании формы: " + e.getMessage()); // Ошибка
        }

        // Отправляем JSON-ответ
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(new com.google.gson.Gson().toJson(responseData));
    }
}