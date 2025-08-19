FROM openjdk:22-jdk-slim

RUN apt-get update && apt-get install -y default-mysql-client

WORKDIR /app

COPY target/Library-0.0.1-SNAPSHOT.jar /app/Library.jar

EXPOSE 8085

ENTRYPOINT ["java", "-jar", "/app/Library.jar"]
