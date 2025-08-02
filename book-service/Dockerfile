FROM openjdk:22-jdk-slim

VOLUME /tmp

COPY target/book-service-1.0.0.jar book-service.jar

EXPOSE 8082

ENTRYPOINT ["java", "-jar", "/book-service.jar"]
