# Stage 1: Build with Maven on Java 17
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app

# Copy all files and build
COPY . .
RUN mvn clean package -DskipTests

# Stage 2: Runtime on Java 17
FROM openjdk:17-jdk-slim
WORKDIR /app

# Copy the built jar
COPY --from=build /app/target/*.jar app.jar

# Expose port
EXPOSE 8080

# Run the app
ENTRYPOINT ["java", "-jar", "app.jar"]
