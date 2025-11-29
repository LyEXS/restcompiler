# Dockerfile
FROM eclipse-temurin:17-jdk-alpine

# Installer gcc et utilitaires
RUN apk add --no-cache gcc g++ make bash

WORKDIR /app

# Copier le projet compilé
COPY target/restcompiler-0.0.1-SNAPSHOT.jar app.jar

# Créer un dossier sandbox
RUN mkdir -p /app/sandbox

# Exposer le port 8080
EXPOSE 8080

# Lancer l'application
ENTRYPOINT ["java", "-jar", "app.jar"]
