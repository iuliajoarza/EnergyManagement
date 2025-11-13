# Build stage
FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /app
# Cache dependencies
COPY ./pom.xml ./
RUN mvn -q -e -DskipTests dependency:go-offline
# Build
COPY ./src ./src
RUN mvn -q -DskipTests clean package

# Runtime stage
FROM eclipse-temurin:21-jre
WORKDIR /app
# Copy the built jar from the build stage (correct artifact name)
COPY --from=build /app/target/AuthService-0.0.1-SNAPSHOT.jar /app/app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
