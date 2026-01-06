# Utilisation d'une image Java 17
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# On copie le JAR qui contient toutes les dépendances
# Assure-tu que le nom correspond bien à ce qui est généré dans ton dossier /target
COPY target/esport-monitor-1.0-SNAPSHOT-jar-with-dependencies.jar app.jar

EXPOSE 8080

CMD ["java", "-jar", "app.jar"]