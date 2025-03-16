FROM openjdk:22-jdk-slim

WORKDIR /app

COPY target/Payments-0.0.1-SNAPSHOT.jar Payments.jar

EXPOSE 8083

ENTRYPOINT ["java", "-jar", "Payments.jar"]