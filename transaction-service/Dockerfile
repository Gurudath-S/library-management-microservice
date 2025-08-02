FROM openjdk:22-jdk-slim

VOLUME /tmp

COPY target/transaction-service-1.0.0.jar transaction-service.jar

EXPOSE 8083

ENTRYPOINT ["java", "-jar", "/transaction-service.jar"]
