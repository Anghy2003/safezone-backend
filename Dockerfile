# =========================
# BUILD (Maven + Java 21)
# =========================
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

COPY pom.xml .
COPY .mvn .mvn
COPY mvnw mvnw
RUN chmod +x mvnw && ./mvnw -q -B dependency:go-offline

COPY src src
RUN ./mvnw -q -DskipTests package

# =========================
# RUNTIME (Java 21)
# =========================
FROM eclipse-temurin:21-jre
WORKDIR /app

ENV PORT=8080

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["sh","-c","java -jar app.jar"]
