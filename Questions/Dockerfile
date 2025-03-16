FROM openjdk:22-jdk-slim

WORKDIR /app

COPY target/Questions-0.0.1-SNAPSHOT.jar Questions.jar

EXPOSE 8082

ENTRYPOINT ["java", "-jar", "Questions.jar"]