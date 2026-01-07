FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

COPY target/esport-monitor-1.0-SNAPSHOT-jar-with-dependencies.jar app.jar

EXPOSE 8080

CMD ["java", "-jar", "app.jar"]