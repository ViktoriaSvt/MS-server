FROM openjdk:22-jdk-slim

WORKDIR /app

COPY target/Auth-0.0.1-SNAPSHOT.jar Auth.jar

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "Auth.jar"]