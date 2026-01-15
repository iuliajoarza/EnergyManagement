FROM maven:3.9.5-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
RUN apk --no-cache add ca-certificates && update-ca-certificates
COPY --from=build /app/target/*.jar app.jar
ENV GOOGLE_API_KEY=AIzaSyBJm0kod238iZADsTYEoqY4kU3S9Owz6b0
EXPOSE 8086
ENTRYPOINT ["java", "-jar", "app.jar"]
