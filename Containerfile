# Étape runtime uniquement (on a déjà build localement)
FROM eclipse-temurin:17-jdk

WORKDIR /app

# Copier le JAR compilé localement
COPY target/restcompiler-0.0.1-SNAPSHOT.jar app.jar

# Créer un dossier sandbox dans le conteneur
RUN mkdir -p /app/sandbox

EXPOSE 8080

ENTRYPOINT ["java","-jar","app.jar"]
