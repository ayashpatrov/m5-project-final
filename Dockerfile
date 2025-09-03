# Stage 1: Сборка проекта с Maven
FROM maven:3.9.1-eclipse-temurin-17 AS build
WORKDIR /app
COPY . .
RUN mvn clean package

# Stage 2: Runtime-образ с JRE
FROM openjdk:17-jdk-slim
WORKDIR /app
COPY --from=build /app/target/jira-1.0.jar /app/jira.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/jira.jar", "--spring.profiles.active=prod"]