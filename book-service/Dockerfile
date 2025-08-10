FROM openjdk:22-jdk-slim

RUN apt-get update && apt-get install -y wget && rm -rf /var/lib/apt/lists/*

VOLUME /tmp

COPY target/book-service-1.0.0.jar book-service.jar

EXPOSE 8082

ENTRYPOINT ["java", "-jar", "/book-service.jar"]
