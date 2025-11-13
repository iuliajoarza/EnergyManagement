# Build stage
FROM maven:3.9-eclipse-temurin-21 AS build

ENV TZ=UTC
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone
ENV CATALINA_OPTS="-Duser.timezone=UTC"

WORKDIR /app
COPY ./pom.xml ./
RUN mvn -q -e -DskipTests dependency:go-offline
COPY ./src ./src
RUN mvn -q -DskipTests clean package

# Runtime stage
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/microserviceDevice-0.0.1-SNAPSHOT.jar /app/microserviceDevice.jar
EXPOSE 8080
CMD ["java", "-jar", "microserviceDevice.jar"]
