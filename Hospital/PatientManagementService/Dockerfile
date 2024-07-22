FROM maven:3.8.1-openjdk-17-slim AS build
WORKDIR /app
COPY src /app/src
COPY pom.xml /app/

RUN mvn clean package -DskipTests

FROM openjdk:17-slim
WORKDIR /app

COPY --from=build /app/target/patient-management-service-1.0-SNAPSHOT.jar /app/patient-management-service.jar
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/patient-management-service.jar"]
