# --- Stage 1 : build ---
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# On copie d'abord le pom.xml seul pour profiter du cache Docker sur les dependances
COPY pom.xml .
RUN mvn -q -B dependency:go-offline

COPY src ./src
RUN mvn -q -B -DskipTests package

# --- Stage 2 : runtime ---
FROM eclipse-temurin:21-jre-jammy AS runtime
WORKDIR /app

# Utilisateur non-root
RUN useradd -r -u 1001 appuser
USER appuser

COPY --from=build /app/target/*.jar app.jar

# Railway injecte PORT dynamiquement ; application.yml le lit deja
EXPOSE 8080

ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "app.jar"]
