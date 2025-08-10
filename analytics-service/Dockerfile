FROM openjdk:22-jdk-slim

RUN apt-get update && apt-get install -y wget && rm -rf /var/lib/apt/lists/*

VOLUME /tmp

COPY target/analytics-service-1.0.0.jar analytics-service.jar

EXPOSE 8084

ENTRYPOINT ["java", "-jar", "/analytics-service.jar"]
