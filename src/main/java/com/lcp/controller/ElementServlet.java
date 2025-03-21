package com.lcp.controller;

import com.google.gson.Gson;
import com.lcp.dao.ElementDao;
import com.lcp.model.Element;
import com.lcp.model.User;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@WebServlet(urlPatterns = {"/element/*", "/element/*/data"})
public class ElementServlet extends HttpServlet {
    private ElementDao elementDao = new ElementDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        User user = (User) request.getSession().getAttribute("user");
        if (user == null) {
            response.sendRedirect("/index.html");
            return;
        }

        String pathInfo = request.getPathInfo();
        String elementId = null;

        if (pathInfo != null && !pathInfo.equals("/")) {
            String[] pathParts = pathInfo.split("/");
            if (pathParts.length > 1) {
                elementId = pathParts[1];
            }
        }

        if (pathInfo != null && pathInfo.endsWith("/data")) {
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");

            if (elementId == null || elementId.isEmpty()) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Идентификатор элемента не указан");
                return;
            }

            Element element = elementDao.getElementById(UUID.fromString(elementId));
            if (element == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Элемент не найден");
                return;
            }

            Map<String, String> data = new HashMap<>();
            data.put("name", element.getName());
            data.put("htmlCode", element.getHtmlCode() != null ? element.getHtmlCode() : "");
            data.put("cssCode", element.getCssCode() != null ? element.getCssCode() : "");
            data.put("label", element.getLabel() != null ? element.getLabel() : "");

            response.getWriter().write(new Gson().toJson(data));
        } else {
            response.setContentType("text/html");
            response.setCharacterEncoding("UTF-8");
            request.getRequestDispatcher("/views/element.html").forward(request, response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        User user = (User) request.getSession().getAttribute("user");
        if (user == null) {
            response.sendRedirect("/index.html");
            return;
        }

        String pathInfo = request.getPathInfo();
        String elementId = null;

        if (pathInfo != null && !pathInfo.equals("/")) {
            String[] pathParts = pathInfo.split("/");
            if (pathParts.length > 1) {
                elementId = pathParts[1];
            }
        }

        StringBuilder jsonBuilder = new StringBuilder();
        try (BufferedReader reader = request.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                jsonBuilder.append(line);
            }
        }
        String json = jsonBuilder.toString();

        Gson gson = new Gson();
        @SuppressWarnings("unchecked")
        Map<String, String> data = gson.fromJson(json, Map.class);

        String name = data.get("name");
        String htmlCode = data.get("htmlCode");
        String cssCode = data.get("cssCode");
        String label = data.get("label");

        Element element;
        if (elementId != null) {
            element = elementDao.getElementById(UUID.fromString(elementId));
            if (element == null) {
                sendErrorResponse(response, "Элемент не найден");
                return;
            }
        } else {
            element = new Element();
            element.setId(UUID.randomUUID());
            element.setUser(user);
        }

        element.setName(name);
        element.setHtmlCode(htmlCode);
        element.setCssCode(cssCode);
        element.setLabel(label);

        try {
            if (elementId == null) {
                elementDao.save(element);
            } else {
                elementDao.update(element);
            }
            sendSuccessResponse(response, "/elements");
        } catch (Exception e) {
            sendErrorResponse(response, "Ошибка при сохранении элемента: " + e.getMessage());
        }
    }

    private void sendSuccessResponse(HttpServletResponse response, String redirect) throws IOException {
        Map<String, String> responseData = new HashMap<>();
        responseData.put("redirect", redirect);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(new Gson().toJson(responseData));
    }

    private void sendErrorResponse(HttpServletResponse response, String error) throws IOException {
        Map<String, String> responseData = new HashMap<>();
        responseData.put("error", error);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(new Gson().toJson(responseData));
    }
}