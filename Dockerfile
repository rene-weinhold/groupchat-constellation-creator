# -------- Stage 1: Build (Java 24 + Maven) --------
FROM eclipse-temurin:24-jdk-jammy AS build
WORKDIR /app

# Install Maven
RUN apt-get update && apt-get install -y --no-install-recommends maven \
    && rm -rf /var/lib/apt/lists/*

# Leverage layer caching for dependencies
COPY pom.xml .
RUN mvn -q -DskipTests dependency:go-offline

# Build
COPY src ./src
RUN mvn -q clean package -DskipTests

# -------- Stage 2: Runtime (Java 24) --------
FROM eclipse-temurin:24-jre-jammy
WORKDIR /app

# Non-root user
RUN useradd -r -s /usr/sbin/nologin spring
COPY --from=build /app/target/*.jar /app/app.jar

USER spring
EXPOSE 8080
ENTRYPOINT ["java", "-Dserver.port=8080", "-jar", "/app/app.jar"]
