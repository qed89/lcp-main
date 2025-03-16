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

        try {
            formService.saveForm(form);
            response.sendRedirect("/menu");
        } catch (Exception e) {
            request.setAttribute("error", "Ошибка при создании формы: " + e.getMessage());
            request.getRequestDispatcher("/views/error.html").include(request, response);
        }
    }
}