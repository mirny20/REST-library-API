# Stage 1: Build application with Maven and JDK 17
FROM maven:3.8.8-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Minimal runtime image
FROM eclipse-temurin:17-jre
WORKDIR /app

# Copy packaged WAR/JAR
COPY --from=build /app/target/*.war app.war

# Copy pre-populated H2 database files (authors, genres, books)
COPY src/main/resources/db/library.mv.db ./src/main/resources/db/library.mv.db

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.war"]
