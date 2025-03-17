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
import java.util.List;

@WebServlet("/forms")
public class FormsServlet extends HttpServlet {
    private FormService formService = new FormService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        User user = (User) request.getSession().getAttribute("user");
        if (user == null) {
            response.sendRedirect("/index.html");
            return;
        }

        int page = Integer.parseInt(request.getParameter("page") != null ? request.getParameter("page") : "0");
        int pageSize = 10; // Количество форм на странице

        List<Form> forms = formService.getFormsByUser(user.getId(), page, pageSize);
        long totalForms = formService.countFormsByUser(user.getId());

        response.setContentType("text/html");
        request.setAttribute("forms", forms);
        request.setAttribute("currentPage", page);
        request.setAttribute("pageSize", pageSize);
        request.setAttribute("totalForms", totalForms);
        request.getRequestDispatcher("/views/forms.html").include(request, response);
    }
}