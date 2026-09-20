# Schritt 1: App bauen mit Java 21
FROM maven:3.9-eclipse-temurin-21-alpine AS build
WORKDIR /app

# Kopiere Konfiguration und lade Abhängigkeiten
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Quellcode kopieren und bauen
COPY src ./src
COPY frontend ./frontend
RUN mvn package -DskipTests

# Schritt 2: Schlankes Laufzeit-Image mit Java 21
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Erstelle den Ordner für deine Daten im Container
RUN mkdir -p /app/data

# Dateien kopieren
COPY --from=build /app/target/Vivien-*.jar Vivien.jar
RUN ln -s /app/data/conf/vivien-server.toml /app/vivien-server.toml

# JVM-Flags
ENV JAVA_OPTS="-Xmx512m -Xms512m"

EXPOSE 8080

# Startbefehl
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar Vivien.jar"]