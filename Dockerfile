# Étape 1 : build
FROM maven:3.9.1-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Étape 2 : runtime
FROM eclipse-temurin:17
WORKDIR /app
COPY --from=build /app/target/restcompiler-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080
CMD ["java", "-jar", "app.jar"]
