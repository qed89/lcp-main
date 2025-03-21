
# Структура исходного кода проекта
```
\main
    \java
        \com
            \lcp
                \controller
                    ElementServlet.java
                    ElementsServlet.java
                    FormServlet.java
                    FormsServlet.java
                    LoginServlet.java
                    LogoutServlet.java
                    RegisterServlet.java
                    TableServlet.java
                    TablesServlet.java
                \dao
                    ElementDao.java
                    FormDao.java
                    UserDao.java
                \model
                    Element.java
                    Form.java
                    User.java
                \service
                    FormService.java
    \resources
        \META-INF
            persistence.xml
    \webapp
        \css
            animations.css
            sidebar.css
        \js
            \editor
                ace.js
                mode-css.js
                mode-html.js
                worker-css.js
                worker-html.js
            sidebar.js
        \META-INF
            context.xml
        \views
            element.html
            elements.html
            form.html
            forms.html
            sidebar.html
            table.html
            tables.html
        index.html
\test
    \java
        \com
            \lcp
                \model
                    FormTest.java
                    UserTest.java
    \resources
        \META-INF
            persistence.xml
```
# Исходный код проекта
## pom.xml
```
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <groupId>com.lcp</groupId>
    <artifactId>lcp</artifactId>
    <version>lcp</version>
    <packaging>war</packaging>

    <properties>
        <java.version>21</java.version>
    </properties>

    <dependencies>
        <dependency>
            <groupId>jakarta.servlet</groupId>
            <artifactId>jakarta.servlet-api</artifactId>
            <version>6.0.0</version>
            <scope>provided</scope>
        </dependency>
        <dependency>
            <groupId>jakarta.transaction</groupId>
            <artifactId>jakarta.transaction-api</artifactId>
            <version>2.0.1</version>
        </dependency>



        <dependency>
            <groupId>org.postgresql</groupId>
            <artifactId>postgresql</artifactId>
            <version>42.2.20</version>
        </dependency>


        <dependency>
            <groupId>jakarta.persistence</groupId>
            <artifactId>jakarta.persistence-api</artifactId>
            <version>3.2.0</version>
        </dependency>
        <dependency>
            <groupId>org.hibernate</groupId>
            <artifactId>hibernate-core</artifactId>
            <version>5.6.10.Final</version>
        </dependency>
        <dependency>
            <groupId>org.hibernate</groupId>
            <artifactId>hibernate-entitymanager</artifactId>
            <version>5.6.10.Final</version>
        </dependency>


        <dependency>
            <groupId>org.hibernate</groupId>
            <artifactId>hibernate-jpamodelgen</artifactId>
            <version>5.6.10.Final</version>
            <scope>provided</scope>
        </dependency>


        <dependency>
            <groupId>org.mindrot</groupId>
            <artifactId>jbcrypt</artifactId>
            <version>0.4</version>
        </dependency>


        <dependency>
            <groupId>com.google.code.gson</groupId>
            <artifactId>gson</artifactId>
            <version>2.10.1</version>
        </dependency>


        <dependency>
            <groupId>org.hibernate.validator</groupId>
            <artifactId>hibernate-validator</artifactId>
            <version>8.0.0.Final</version>
        </dependency>
        <dependency>
            <groupId>org.glassfish</groupId>
            <artifactId>jakarta.el</artifactId>
            <version>4.0.2</version>
        </dependency>

        
        <dependency>
            <groupId>org.junit.jupiter</groupId>
            <artifactId>junit-jupiter-api</artifactId>
            <version>5.11.4</version>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.junit.jupiter</groupId>
            <artifactId>junit-jupiter-engine</artifactId>
            <version>5.11.4</version>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <finalName>ROOT</finalName>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <version>3.8.1</version>
                <configuration>
                    <source>${java.version}</source>
                    <target>${java.version}</target>
                </configuration>
            </plugin>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-war-plugin</artifactId>
                <version>3.3.1</version>
            </plugin>
        </plugins>
    </build>
</project>
```

## Dockerfile
```
# Этап сборки
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app

# Копируем только pom.xml и устанавливаем зависимости
COPY pom.xml .
RUN mvn dependency:go-offline -B  # Загружаем зависимости и кэшируем их

# Копируем исходный код и собираем проект
COPY src ./src
RUN mvn clean package -DskipTests  # Собираем проект, используя кэшированные зависимости

# Этап запуска
FROM eclipse-temurin:21-jdk-jammy
WORKDIR /app
COPY --from=build /app/target/ROOT.war /opt/wildfly/standalone/deployments/
COPY wildfly-35.0.0.Final /opt/wildfly
EXPOSE 8080 9990
CMD ["/opt/wildfly/bin/standalone.sh", "-b", "0.0.0.0"]
```

## docker-compose.yml
```
version: '3.8'

services:
  web-app:
    image: my-web-app
    build:
      context: .
      dockerfile: Dockerfile
    ports:
      - "8080:8080"
      - "9990:9990"
    depends_on:
      postgres:
        condition: service_healthy
    environment:
      - DATABASE_URL=jdbc:postgresql://postgres:5432/lcp
      - DATABASE_USER=postgres
      - DATABASE_PASSWORD=postgres
    volumes:
      - maven-repo:/root/.m2  # Кэшируем Maven-зависимости
    networks:
      - my-network

  postgres:
    image: postgres:16.6
    environment:
      POSTGRES_DB: lcp
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: postgres
    ports:
      - "5432:5432"
    volumes:
      - postgres-data:/var/lib/postgresql/data
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U postgres -d lcp"]
      interval: 5s
      timeout: 5s
      retries: 5
    networks:
      - my-network

volumes:
  postgres-data:
  maven-repo:  # Добавляем volume для кэширования Maven

networks:
  my-network:
```

## \src\main\java\com\lcp\controller\ElementServlet.java
```
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
```

## \src\main\java\com\lcp\controller\ElementsServlet.java
```
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
```

## \src\main\java\com\lcp\controller\FormServlet.java
```
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

@WebServlet("/f")
public class FormServlet extends HttpServlet {
    private FormService formService = new FormService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html");
        request.getRequestDispatcher("/views/form.html").include(request, response);
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
```

## \src\main\java\com\lcp\controller\FormsServlet.java
```
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
```

## \src\main\java\com\lcp\controller\LoginServlet.java
```
package com.lcp.controller;

import com.google.gson.Gson;
import com.lcp.dao.UserDao;
import com.lcp.model.User;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {
    private UserDao userDao = new UserDao();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String username = request.getParameter("username");
        String password = request.getParameter("password");

        Map<String, String> responseData = new HashMap<>();

        if (username == null || username.isEmpty() || password == null || password.isEmpty()) {
            responseData.put("error", "Имя пользователя и пароль не могут быть пустыми.");
        } else {
            try {
                User user = userDao.findByUsername(username);
                if (user != null && userDao.checkPassword(password, user.getPassword())) {
                    request.getSession().setAttribute("user", user);
                    responseData.put("redirect", "/forms"); // Успешный вход, перенаправляем
                } else {
                    responseData.put("error", "Неверное имя пользователя или пароль.");
                }
            } catch (Exception e) {
                responseData.put("error", "Ошибка при входе: " + e.getMessage());
            }
        }

        // Отправляем JSON-ответ
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(new Gson().toJson(responseData));
    }
}
```

## \src\main\java\com\lcp\controller\LogoutServlet.java
```
package com.lcp.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/logout")
public class LogoutServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.getSession().invalidate();
        response.sendRedirect("index.html");
    }
}
```

## \src\main\java\com\lcp\controller\RegisterServlet.java
```
package com.lcp.controller;

import com.google.gson.Gson;
import com.lcp.dao.UserDao;
import com.lcp.model.User;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@WebServlet("/register")
public class RegisterServlet extends HttpServlet {
    private UserDao userDao = new UserDao();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String username = request.getParameter("username");
        String password = request.getParameter("password");

        Map<String, String> responseData = new HashMap<>();

        User user = new User();
        user.setUsername(username);
        user.setPassword(password);

        // Валидация
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();
        Set<ConstraintViolation<User>> violations = validator.validate(user);

        if (!violations.isEmpty()) {
            StringBuilder errors = new StringBuilder();
            for (ConstraintViolation<User> violation : violations) {
                errors.append(violation.getMessage()).append("<br>");
            }
            responseData.put("error", errors.toString());
        } else if (userDao.findByUsername(username) != null) {
            responseData.put("error", "Пользователь с таким именем уже существует.");
        } else {
            try {
                userDao.save(user);
                responseData.put("redirect", "/index.html"); // Успешная регистрация, перенаправляем
            } catch (Exception e) {
                responseData.put("error", "Ошибка при регистрации: " + e.getMessage());
            }
        }

        // Отправляем JSON-ответ
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(new Gson().toJson(responseData));
    }
}
```

## \src\main\java\com\lcp\controller\TableServlet.java
```
package com.lcp.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

@WebServlet(urlPatterns = {"/t/*", "/t/*/data"})
public class TableServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String pathInfo = request.getPathInfo(); // Получаем полный путь, например, "/users" или "/users/data"
        String tableName = null;

        if (pathInfo != null) {
            // Извлекаем имя таблицы из пути
            String[] pathParts = pathInfo.split("/");
            if (pathParts.length > 1) {
                tableName = pathParts[1]; // Имя таблицы находится на второй позиции
            }
        }

        if (tableName == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Имя таблицы не указано");
            return;
        }

        if (pathInfo.endsWith("/data")) {
            // Возвращаем HTML-фрагмент с данными таблицы
            response.setCharacterEncoding("UTF-8");
            response.setContentType("text/html; charset=UTF-8");

            List<String> headers = new ArrayList<>();
            List<List<Object>> rows = new ArrayList<>();

            try (Connection conn = DriverManager.getConnection("jdbc:postgresql://postgres:5432/lcp", "postgres", "postgres");
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT * FROM " + tableName)) {

                // Получаем заголовки
                for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
                    headers.add(rs.getMetaData().getColumnName(i));
                }

                // Получаем строки
                while (rs.next()) {
                    List<Object> row = new ArrayList<>();
                    for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
                        row.add(rs.getObject(i));
                    }
                    rows.add(row);
                }
            } catch (Exception e) {
                e.printStackTrace();
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Ошибка при получении данных таблицы");
                return;
            }

            // Формируем HTML-фрагмент
            StringBuilder html = new StringBuilder();
            if (!rows.isEmpty()) {
                html.append("<table class=\"table table-striped\">")
                    .append("<thead><tr>");
                for (String header : headers) {
                    html.append("<th>").append(header).append("</th>");
                }
                html.append("</tr></thead><tbody>");
                for (List<Object> row : rows) {
                    html.append("<tr>");
                    for (Object value : row) {
                        html.append("<td>").append(value).append("</td>");
                    }
                    html.append("</tr>");
                }
                html.append("</tbody></table>");
            } else {
                html.append("<p>Таблица пуста.</p>");
            }

            response.getWriter().write(html.toString());
        } else {
            // Возвращаем HTML-страницу
            response.setContentType("text/html");
            response.setCharacterEncoding("UTF-8");
            request.setAttribute("tableName", tableName);
            request.getRequestDispatcher("/views/table.html").forward(request, response);
        }
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Устанавливаем кодировку запроса
        request.setCharacterEncoding("UTF-8");

        // Чтение JSON из тела запроса
        StringBuilder jsonBuilder = new StringBuilder();
        try (BufferedReader reader = request.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                jsonBuilder.append(line);
            }
        }
        String json = jsonBuilder.toString();

        // Парсинг JSON с использованием Gson
        Gson gson = new Gson();
        @SuppressWarnings("unchecked")
        Map<String, Object> data = gson.fromJson(json, Map.class);

        // Извлечение данных из JSON
        String tableName = (String) data.get("tableName");
        @SuppressWarnings("unchecked")
        ArrayList<Map<String, String>> attributes = (ArrayList<Map<String, String>>) data.get("attributes");

        // Создание таблицы в базе данных
        try (Connection conn = DriverManager.getConnection("jdbc:postgresql://postgres:5432/lcp", "postgres", "postgres");
                Statement stmt = conn.createStatement()) {

            StringBuilder sql = new StringBuilder("CREATE TABLE " + tableName + " (id SERIAL PRIMARY KEY");
            for (Map<String, String> attribute : attributes) {
                sql.append(", ").append(attribute.get("name")).append(" ").append(attribute.get("type"));
            }
            sql.append(")");

            stmt.executeUpdate(sql.toString());

            // Успешный ответ
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("success", true);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(gson.toJson(responseData));
        } catch (Exception e) {
            // Ошибка
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("success", false);
            responseData.put("message", e.getMessage());
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(gson.toJson(responseData));
        }
    }
}
```

## \src\main\java\com\lcp\controller\TablesServlet.java
```
package com.lcp.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

@WebServlet(urlPatterns = {"/tables", "/tables-data"})
public class TablesServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String path = request.getServletPath();

        if ("/tables".equals(path)) {
            // Возвращаем HTML-страницу
            response.setContentType("text/html");
            response.setCharacterEncoding("UTF-8");
            request.getRequestDispatcher("/views/tables.html").forward(request, response);
        } else if ("/tables-data".equals(path)) {
            // Возвращаем HTML-фрагмент для таблицы
            response.setCharacterEncoding("UTF-8");
            response.setContentType("text/html; charset=UTF-8");

            List<String> tables = new ArrayList<>();

            try (Connection conn = DriverManager.getConnection("jdbc:postgresql://postgres:5432/lcp", "postgres", "postgres");
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT table_name FROM information_schema.tables WHERE table_schema = 'public'")) {

                while (rs.next()) {
                    tables.add(rs.getString("table_name"));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            // Формируем HTML-фрагмент для строк таблицы
            StringBuilder html = new StringBuilder();
            for (String table : tables) {
                html.append("<tr>")
                    .append("<td>").append(table).append("</td>")
                    .append("<td><a href=\"/t/").append(table).append("\" class=\"btn btn-outline-secondary\">Просмотреть</a></td>")
                    .append("</tr>");
            }

            response.getWriter().write(html.toString());
        }
    }
}
```

## \src\main\java\com\lcp\dao\ElementDao.java
```
package com.lcp.dao;

import com.lcp.model.Element;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import jakarta.persistence.PersistenceException;
import jakarta.persistence.TypedQuery;

import java.util.List;
import java.util.UUID;

public class ElementDao {
    private EntityManagerFactory emf = Persistence.createEntityManagerFactory("my-persistence-unit");

    public void save(Element element) throws PersistenceException {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(element);
            em.getTransaction().commit();
        } catch (PersistenceException e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw new PersistenceException("Ошибка при сохранении элемента: " + e.getMessage(), e);
        } finally {
            em.close();
        }
    }

    public Element getElementById(UUID id) {
        EntityManager em = emf.createEntityManager();
        try {
            return em.find(Element.class, id);
        } catch (Exception e) {
            throw new PersistenceException("Ошибка при получении элемента: " + e.getMessage(), e);
        } finally {
            em.close();
        }
    }

    public List<Element> getElementsByUser(Long userId, int page, int pageSize) {
        EntityManager em = emf.createEntityManager();
        try {
            TypedQuery<Element> query = em.createQuery(
                "SELECT e FROM Element e WHERE e.user.id = :userId ORDER BY e.name", Element.class);
            query.setParameter("userId", userId);
            query.setFirstResult(page * pageSize);
            query.setMaxResults(pageSize);
            return query.getResultList();
        } catch (Exception e) {
            throw new PersistenceException("Ошибка при получении списка элементов: " + e.getMessage(), e);
        } finally {
            em.close();
        }
    }

    public void update(Element element) throws PersistenceException {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            em.merge(element);
            em.getTransaction().commit();
        } catch (PersistenceException e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw new PersistenceException("Ошибка при обновлении элемента: " + e.getMessage(), e);
        } finally {
            em.close();
        }
    }

    public long countElementsByUser(Long userId) {
        EntityManager em = emf.createEntityManager();
        TypedQuery<Long> query = em.createQuery(
            "SELECT COUNT(e) FROM Element e WHERE e.user.id = :userId", Long.class);
        query.setParameter("userId", userId);
        long count = query.getSingleResult();
        em.close();
        return count;
    }
}
```

## \src\main\java\com\lcp\dao\FormDao.java
```
package com.lcp.dao;

import com.lcp.model.Form;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import jakarta.persistence.PersistenceException;
import jakarta.persistence.TypedQuery;

import java.util.List;
import java.util.UUID;

public class FormDao {
    private EntityManagerFactory emf = Persistence.createEntityManagerFactory("my-persistence-unit");

    public void save(Form form) throws PersistenceException {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(form);
            em.getTransaction().commit();
        } catch (PersistenceException e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw new PersistenceException("Ошибка при сохранении формы: " + e.getMessage(), e);
        } finally {
            em.close();
        }
    }
    
    public Form getFormById(UUID id) {
        EntityManager em = emf.createEntityManager();
        try {
            return em.find(Form.class, id);
        } catch (Exception e) {
            throw new PersistenceException("Ошибка при получении формы: " + e.getMessage(), e);
        } finally {
            em.close();
        }
    }
    
    public List<Form> getFormsByUser(Long userId, int page, int pageSize) {
        EntityManager em = emf.createEntityManager();
        try {
            TypedQuery<Form> query = em.createQuery(
                "SELECT f FROM Form f WHERE f.user.id = :userId ORDER BY f.createdDate DESC", Form.class);
            query.setParameter("userId", userId);
            query.setFirstResult(page * pageSize);
            query.setMaxResults(pageSize);
            return query.getResultList();
        } catch (Exception e) {
            throw new PersistenceException("Ошибка при получении списка форм: " + e.getMessage(), e);
        } finally {
            em.close();
        }
    }
    
    public void update(Form form) throws PersistenceException {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            em.merge(form);
            em.getTransaction().commit();
        } catch (PersistenceException e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw new PersistenceException("Ошибка при обновлении формы: " + e.getMessage(), e);
        } finally {
            em.close();
        }
    }
    
    public void delete(UUID id) throws PersistenceException {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            Form form = em.find(Form.class, id);
            if (form != null) {
                em.remove(form);
            }
            em.getTransaction().commit();
        } catch (PersistenceException e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw new PersistenceException("Ошибка при удалении формы: " + e.getMessage(), e);
        } finally {
            em.close();
        }
    }

    public long countFormsByUser(Long userId) {
        EntityManager em = emf.createEntityManager();
        TypedQuery<Long> query = em.createQuery(
            "SELECT COUNT(f) FROM Form f WHERE f.user.id = :userId", Long.class);
        query.setParameter("userId", userId);
        long count = query.getSingleResult();
        em.close();
        return count;
    }
}
```

## \src\main\java\com\lcp\dao\UserDao.java
```
package com.lcp.dao;

import com.lcp.model.User;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.NoResultException;
import jakarta.persistence.Persistence;
import jakarta.persistence.PersistenceException;

import org.mindrot.jbcrypt.BCrypt;

public class UserDao {
    private EntityManagerFactory emf;

    public UserDao() {
        this.emf = Persistence.createEntityManagerFactory("my-persistence-unit");
    }

    public UserDao(EntityManagerFactory emf) {
        this.emf = emf;
    }
    
    public void save(User user) throws PersistenceException {
        EntityManager em = emf.createEntityManager();

        try {
            em.getTransaction().begin();

            // Хешируем пароль перед сохранением
            user.setPassword(BCrypt.hashpw(user.getPassword(), BCrypt.gensalt()));
            em.persist(user);

            em.getTransaction().commit();
        } catch (PersistenceException e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw new PersistenceException("Ошибка при сохранении пользователя: " + e.getMessage(), e);
        } finally {
            em.close();
        }
    }

    public User findByUsername(String username) {
        EntityManager em = emf.createEntityManager();
        
        try {
            return em
                .createQuery("SELECT u FROM User u WHERE u.username = :username", User.class)
                .setParameter("username", username)
                .getSingleResult();
        } catch (NoResultException e) {
            return null; // Пользователь не найден
        } finally {
            em.close();
        }
    }

    public boolean checkPassword(String plainPassword, String hashedPassword) {
        return BCrypt.checkpw(plainPassword, hashedPassword);
    }
}
```

## \src\main\java\com\lcp\model\Element.java
```
package com.lcp.model;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "elements")
public class Element {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String htmlCode;

    @Column(columnDefinition = "TEXT")
    private String cssCode;

    @Column
    private String label;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Геттеры и сеттеры
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getHtmlCode() {
        return htmlCode;
    }

    public void setHtmlCode(String htmlCode) {
        this.htmlCode = htmlCode;
    }

    public String getCssCode() {
        return cssCode;
    }

    public void setCssCode(String cssCode) {
        this.cssCode = cssCode;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
```

## \src\main\java\com\lcp\model\Form.java
```
package com.lcp.model;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "forms")
public class Form {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(name = "created_date", nullable = false)
    private String createdDate;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Геттеры и сеттеры

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(String createdDate) {
        this.createdDate = createdDate;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
```

## \src\main\java\com\lcp\model\User.java
```
package com.lcp.model;

import jakarta.persistence.*;
// import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    @Size(min = 3, max = 50, message = "Имя пользователя должно быть от 3 до 50 символов")
    private String username;

    @Column(nullable = false)
    @Size(min = 18, max = 100, message = "Пароль должно быть от 18 до 100 символов")
    private String password;

    // Геттеры и сеттеры
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
```

## \src\main\java\com\lcp\service\FormService.java
```
package com.lcp.service;

import com.lcp.dao.FormDao;
import com.lcp.model.Form;
import java.util.List;
import java.util.UUID;

public class FormService {
    private FormDao formDao = new FormDao();

    public List<Form> getFormsByUser(Long userId, int page, int pageSize) {
        return formDao.getFormsByUser(userId, page, pageSize);
    }

    public Form getFormById(UUID id) {
        return formDao.getFormById(id);
    }

    public void saveForm(Form form) {
        formDao.save(form);
    }

    public void updateForm(Form form) {
        formDao.update(form);
    }

    public void deleteForm(UUID id) {
        formDao.delete(id);
    }

    public long countFormsByUser(Long userId) {
        return formDao.countFormsByUser(userId);
    }
}
```

## \src\main\resources\META-INF\persistence.xml
```
<persistence xmlns="http://xmlns.jcp.org/xml/ns/persistence"
             xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
             xsi:schemaLocation="http://xmlns.jcp.org/xml/ns/persistence http://xmlns.jcp.org/xml/ns/persistence/persistence_2_2.xsd"
             version="2.2">
    <persistence-unit name="my-persistence-unit">
        <provider>org.hibernate.jpa.HibernatePersistenceProvider</provider>
        <class>com.lcp.model.User</class>
        <class>com.lcp.model.Form</class>
        <class>com.lcp.model.Element</class>
        <properties>
            <property name="jakarta.persistence.jdbc.driver" value="org.postgresql.Driver"/>
            <property name="jakarta.persistence.jdbc.url" value="jdbc:postgresql://postgres:5432/lcp"/>
            <property name="jakarta.persistence.jdbc.user" value="postgres"/>
            <property name="jakarta.persistence.jdbc.password" value="postgres"/>
            <property name="hibernate.hbm2ddl.auto" value="update"/>
            <property name="hibernate.show_sql" value="true"/>
        </properties>
    </persistence-unit>
</persistence>
```

## \src\main\webapp\META-INF\context.xml
```
<Context path=""/>
```

## \src\main\webapp\views\element.html
```
<!DOCTYPE html>
<html lang="ru">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Редактор элемента</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <script src="https://unpkg.com/htmx.org@1.9.3"></script>
    <script src="https://cdnjs.cloudflare.com/ajax/libs/toastr.js/latest/toastr.min.js"></script>
    <link href="https://cdnjs.cloudflare.com/ajax/libs/toastr.js/latest/toastr.min.css" rel="stylesheet">
    <link href="/css/sidebar.css" rel="stylesheet">
    <script src="/js/editor/ace.js"></script>
    <script src="/js/editor/mode-html.js"></script>
    <script src="/js/editor/mode-css.js"></script>
    <style>
        /* Ваши стили остаются без изменений */
        .container-fluid {
            display: flex;
            height: 100vh;
            padding: 0;
        }
        .editor-container {
            flex: 1;
            display: flex;
            flex-direction: column;
            align-items: center;
        }
        .tab-switcher {
            display: flex;
            justify-content: center;
            background-color: #f8f9fa;
            padding: 10px;
            width: 100%;
            border-bottom: 1px solid #ddd;
        }
        .tab-switcher button {
            padding: 10px 20px;
            border: none;
            background: none;
            cursor: pointer;
            color: #007bff;
        }
        .tab-switcher button.active {
            font-weight: bold;
            color: #000;
            background-color: #e9ecef;
        }
        .editor-pane {
            flex: 1;
            width: 80%;
            padding: 20px;
            display: flex;
            justify-content: center;
            align-items: center;
            position: relative;
        }
        .editor {
            width: 100%;
            height: calc(100% - 60px); /* Ограничиваем высоту редактора */
            min-height: 400px;
            border: 1px solid #ddd;
        }
        .tab-content {
            width: 100%;
            height: 100%;
        }
        .preview-pane {
            flex: 1;
            padding: 20px;
            background-color: #fff;
            border-left: 1px solid #ddd;
            overflow: auto;
            display: flex;
            justify-content: center;
            align-items: center;
        }
        .settings-panel {
            width: 250px;
            background-color: #f8f9fa;
            padding: 20px;
            position: fixed;
            right: -250px;
            top: 0;
            height: 100%;
            transition: right 0.3s;
            border-left: 1px solid #ddd;
            z-index: 1000;
        }
        .settings-panel.active {
            right: 0;
        }
        .settings-toggle {
            position: fixed;
            right: 20px;
            top: 20px;
            z-index: 1001;
            transition: right 0.3s;
        }
        .settings-toggle.active {
            right: 270px;
        }
        .save-button {
            position: absolute;
            bottom: 20px;
            left: 50%;
            transform: translateX(-50%);
            z-index: 10;
        }
    </style>
</head>
<body class="bg-light">
    <div class="container-fluid">
        <div class="sidebar" id="sidebar">
            <a href="/forms">Формы</a>
            <a href="/tables">Таблицы</a>
            <a href="/elements">Элементы</a>
        </div>
        <div class="burger-menu" onclick="toggleSidebar()" aria-label="Меню">☰</div>

        <div class="editor-container">
            <div class="tab-switcher">
                <button id="html-tab" class="active" onclick="switchTab('html')">HTML</button>
                <button id="css-tab" onclick="switchTab('css')">CSS</button>
            </div>
            <div class="editor-pane">
                <div id="html-content" class="tab-content">
                    <div id="html-editor" class="editor"></div>
                </div>
                <div id="css-content" class="tab-content" style="display: none;">
                    <div id="css-editor" class="editor"></div>
                </div>

                <!-- Форма для отправки данных через HTMX -->
                <form id="element-form" hx-post="/save-element" hx-target="#preview" hx-swap="innerHTML">
                    <input type="hidden" id="name" name="name">
                    <input type="hidden" id="htmlCode" name="htmlCode">
                    <input type="hidden" id="cssCode" name="cssCode">
                    <input type="hidden" id="label" name="label">
                    <button type="submit" class="btn btn-outline-secondary save-button">Сохранить</button>
                </form>
            </div>
        </div>

        <div class="preview-pane" id="preview"></div>

        <button class="btn btn-outline-secondary settings-toggle" onclick="toggleSettings()" aria-label="Настройки">⚙️</button>
        <div class="settings-panel" id="settings-panel">
            <h5>Настройки элемента</h5>
            <div class="mb-3">
                <label for="name" class="form-label">Название:</label>
                <input type="text" id="name" name="name" class="form-control">
            </div>
            <div class="mb-3">
                <label for="label" class="form-label">Метка:</label>
                <input type="text" id="label" name="label" class="form-control">
            </div>
        </div>
    </div>

    <script>
        // Инициализация редакторов Ace
        ace.config.set('basePath', '/js/editor');

        const htmlEditor = ace.edit("html-editor");
        htmlEditor.session.setMode("ace/mode/html");
        htmlEditor.setOptions({ fontSize: "14px" });

        const cssEditor = ace.edit("css-editor");
        cssEditor.session.setMode("ace/mode/css");
        cssEditor.setOptions({ fontSize: "14px" });

        // Функция для переключения вкладок
        function switchTab(tab) {
            document.querySelectorAll('.tab-switcher button').forEach(el => el.classList.remove('active'));
            document.getElementById(tab + '-tab').classList.add('active');

            if (tab === 'html') {
                document.getElementById('html-content').style.display = 'block';
                document.getElementById('css-content').style.display = 'none';
                htmlEditor.resize();
            } else {
                document.getElementById('html-content').style.display = 'none';
                document.getElementById('css-content').style.display = 'block';
                cssEditor.resize();
            }
            updatePreview();
        }

        // Функция для обновления превью
        function updatePreview() {
            const htmlCode = htmlEditor.getValue();
            const cssCode = cssEditor.getValue();
            const preview = document.getElementById('preview');
            preview.innerHTML = `<style>${cssCode}</style>${htmlCode}`;
        }

        // Функция для отправки данных через HTMX
        function saveElement() {
            // Обновляем значения скрытых полей формы
            document.getElementById('name').value = document.getElementById('name').value;
            document.getElementById('htmlCode').value = htmlEditor.getValue();
            document.getElementById('cssCode').value = cssEditor.getValue();
            document.getElementById('label').value = document.getElementById('label').value;

            // Отправляем форму с помощью HTMX
            htmx.trigger('#element-form', 'submit');
        }

        // Обработка перенаправления после успешного сохранения
        document.body.addEventListener('htmx:afterRequest', function(evt) {
            const response = evt.detail.xhr.responseText;
            const data = JSON.parse(response);
            if (data.redirect) {
                window.location.href = data.redirect;
            }
        });

        // Инициализация редакторов и загрузка данных
        htmlEditor.session.on('change', updatePreview);
        cssEditor.session.on('change', updatePreview);
        window.addEventListener('load', () => {
            htmlEditor.resize();
            cssEditor.resize();
        });
    </script>
    <script src="/js/sidebar.js"></script>
</body>
</html>
```

## \src\main\webapp\views\elements.html
```
<!DOCTYPE html>
<html lang="ru">
<head>
    <meta charset="UTF-8">
    <title>Мои элементы</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <script src="https://unpkg.com/htmx.org@1.9.3"></script>
    <link href="/css/sidebar.css" rel="stylesheet">
    <style>
        .loading-spinner {
            border: 4px solid #f3f3f3;
            border-top: 4px solid #3498db;
            border-radius: 50%;
            width: 40px;
            height: 40px;
            animation: spin 1s linear infinite;
            margin: 20px auto;
        }
        @keyframes spin {
            0% { transform: rotate(0deg); }
            100% { transform: rotate(360deg); }
        }
    </style>
</head>
<body class="bg-light">
    <div class="container mt-5">
        <h1 class="mb-4">Мои элементы</h1>
        <a href="/element/" class="btn btn-outline-secondary mb-3">Создать новый элемент</a>
        <table class="table table-striped">
            <thead>
                <tr>
                    <th>Название</th>
                    <th>Метка</th>
                    <th>Действия</th>
                </tr>
            </thead>
            <tbody id="elements-body" hx-get="/elements-data" hx-trigger="load">
                <tr>
                    <td colspan="3" class="text-center">
                        <div class="loading-spinner"></div>
                    </td>
                </tr>
            </tbody>
        </table>
    </div>
    <script src="/js/sidebar.js"></script>
</body>
</html>
```

## \src\main\webapp\views\form.html
```
<!DOCTYPE html>
<html lang="ru">
<head>
    <meta charset="UTF-8">
    <title>Создание формы</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">

    <script src="https://unpkg.com/htmx.org@1.9.3"></script>
     
    <script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>

    <link href="https://cdnjs.cloudflare.com/ajax/libs/toastr.js/latest/toastr.min.css" rel="stylesheet">
    <script src="https://cdnjs.cloudflare.com/ajax/libs/toastr.js/latest/toastr.min.js"></script>
    
    <script src="https://unpkg.com/htmx.org@1.9.3"></script>
    <link href="/css/sidebar.css" rel="stylesheet">
</head>
<body class="bg-light">
    <div class="container mt-5">
        <h1 class="mb-4">Создание новой формы</h1>
        <form hx-post="/f" hx-swap="none" class="mb-3" id="create-form">
            <div class="mb-3">
                <label for="name" class="form-label">Название формы:</label>
                <input type="text" id="name" name="name" class="form-control" required>
            </div>

            <div id="fields-container">
                <!-- Поля будут добавляться сюда -->
            </div>

            <div class="mb-3">
                <label for="field-type" class="form-label">Добавить поле:</label>
                <select id="field-type" class="form-select">
                    <option value="text">Текстовое поле</option>
                    <option value="checkbox">Чекбокс</option>
                    <option value="dropdown">Выпадающий список</option>
                </select>
                <button type="button" class="btn btn-outline-secondary mt-2" onclick="addField()">Добавить поле</button>
            </div>

            <button type="submit" class="btn btn-outline-secondary">Создать форму</button>
        </form>
        <a href="/forms" class="btn btn-outline-secondary">Назад к формам</a>
    </div>

    <script defer>
        toastr.options = {
            "closeButton": true,
            "positionClass": "toast-top-center",
            "preventDuplicates": true,
            "showDuration": "300",
            "hideDuration": "1000",
            "timeOut": "5000",
            "extendedTimeOut": "1000"
        };

        // Обработка ответа от сервера
        document.getElementById('create-form').addEventListener('submit', function (event) {
            event.preventDefault(); // Отменяем стандартную отправку формы

            const formData = new FormData(this);

            fetch('/f/new', {
                method: 'POST',
                body: formData
            })
            .then(response => response.json())
            .then(data => {
                if (data.redirect) {
                    window.location.href = data.redirect; // Перенаправляем на /forms
                } else if (data.error) {
                    toastr.error(data.error); // Отображаем ошибку через toastr
                }
            })
            .catch(error => {
                toastr.error('Ошибка при отправке формы: ' + error.message);
            });
        });

        function addField() {
            const fieldType = document.getElementById('field-type').value;
            const fieldsContainer = document.getElementById('fields-container');

            const fieldDiv = document.createElement('div');
            fieldDiv.className = 'mb-3';

            let fieldHtml = '';

            if (fieldType === 'text') {
                fieldHtml = `
                    <label class="form-label">Текстовое поле:</label>
                    <input type="text" name="field-name" class="form-control" placeholder="Введите название поля" required>
                `;
            } else if (fieldType === 'checkbox') {
                fieldHtml = `
                    <label class="form-label">Чекбокс:</label>
                    <input type="text" name="field-name" class="form-control" placeholder="Введите название чекбокса" required>
                `;
            } else if (fieldType === 'dropdown') {
                fieldHtml = `
                    <label class="form-label">Выпадающий список:</label>
                    <input type="text" name="field-name" class="form-control" placeholder="Введите название списка" required>
                    <input type="text" name="field-options" class="form-control mt-2" placeholder="Введите варианты через запятую" required>
                `;
            }

            fieldDiv.innerHTML = fieldHtml;
            fieldsContainer.appendChild(fieldDiv);
        }
    </script>

    <script src="/js/sidebar.js"></script>
</body>
</html>
```

## \src\main\webapp\views\forms.html
```
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Мои формы</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <script src="https://unpkg.com/htmx.org@1.9.3"></script>
    <link href="/css/sidebar.css" rel="stylesheet">
</head>
<body class="bg-light">
    <div class="container mt-5">
        <h1 class="mb-4">Мои формы</h1>
        <a href="/f" class="btn btn-outline-secondary mb-3">Создать новую форму</a>
        <div id="forms-table" class="table-responsive">
            <table class="table table-striped">
                <thead>
                    <tr>
                        <th>Название</th>
                        <th>Дата создания</th>
                        <th>Действия</th>
                    </tr>
                </thead>
                <tbody id="forms-body">
                    <tr>
                        <td colspan="3" class="text-center">Загрузка форм...</td>
                    </tr>
                </tbody>
            </table>
        </div>
    </div>

    <script src="/js/sidebar.js"></script>
</body>
</html>
```

## \src\main\webapp\views\sidebar.html
```
<div class="sidebar" id="sidebar">
    <a href="/forms">Формы</a>
    <a href="/tables">Таблицы</a>
    <a href="/elements">Элементы</a>
</div>
<!-- <div class="burger-menu" onclick="toggleSidebar()">&#9776;</div> -->
<div class="burger-menu" onclick="toggleSidebar()">☰</div>
```

## \src\main\webapp\views\table.html
```
<!DOCTYPE html>
<html lang="ru">
<head>
    <meta charset="UTF-8">
    <title>Таблица</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <script src="https://unpkg.com/htmx.org@1.9.3"></script>
    <link href="/css/sidebar.css" rel="stylesheet">
    <style>
        .loading-spinner {
            border: 4px solid #f3f3f3;
            border-top: 4px solid #3498db;
            border-radius: 50%;
            width: 40px;
            height: 40px;
            animation: spin 1s linear infinite;
            margin: 20px auto;
        }

        @keyframes spin {
            0% { transform: rotate(0deg); }
            100% { transform: rotate(360deg); }
        }
    </style>
</head>
<body class="bg-light">
    <div class="container mt-5">
        <h1 class="mb-4">Таблица: <span id="table-name"></span></h1>
        <div id="table-content" hx-get="/t/${tableName}/data" hx-trigger="load">
            <div class="loading-spinner"></div> <!-- Анимация загрузки -->
        </div>
    </div>

    <script>
        // Устанавливаем название таблицы в заголовок
        const tableName = new URL(window.location.href).pathname.split('/').pop();
        document.getElementById('table-name').textContent = tableName;

        // Обновляем HTMX-запрос с правильным URL
        document.getElementById('table-content').setAttribute('hx-get', `/t/${tableName}/data`);
    </script>

    <script src="/js/sidebar.js"></script>
</body>
</html>
```

## \src\main\webapp\views\tables.html
```
<!DOCTYPE html>
<html lang="ru">
<head>
    <meta charset="UTF-8">
    <title>Таблицы базы данных</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <script src="https://unpkg.com/htmx.org@1.9.3"></script>
    <link href="/css/sidebar.css" rel="stylesheet">
    <style>
        .loading-spinner {
            border: 4px solid #f3f3f3;
            border-top: 4px solid #3498db;
            border-radius: 50%;
            width: 40px;
            height: 40px;
            animation: spin 1s linear infinite;
            margin: 20px auto;
        }

        @keyframes spin {
            0% { transform: rotate(0deg); }
            100% { transform: rotate(360deg); }
        }
    </style>
</head>
<body class="bg-light">
    <div class="container mt-5">
        <h1 class="mb-4">Таблицы базы данных</h1>
        <button class="btn btn-outline-secondary mb-3" onclick="openCreateTableForm()">Создать новую таблицу</button>
        <table class="table table-striped">
            <thead>
                <tr>
                    <th>Название таблицы</th>
                    <th>Действия</th>
                </tr>
            </thead>
            <tbody id="tables-body" hx-get="/tables-data" hx-trigger="load">
                <tr>
                    <td colspan="2" class="text-center">
                        <div class="loading-spinner"></div> <!-- Анимация загрузки -->
                    </td>
                </tr>
            </tbody>
        </table>
    </div>

    <!-- Модальное окно для создания таблицы -->
    <div id="create-table-form" class="modal" tabindex="-1">
        <div class="modal-dialog">
            <div class="modal-content">
                <div class="modal-header">
                    <h5 class="modal-title">Создание новой таблицы</h5>
                    <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                </div>
                <div class="modal-body">
                    <form id="create-table-form-content">
                        <div class="mb-3">
                            <label for="table-name" class="form-label">Название таблицы:</label>
                            <input type="text" id="table-name" name="table-name" class="form-control" required>
                        </div>
                        <div id="attributes-container">
                            <!-- Поля для атрибутов таблицы будут добавляться сюда -->
                        </div>
                        <button type="button" class="btn btn-outline-secondary mt-2" onclick="addAttribute()">Добавить атрибут</button>
                    </form>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-outline-secondary" data-bs-dismiss="modal">Закрыть</button>
                    <button type="button" class="btn btn-secondary" onclick="createTable()">Создать таблицу</button>
                </div>
            </div>
        </div>
    </div>

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
    <script>
        function openCreateTableForm() {
            const modal = new bootstrap.Modal(document.getElementById('create-table-form'));
            modal.show();
        }

        function addAttribute() {
            const attributesContainer = document.getElementById('attributes-container');
            const attributeDiv = document.createElement('div');
            attributeDiv.className = 'mb-3';
            attributeDiv.innerHTML = `
                <label class="form-label">Атрибут:</label>
                <input type="text" name="attribute-name" class="form-control" placeholder="Название атрибута" required>
                <select name="attribute-type" class="form-select mt-2">
                    <option value="VARCHAR">Текст</option>
                    <option value="INTEGER">Число</option>
                    <option value="BOOLEAN">Логическое</option>
                    <option value="DATE">Дата</option>
                </select>
            `;
            attributesContainer.appendChild(attributeDiv);
        }

        function createTable() {
            const tableName = document.getElementById('table-name').value;
            const attributes = Array.from(document.querySelectorAll('#attributes-container div')).map(div => {
                return {
                    name: div.querySelector('input[name="attribute-name"]').value,
                    type: div.querySelector('select[name="attribute-type"]').value
                };
            });

            fetch('/t', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({ 'tableName': tableName, 'attributes': attributes })
            }).then(response => response.json())
            .then(data => {
                if (data.success) {
                    window.location.reload();
                } else {
                    alert('Ошибка при создании таблицы: ' + data.message);
                }
            })
            .catch(error => {
                console.error('Ошибка:', error);
            });
        }
    </script>

    <script src="/js/sidebar.js"></script>
</body>
</html>
```

## \src\main\webapp\index.html
```
<!DOCTYPE html>
<html lang="ru">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Главная страница</title>

    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
     
    <script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>

    <link href="https://cdnjs.cloudflare.com/ajax/libs/toastr.js/latest/toastr.min.css" rel="stylesheet">
    <script src="https://cdnjs.cloudflare.com/ajax/libs/toastr.js/latest/toastr.min.js"></script>

    <script src="https://unpkg.com/htmx.org@1.9.3"></script>

    <style>
        /* Стили для переключателя вкладок */
        .welcome-title {
            display: flex;
            justify-content: center;
        }
        .tab-switcher {
            display: flex;
            justify-content: center;
            margin-bottom: 20px;
        }
        .tab-switcher button {
            background: none;
            border: none;
            padding: 10px 20px;
            cursor: pointer;
            font-size: 18px;
            color: #007bff;
            transition: color 0.3s;
        }
        .tab-switcher button.active {
            color: #000;
            font-weight: bold;
            border-bottom: 2px solid #007bff;
        }
        .tab-content {
            display: none;
        }
        .tab-content.active {
            display: block;
        }
    </style>
</head>
<body class="bg-light">
    <div class="container mt-5">
        <div class="row justify-content-center">
            <div class="col-md-6">
                <h1 class="welcome-title">Добро пожаловать!</h1>

                <div class="tab-switcher">
                    <button id="login-tab" class="active">Авторизация</button>
                    <button id="register-tab">Регистрация</button>
                </div>

                <!-- Вкладка "Авторизация" -->
                <div id="login-content" class="tab-content active">
                    <!-- <form action="/login" method="post" id="login-form"> -->
                    <form id="login-form" hx-post="/login" hx-swap="none">
                        <div class="mb-3">
                            <label for="username" class="form-label">Имя пользователя:</label>
                            <input type="text" id="username" name="username" class="form-control" required>
                        </div>
                        <div class="mb-3">
                            <label for="password" class="form-label">Пароль:</label>
                            <input type="password" id="password" name="password" class="form-control" required>
                        </div>
                        <button type="submit" class="btn btn-outline-secondary w-100">Войти</button>
                    </form>
                    <div id="login-error-message" class="mt-3 text-danger text-center"></div>
                </div>

                <!-- Вкладка "Регистрация" -->
                <div id="register-content" class="tab-content">
                    <!-- <form action="/register" method="post" id="register-form"> -->
                    <form id="register-form" hx-post="/register" hx-swap="none">
                        <div class="mb-3">
                            <label for="reg-username" class="form-label">Имя пользователя:</label>
                            <input type="text" id="reg-username" name="username" class="form-control" required>
                        </div>
                        <div class="mb-3">
                            <label for="reg-password" class="form-label">Пароль:</label>
                            <input type="password" id="reg-password" name="password" class="form-control" required>
                        </div>
                        <button type="submit" class="btn btn-outline-secondary w-100">Зарегистрироваться</button>
                    </form>
                    <div id="register-error-message" class="mt-3 text-danger text-center"></div>
                </div>
            </div>
        </div>
    </div>

    <script>
        toastr.options = {
            "closeButton": true,
            "positionClass": "toast-top-center",
            "preventDuplicates": true,
            "showDuration": "300",
            "hideDuration": "1000",
            "timeOut": "5000",
            "extendedTimeOut": "1000"
        };

        // Обработка ответа от HTMX
        document.body.addEventListener('htmx:afterRequest', function(event) {
            const response = JSON.parse(event.detail.xhr.responseText);
            if (response.error) {
                toastr.error(response.error); // Отображаем ошибку
            } else if (response.redirect) {
                window.location.href = response.redirect; // Перенаправляем
            }
        });

        // Переключение между вкладками
        document.getElementById('login-tab').addEventListener('click', function() {
            switchTab('login');
        });

        document.getElementById('register-tab').addEventListener('click', function() {
            switchTab('register');
        });

        function switchTab(tab) {
            // Убираем активный класс у всех вкладок и кнопок
            document.querySelectorAll('.tab-content, .tab-switcher button').forEach(function(element) {
                element.classList.remove('active');
            });

            // Активируем выбранную вкладку и кнопку
            document.getElementById(tab + '-content').classList.add('active');
            document.getElementById(tab + '-tab').classList.add('active');
        }
    </script>
</body>
</html>
```

## \src\test\java\com\lcp\model\FormTest.java
```
package com.lcp.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.UUID;

class FormTest {

    @Test
    void testFormGettersAndSetters() {
        Form form = new Form();
        UUID id = UUID.randomUUID();
        String name = "Test Form";
        String createdDate = "2023-10-01";
        User user = new User();

        form.setId(id);
        form.setName(name);
        form.setCreatedDate(createdDate);
        form.setUser(user);

        assertEquals(id, form.getId());
        assertEquals(name, form.getName());
        assertEquals(createdDate, form.getCreatedDate());
        assertEquals(user, form.getUser());
    }
}
```

## \src\test\java\com\lcp\model\UserTest.java
```
package com.lcp.model;

import org.junit.jupiter.api.Test;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Set;

class UserTest {

    @Test
    void testUserGettersAndSetters() {
        User user = new User();
        Long id = 1L;
        String username = "testuser";
        String password = "password123";

        user.setId(id);
        user.setUsername(username);
        user.setPassword(password);

        assertEquals(id, user.getId());
        assertEquals(username, user.getUsername());
        assertEquals(password, user.getPassword());
    }

    @Test
    void testUserValidation() {
        User user = new User();

        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();
        Set<ConstraintViolation<User>> violations = null;

        // Имя пользователя меньше 3 символов
        user.setUsername("er");
        // Пароль короче 18 символов
        user.setPassword("er");
        violations = validator.validate(user);
        assertEquals(2, violations.size());
        
        // Имя пользователя больше 50 символов
        user.setUsername("more than 50 symbols symbols symbols symbols symbols");
        // Пароль больше 100 символов
        user.setPassword("more than 100 symbols symbols symbols symbols symbols symbols symbols symbols symbols symbols symbols");
        violations = validator.validate(user);
        assertEquals(2, violations.size());

        // Имя пользователя меньше 3 символов
        user.setUsername("");
        // Пароль короче 18 символов
        user.setPassword("");
        violations = validator.validate(user);
        assertEquals(2, violations.size());
    }
}
```

## \src\test\resources\META-INF\persistence.xml
```
<persistence xmlns="http://xmlns.jcp.org/xml/ns/persistence"
             xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
             xsi:schemaLocation="http://xmlns.jcp.org/xml/ns/persistence http://xmlns.jcp.org/xml/ns/persistence/persistence_2_2.xsd"
             version="2.2">
    <persistence-unit name="test-persistence-unit">
        <provider>org.hibernate.jpa.HibernatePersistenceProvider</provider>
        <class>com.lcp.model.User</class>
        <properties>
            <property name="jakarta.persistence.jdbc.driver" value="org.postgresql.Driver"/>
            <property name="jakarta.persistence.jdbc.url" value="jdbc:postgresql://postgres:5432/lcp"/>
            <property name="jakarta.persistence.jdbc.user" value="postgres"/>
            <property name="jakarta.persistence.jdbc.password" value="postgres"/>
            <property name="hibernate.hbm2ddl.auto" value="create-drop"/>
            <property name="hibernate.show_sql" value="true"/>
        </properties>
    </persistence-unit>
</persistence>
```

