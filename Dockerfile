# Schritt 1: App bauen mit Java 21
FROM azul-zulu:25 AS build
WORKDIR /app

# Kopiere Datein
COPY pom.xml .
COPY src ./src
COPY frontend ./frontend

# Baue Projekt
RUN apt-get update && apt-get install -y maven && mvn package -DskipTests

# ----- Runtime Image -----
FROM azul-zulu:21-jre
WORKDIR /app

# Erstelle den Ordner für deine Daten im Container
RUN mkdir -p /app/data

# Dateien kopieren
COPY --from=build /app/target/Vivien-*.jar Vivien.jar
# RUN ln -s /app/data/conf/vivien-server.toml /app/vivien-server.toml

# JVM-Flags
ENV JAVA_OPTS="-Xmx512m -Xms512m"

EXPOSE 8080

# Startbefehl
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar Vivien.jar"]