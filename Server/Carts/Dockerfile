FROM openjdk:22-jdk-slim

WORKDIR /app

COPY target/Carts-0.0.1-SNAPSHOT.jar /app/Carts-0.0.1-SNAPSHOT.jar

EXPOSE 8086

ENTRYPOINT ["java", "-jar", "/app/Carts-0.0.1-SNAPSHOT.jar"]
