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