package com.lcp.controller;

import com.lcp.dao.ElementDao;
import com.lcp.model.Element;
import com.lcp.model.User;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

@WebServlet(urlPatterns = {"/elements", "/elements-data"})
public class ElementsServlet extends HttpServlet {
    private ElementDao elementDao = new ElementDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        User user = (User) request.getSession().getAttribute("user");
        if (user == null) {
            response.sendRedirect("/index.html");
            return;
        }

        String path = request.getServletPath();

        if ("/elements".equals(path)) {
            response.setContentType("text/html");
            response.setCharacterEncoding("UTF-8");
            request.getRequestDispatcher("/views/elements.html").forward(request, response);
        } else if ("/elements-data".equals(path)) {
            response.setContentType("text/html");
            response.setCharacterEncoding("UTF-8");

            int page = Integer.parseInt(request.getParameter("page") != null ? request.getParameter("page") : "0");
            int pageSize = 10;

            List<Element> elements = elementDao.getElementsByUser(user.getId(), page, pageSize);

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

            response.getWriter().write(html.toString());
        }
    }
}