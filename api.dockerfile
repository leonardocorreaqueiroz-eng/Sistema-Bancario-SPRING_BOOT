FROM eclipse-temurin:21-alpine
WORKDIR /app
EXPOSE 8080
COPY target/Sistema_Bancario-SpringBoot-0.0.1-SNAPSHOT.jar app.jar
CMD ["java", "-jar", "app.jar"]
