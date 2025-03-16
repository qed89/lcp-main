package com.lcp.controller;

import com.lcp.dao.UserDao;
import com.lcp.model.User;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {
    private UserDao userDao = new UserDao();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String username = request.getParameter("username");
        String password = request.getParameter("password");

        System.out.println("ASE Username: " + username); // Логирование

        if (username == null || username.isEmpty() || password == null || password.isEmpty()) {
            sendErrorResponse(response, "Имя пользователя и пароль не могут быть пустыми.");
            return;
        }

        try {
            User user = userDao.findByUsername(username);
            if (user != null && userDao.checkPassword(password, user.getPassword())) {
                request.getSession().setAttribute("user", user);
                response.sendRedirect("/menu");
            } else {
                sendErrorResponse(response, "Неверное имя пользователя или пароль.");
            }
        } catch (Exception e) {
            sendErrorResponse(response, "Ошибка при входе: " + e.getMessage());
        }
    }

    private void sendErrorResponse(HttpServletResponse response, String message) throws IOException {
        response.setContentType("text/html");
        response.getWriter().write("<p style='color:red;'>" + message + "</p>");
    }
}