# 🎨 Vivien - Developer Cheat Sheet (Quick Start)

Gedankenstütze für den täglichen Entwicklungs-Workflow (Java backend + Vue/TS frontend).

---

## 🛠️ Erstmaliges Setup (nach dem Auschecken)

### 1. Frontend-Abhängigkeiten installieren
Navigiere in den Frontend-Ordner und installiere Node-Pakete:
```bash
cd frontend
npm install
```

### 2. Server-Konfiguration anlegen
Erstelle eine Datei namens `vivien-server.toml` im **Hauptverzeichnis** (neben der `pom.xml`).
*Hinweis: Wenn sie fehlt oder Syntaxfehler hat, startet Vivien automatisch im `SETUP`-Modus.*

```toml
# Vivien Server Konfiguration
mode = "local"

# Das Git-Repository, auf dem Vivien arbeiten soll
repo_url = "https://github.com"
local_repo_path = "./game-repo"

[server]
host = "localhost"
port = 8080
security = "lax"
---

## 🔄 Täglicher Dev-Workflow (Zwei Server laufen parallel)

### Schritt 1: Java Backend starten (IntelliJ)
Starte die `ServerMain.java` einfach über den grünen Play-Button in IntelliJ.
* Backend läuft auf: `http://localhost:8080`
* Im `LOCAL`-Modus versucht Java, automatisch den Browser zu öffnen.

### Schritt 2: Frontend Dev-Server starten (VS Code)
Öffne den Unterordner `frontend` in VS Code und starte das Terminal:
```bash
cd frontend
npm run dev
```
* Frontend läuft auf: `http://localhost:3000`
* **Wichtig:** Arbeite im Browser *nur* auf Port `3000`. Der Vite-Proxy leitet alle `/api/*` Anfragen im Hintergrund automatisch an Port `8080` weiter.

---

## ⚙️ Code-Generierung (Java DTOs ➔ TypeScript)

Wenn du Java-Klassen im Package `tws.vivien.dto` erstellst, änderst oder Felder umbenennst, musst du die TypeScript-Interfaces neu generieren.

Führe diesen Befehl im **Hauptverzeichnis** aus (wo die `pom.xml` liegt):

```bash
mvn process-classes
```
* **Was passiert?** Das Maven-Plugin scannt deine DTOs und überschreibt die Datei `frontend/src/types/vivien-generated.d.ts` vollautomatisch.
* *Tipp:* Wenn du das Backend in IntelliJ startest, führt die IDE das `compile` meistens eh aus, wodurch die TS-Typen oft schon aktuell sind.

---

## 📦 Release / Fertiges Tool bauen

Wenn das Tool fertig ist und du eine einzige, ausführbare `.jar` weitergeben willst:

1. **Frontend kompilieren:**
   ```bash
   cd frontend
   npm run build
   ```
   *(Kopiere den Inhalt des resultierenden `dist/`-Ordners manuell nach `src/main/resources/public/` – das automatisieren wir später per Maven).*

2. **Fat-JAR in Maven bauen:**
   Im Hauptverzeichnis:
   ```bash
   mvn clean package
   ```
   Deine fertige App liegt nun unter `target/vivien-asset-bridge-1.0-SNAPSHOT.jar` und kann per Doppelklick oder `java -jar App.jar` gestartet werden.
