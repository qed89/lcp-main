
# Структура исходного кода проекта
```
\main
    \java
        \com
            \lcp
                \config
                    ThymeleafConfig.java
                \controller
                    CreateTableServlet.java
                    FormBuilderServlet.java
                    LoginServlet.java
                    LogoutServlet.java
                    MenuServlet.java
                    RegisterServlet.java
                    TableServlet.java
                    TablesServlet.java
                \dao
                    FormDao.java
                    UserDao.java
                \model
                    Form.java
                    User.java
                \service
                    FormService.java
    \resources
        \META-INF
            persistence.xml
        \views
            table.html
            tables.html
    \webapp
        \META-INF
            context.xml
        \views
            error.html
            formbuilder.html
            userForms.html
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
            <groupId>org.thymeleaf</groupId>
            <artifactId>thymeleaf</artifactId>
            <version>3.1.2.RELEASE</version>
        </dependency>
        <dependency>
            <groupId>org.thymeleaf.extras</groupId>
            <artifactId>thymeleaf-extras-java8time</artifactId>
            <version>3.0.4.RELEASE</version>
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

## \src\main\java\com\lcp\config\ThymeleafConfig.java
```
package com.lcp.config;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

public class ThymeleafConfig {
    private static TemplateEngine templateEngine;

    public static void initialize() {
        // Создаем резолвер шаблонов
        ClassLoaderTemplateResolver templateResolver = new ClassLoaderTemplateResolver();
        templateResolver.setTemplateMode(TemplateMode.HTML); // Режим шаблонов — HTML
        templateResolver.setPrefix("/views/");               // Папка, где находятся шаблоны
        templateResolver.setSuffix(".html");                 // Расширение файлов шаблонов
        templateResolver.setCacheable(false);                // Отключите кэширование для разработки

        // Создаем и настраиваем движок Thymeleaf
        templateEngine = new TemplateEngine();
        templateEngine.setTemplateResolver(templateResolver);
    }

    public static TemplateEngine getTemplateEngine() {
        return templateEngine;
    }
}
```

## \src\main\java\com\lcp\controller\CreateTableServlet.java
```
package com.lcp.controller;

import com.google.gson.Gson;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Map;

@WebServlet("/create-table")
public class CreateTableServlet extends HttpServlet {
    @SuppressWarnings({ "unchecked", "rawtypes" })
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
        Map<String, Object> data = gson.fromJson(json, Map.class);

        // Извлечение данных из JSON
        String tableName = (String) data.get("tableName");
        ArrayList attributes = (ArrayList) data.get("attributes");

        // Создание таблицы в базе данных
        try (Connection conn = DriverManager.getConnection("jdbc:postgresql://localhost:5432/lcp", "postgres", "postgres");
             Statement stmt = conn.createStatement()) {

            StringBuilder sql = new StringBuilder("CREATE TABLE " + tableName + " (id SERIAL PRIMARY KEY");
            for (Object attribute : attributes) {
                Map<String, String> attr = (Map<String, String>) attribute;
                sql.append(", ").append(attr.get("name")).append(" ").append(attr.get("type"));
            }
            sql.append(")");

            stmt.executeUpdate(sql.toString());
            response.getWriter().write("{\"success\": true}");
        } catch (Exception e) {
            response.getWriter().write("{\"success\": false, \"message\": \"" + e.getMessage() + "\"}");
        }
    }
}
```

## \src\main\java\com\lcp\controller\FormBuilderServlet.java
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
```

## \src\main\java\com\lcp\controller\LoginServlet.java
```
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

## \src\main\java\com\lcp\controller\MenuServlet.java
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

@WebServlet("/menu")
public class MenuServlet extends HttpServlet {
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

        List<Form> userForms = formService.getFormsByUser(user.getId(), page, pageSize);
        long totalForms = formService.countFormsByUser(user.getId());

        response.setContentType("text/html");
        request.setAttribute("forms", userForms);
        request.setAttribute("currentPage", page);
        request.setAttribute("pageSize", pageSize);
        request.setAttribute("totalForms", totalForms);
        request.getRequestDispatcher("/views/userForms.html").include(request, response);
    }
}
```

## \src\main\java\com\lcp\controller\RegisterServlet.java
```
package com.lcp.controller;

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
import java.util.Set;

@WebServlet("/register")
public class RegisterServlet extends HttpServlet {
    private UserDao userDao = new UserDao();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String username = request.getParameter("username");
        String password = request.getParameter("password");

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
            sendErrorResponse(response, errors.toString());
            return;
        }

        // Проверка уникальности имени пользователя
        if (userDao.findByUsername(username) != null) {
            sendErrorResponse(response, "Пользователь с таким именем уже существует.");
            return;
        }

        try {
            userDao.save(user);
            response.sendRedirect("index.html");
        } catch (Exception e) {
            sendErrorResponse(response, "Ошибка при регистрации: " + e.getMessage());
        }
    }

    private void sendErrorResponse(HttpServletResponse response, String message) throws IOException {
        response.setContentType("text/html");
        response.getWriter().write("<p style='color:red;'>" + message + "</p>");
    }
}
```

## \src\main\java\com\lcp\controller\TableServlet.java
```
package com.lcp.controller;

import com.lcp.config.ThymeleafConfig;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.web.servlet.JakartaServletWebApplication;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

@WebServlet("/t/*")
public class TableServlet extends HttpServlet {
    private TemplateEngine templateEngine;

    @Override
    public void init() throws ServletException {
        // Инициализация Thymeleaf
        ThymeleafConfig.initialize();
        templateEngine = ThymeleafConfig.getTemplateEngine();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Устанавливаем кодировку ответа
        response.setCharacterEncoding("UTF-8");
        response.setContentType("text/html; charset=UTF-8");

        String tableName = request.getPathInfo().substring(1);

        List<String> headers = new ArrayList<>();
        List<List<Object>> rows = new ArrayList<>();

        // Получаем данные из таблицы
        try (Connection conn = DriverManager.getConnection("jdbc:postgresql://localhost:5432/lcp", "postgres", "postgres");
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
        }

        // Создаем контекст для Thymeleaf
        var application = JakartaServletWebApplication.buildApplication(getServletContext());
        var webExchange = application.buildExchange(request, response);
        WebContext context = new WebContext(webExchange);

        // Передаем данные в шаблон
        context.setVariable("tableName", tableName);
        context.setVariable("headers", headers);
        context.setVariable("rows", rows);

        // Обрабатываем шаблон и отправляем ответ
        templateEngine.process("table", context, response.getWriter());
    }
}
```

## \src\main\java\com\lcp\controller\TablesServlet.java
```
package com.lcp.controller;

import com.lcp.config.ThymeleafConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.web.servlet.JakartaServletWebApplication;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

@WebServlet("/tables")
public class TablesServlet extends HttpServlet {
    private TemplateEngine templateEngine;

    @Override
    public void init() throws ServletException {
        // Инициализация Thymeleaf
        ThymeleafConfig.initialize();
        templateEngine = ThymeleafConfig.getTemplateEngine();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Устанавливаем кодировку ответа
        response.setCharacterEncoding("UTF-8");
        response.setContentType("text/html; charset=UTF-8");
    
        List<String> tables = new ArrayList<>();
    
        // Получаем список таблиц из базы данных
        try (Connection conn = DriverManager.getConnection("jdbc:postgresql://localhost:5432/lcp", "postgres", "postgres");
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT table_name FROM information_schema.tables WHERE table_schema = 'public'")) {
    
            while (rs.next()) {
                tables.add(rs.getString("table_name"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    
        // Создаем контекст для Thymeleaf
        var application = JakartaServletWebApplication.buildApplication(getServletContext());
        var webExchange = application.buildExchange(request, response);
        WebContext context = new WebContext(webExchange);
    
        // Передаем данные в шаблон
        context.setVariable("tables", tables);
    
        // Обрабатываем шаблон и отправляем ответ
        templateEngine.process("tables", context, response.getWriter());
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

    // Геттер для id
    public Long getId() {
        return id;
    }

    // Сеттер для id
    public void setId(Long id) {
        this.id = id;
    }

    // Геттер для username
    public String getUsername() {
        return username;
    }

    // Сеттер для username
    public void setUsername(String username) {
        this.username = username;
    }

    // Геттер для password
    public String getPassword() {
        return password;
    }

    // Сеттер для password
    public void setPassword(String password) {
        this.password = password;
    }
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

## \src\main\resources\views\table.html
```
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8">
    <title th:text="${tableName}">Таблица</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
</head>
<body class="bg-light">
    <div class="container mt-5">
        <h1 class="mb-4" th:text="'Таблица: ' + ${tableName}">Таблица</h1>
        <div th:if="${not rows.empty}">
            <table class="table table-striped">
                <thead>
                    <tr>
                        <th th:each="header : ${headers}" th:text="${header}">Заголовок</th>
                    </tr>
                </thead>
                <tbody>
                    <tr th:each="row : ${rows}">
                        <td th:each="value : ${row}" th:text="${value}">Значение</td>
                    </tr>
                </tbody>
            </table>
        </div>
        <div th:if="${rows.empty}">
            <p>Таблица пуста.</p>
        </div>
    </div>
</body>
</html>
```

## \src\main\resources\views\tables.html
```
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8">
    <title>Таблицы базы данных</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
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
            <tbody>
                <tr th:each="table : ${tables}">
                    <td th:text="${table}">Название таблицы</td>
                    <td><a th:href="@{/t/{name}(name=${table})}" class="btn btn-outline-secondary">Просмотреть</a></td>
                </tr>
            </tbody>
        </table>
    </div>

    <!-- Форма для создания новой таблицы -->
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

    <!-- Подключение Bootstrap JS -->
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

            fetch('/create-table', {
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
</body>
</html>
```

## \src\main\webapp\META-INF\context.xml
```
<Context path=""/>
```

## \src\main\webapp\views\error.html
```
<!DOCTYPE html>
<html lang="ru">
<head>
    <meta charset="UTF-8">
    <title>Ошибка</title>
</head>
<body>
    <h1>Ошибка</h1>
    <p>${error}</p>
    <a href="/menu">Вернуться к формам</a>
</body>
</html>
```

## \src\main\webapp\views\formbuilder.html
```
<!DOCTYPE html>
<html lang="ru">
<head>
    <meta charset="UTF-8">
    <title>Создание формы</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <script>
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
</head>
<body class="bg-light">
    <div class="container mt-5">
        <h1 class="mb-4">Создание новой формы</h1>
        <form action="/formbuilder" method="post" class="mb-3">
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
        <a href="/menu" class="btn btn-outline-secondary">Назад к формам</a>
    </div>
</body>
</html>
```

## \src\main\webapp\views\userForms.html
```
<!DOCTYPE html>
<html lang="ru">
<head>
    <meta charset="UTF-8">
    <title>Мои формы</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <script src="https://unpkg.com/htmx.org@1.9.3"></script>
</head>
<body class="bg-light">
    <div class="container mt-5">
        <h1 class="mb-4">Мои формы</h1>
        <a href="/formbuilder" class="btn btn-outline-secondary mb-3">Создать новую форму</a>
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
    <style>
        /* Стили для переключателя вкладок */
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
                <h1 class="display-4">Доброго пожаловать!</h1>
                <!-- Переключатель вкладок -->
                <div class="tab-switcher">
                    <button id="login-tab" class="active">Авторизация</button>
                    <button id="register-tab">Регистрация</button>
                </div>

                <!-- Вкладка "Авторизация" -->
                <div id="login-content" class="tab-content active">
                    <form action="/login" method="post" id="login-form">
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
                    <form action="/register" method="post" id="register-form">
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

