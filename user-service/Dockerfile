FROM openjdk:22-jdk-slim

# Install wget for healthchecks
RUN apt-get update && apt-get install -y wget && rm -rf /var/lib/apt/lists/*

VOLUME /tmp

COPY target/user-service-1.0.0.jar user-service.jar

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "/user-service.jar"]
