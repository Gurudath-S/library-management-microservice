FROM openjdk:22-jdk-slim

# Install wget for healthchecks
RUN apt-get update && apt-get install -y wget && rm -rf /var/lib/apt/lists/*

VOLUME /tmp

COPY target/config-server-1.0.0.jar config-server.jar

EXPOSE 8888

ENTRYPOINT ["java", "-jar", "/config-server.jar"]
