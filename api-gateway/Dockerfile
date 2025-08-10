FROM openjdk:22-jdk-slim

RUN apt-get update && apt-get install -y wget && rm -rf /var/lib/apt/lists/*

VOLUME /tmp

COPY target/api-gateway-1.0.0.jar api-gateway.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/api-gateway.jar"]
