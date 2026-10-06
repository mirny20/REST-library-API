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

# Directory for persistent H2 database (mount a volume here to keep data between restarts)
RUN mkdir -p /data

EXPOSE 8080

# DB path can be overridden via DB_PATH env variable (defaults to /data)
ENV DB_PATH=/data

ENTRYPOINT java -Dspring.datasource.dbpath=${DB_PATH} -jar app.war
