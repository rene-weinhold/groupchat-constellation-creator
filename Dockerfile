# -------- Stage 1: Build the app --------
FROM eclipse-temurin:24-jdk-alpine AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# -------- Stage 2: Create final container --------
FROM eclipse-temurin:24-jdk-alpine
WORKDIR /app
RUN addgroup -S spring && adduser -S spring -G spring
COPY --from=build /app/target/*.jar app.jar
USER spring
EXPOSE 8080
ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-Dserver.port=8080", "-jar", "app.jar"]
