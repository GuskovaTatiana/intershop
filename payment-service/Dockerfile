# Стадия сборки
FROM gradle:8.7-jdk21 AS builder
WORKDIR /app

# Копируем все необходимые Gradle файлы сначала
COPY build.gradle .
COPY settings.gradle .
#COPY gradle.properties .
COPY gradlew .
COPY gradle ./gradle

# Даем права на выполнение gradlew
RUN chmod +x gradlew

# Копируем исходный код модулей
COPY payment-service ./payment-service
#COPY src ./src

# Собираем с помощью Gradle wrapper
RUN ./gradlew :payment-service:clean :payment-service:bootJar -x test

# Стадия запуска
FROM openjdk:21-jdk-slim
WORKDIR /app
COPY --from=builder /app/payment-service/build/libs/*.jar app.jar
EXPOSE 8009
ENTRYPOINT ["java", "-jar", "app.jar"]