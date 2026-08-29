package tws.vivien.core;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.ignore.IgnoreNode;
import tws.vivien.dto.ElementType;
import tws.vivien.dto.GitStatus;
import tws.vivien.dto.RepositoryElement;
import tws.vivien.dto.RepositoryRoot;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static java.nio.file.StandardWatchEventKinds.*;

public class RepositoryCache
{
	private final Path rootPath;
	private final IgnoreNode gitIgnore = new IgnoreNode();
	private volatile boolean isMuted = false;

	// Die Wurzel des In-Memory Baums
	private RepositoryRoot rootElement;

	// Der Turbo-Lookup für gezielte Updates: Pfad -> Element-Referenz
	private final ConcurrentHashMap<String, RepositoryElement> pathLookup = new ConcurrentHashMap<>();

	// WatchKeys zu Pfaden mappen, um zu wissen, welcher Ordner gefeuert hat
	private final ConcurrentHashMap<WatchKey, Path> watchKeys = new ConcurrentHashMap<>();
	private final WatchService watchService;

	public synchronized RepositoryRoot getRootElement() {
		return this.rootElement;
	}

	public Map<String, RepositoryElement> getPathLookup() {
		return Collections.unmodifiableMap(this.pathLookup);
	}

	public RepositoryCache(Path rootPath, org.eclipse.jgit.lib.Repository jgitRepo) throws IOException, GitAPIException
	{
		this.rootPath = rootPath.toAbsolutePath().normalize();
		this.watchService = FileSystems.getDefault().newWatchService();

		loadGitIgnore();

		// 1. Initialer vollständiger Scan beim Serverstart
		buildInitialCache();

		jgitRepo.getListenerList().addRefsChangedListener(event -> {
			triggerFullCacheRefresh();
		});

		// 2. WatchService für alle existierenden Ordner registrieren
		registerRecursive(this.rootPath);

		// 3. Hintergrund-Thread starten
		Thread watchThread = new Thread(this::listenToEvents, "GitRepo-WatchService");
		watchThread.setDaemon(true);
		watchThread.start();
	}

	private void loadGitIgnore()
	{
		File ignoreFile = new File(rootPath.toFile(), ".gitignore");
		if (ignoreFile.exists()) {
			try (FileInputStream fis = new FileInputStream(ignoreFile))
			{
				gitIgnore.parse(fis);
				System.out.println("✅ .gitignore erfolgreich geladen und für Filterung aktiv.");
			} catch (IOException e) {
				System.err.println("Fehler beim Lesen der .gitignore: " + e.getMessage());
			}
		}
	}

	private boolean isIgnored(String relativePath, boolean isDirectory)
	{
		if (relativePath.isEmpty()) return false;

		// JGit IgnoreNode Matcher aufrufen
		IgnoreNode.MatchResult result = gitIgnore.isIgnored(relativePath, isDirectory);
		return result == IgnoreNode.MatchResult.IGNORED;
	}

	/**
	 * Startet einen sicheren, isolierten Full-Refresh des Caches bei Git-Massenoperationen
	 */
	public void triggerFullCacheRefresh()
	{
		System.out.println("🔄 Rebuild Cache...");

		// 1. Watcher stummschalten, damit eintreffende OS-Events ignoriert werden
		this.isMuted = true;

		try
		{
			synchronized (this)
			{
				buildInitialCache();

				// 3. Bestehende Watcher-Keys erneuern, falls sich Ordnerstrukturen drastisch geändert haben
				// (Optionally: watchKeys leer machen und registerRecursive(rootPath) neu aufrufen)
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			// 4. WICHTIG: Alle Events, die sich WÄHREND des Einlesens angestaut haben, ungelesen verwerfen
			flushPendingEvents();

			// 5. Watcher wieder scharf schalten
			this.isMuted = false;
			System.out.println("✅ Cache-Refresh abgeschlossen. Watcher wieder aktiv.");
		}
	}

	private void flushPendingEvents()
	{
		// Wir holen uns alle aktuell signalisierten Keys ab und verwerfen deren Events
		WatchKey key;
		while ((key = watchService.poll()) != null) {
			key.pollEvents(); // Events löschen
			key.reset();      // Key wieder bereitmachen
		}
	}

	// Liefert die Wurzel für die Javalin-Route
	public synchronized RepositoryElement getRoot() {
		return this.rootElement;
	}

	// Liefert ein spezifisches Unterverzeichnis für das Lazy Loading im Client
	public RepositoryElement getDirectory(String path) throws IOException
	{
		String normalizedPath = path.replace("\\", "/").replaceAll("^/|/$", "");
		if (normalizedPath.isEmpty()) return getRoot();
		var result = pathLookup.get(normalizedPath);
		if (result == null) throw new IOException("Element für '" + path + "' nicht gefunden.");
		return result;
	}

	/**
	 * Baut den initialen In-Memory Baum auf (Ordner starten mit children = null wegen Lazy Loading)
	 */
	private void buildInitialCache() throws GitAPIException
	{
		this.pathLookup.clear();

		this.rootElement = new RepositoryRoot();
		this.rootElement.name = "";
		this.rootElement.path = "";
		this.rootElement.type = ElementType.ROOT;
		this.rootElement.gitStatus = GitStatus.Clean;

		// Wir scannen initial nur die Root-Ebene für das Lazy-Prinzip vor
		scanDirectoryFromDisk(this.rootElement);
	}

	/**
	 * Scannt ein Verzeichnis von der Festplatte und befüllt dessen Kinder
	 */
	public synchronized void scanDirectoryFromDisk(RepositoryElement parent)
	{
		File dir = new File(rootPath.toFile(), parent.path);
		File[] files = dir.listFiles();

		if (files == null) {
			parent.children = new ArrayList<>();
			return;
		}

		List<RepositoryElement> childrenList = new ArrayList<>();

		for (File file : files)
		{
			String name = file.getName();
			if (name.equals(".git")) continue;

			// Relativen Pfad für dieses Element bauen
			String childPath = parent.path.isEmpty() ? name : parent.path + "/" + name;

			// 🛑 FILTER: Ignorierte Unity-Ordner/Dateien überspringen
			if (isIgnored(childPath, file.isDirectory())) {
				continue;
			}

			RepositoryElement child = new RepositoryElement();
			child.name = file.getName();
			child.path = childPath;
			child.type = file.isDirectory() ? ElementType.FOLDER : ElementType.FILE;
			child.children = null; // null für lazy loading
			child.gitStatus = determineGitStatus(child.path); // Hier deine JGit-Status Logik nutzen

			childrenList.add(child);
			pathLookup.put(child.path, child);

			scanDirectoryFromDisk(child);
		}

		parent.children = childrenList;
	}

	/**
	 * Der unendliche Loop, der auf Betriebssystem-Events lauscht
	 */
	private void listenToEvents()
	{
		try
		{
			while (!Thread.currentThread().isInterrupted())
			{
				WatchKey key = watchService.take(); // Blockiert, bis ein Event eintrifft

				if (isMuted)
				{
					key.pollEvents(); // Events ungelesen aus der Queue holen und verwerfen
					key.reset();
					continue;
				}

				Path dirPath = watchKeys.get(key);

				if (dirPath == null) {
					key.reset();
					continue;
				}

				String relativeParentPath = rootPath.relativize(dirPath).toString().replace("\\", "/");
				RepositoryElement parentElement = relativeParentPath.isEmpty() ? rootElement : pathLookup.get(relativeParentPath);

				// Falls der Ordner im Cache noch gar nicht per Lazy-Loading geöffnet wurde,
				// müssen wir die Events nicht verarbeiten (da children eh null ist)
				if (parentElement != null && parentElement.children != null) {

					for (WatchEvent<?> event : key.pollEvents()) {
						WatchEvent.Kind<?> kind = event.kind();
						Path eventPath = (Path) event.context();
						Path fullPath = dirPath.resolve(eventPath);

						String childName = eventPath.getFileName().toString();
						if (childName.equals(".git")) continue;

						String childRelativePath = relativeParentPath.isEmpty() ? childName : relativeParentPath + "/" + childName;

						// Gezielte Updates ausführen
						synchronized (this) {
							if (kind == ENTRY_CREATE) {
								// 🛑 FILTER: Verhindert, dass zur Laufzeit erstellte Temp-Dateien im Cache landen
								if (isIgnored(childRelativePath, Files.isDirectory(fullPath))) continue;

								handleCreateEvent(parentElement, fullPath, childName, childRelativePath);
							} else if (kind == ENTRY_DELETE) {
								handleDeleteEvent(parentElement, childRelativePath);
							} else if (kind == ENTRY_MODIFY) {
								handleModifyEvent(childRelativePath);
							}
						}
					}
				}

				// Wichtig: Key zurücksetzen, um weiter auf diesem Ordner zu lauschen
				boolean valid = key.reset();
				if (!valid) {
					watchKeys.remove(key); // Ordner wurde gelöscht
				}
			}
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void handleCreateEvent(RepositoryElement parent, Path fullPath, String name, String relativePath)
	{
		// Falls das Element bereits existiert, nichts tun
		if (pathLookup.containsKey(relativePath)) return;

		boolean isDir = Files.isDirectory(fullPath);

		RepositoryElement newElement = new RepositoryElement();
		newElement.name = name;
		newElement.path = relativePath;
		newElement.type = isDir ? ElementType.FOLDER : ElementType.FILE;
		newElement.children = null;
		//newElement.parent = parent;
		newElement.gitStatus = determineGitStatus(relativePath);

		// Atomar in die Strukturen einfügen
		parent.children.add(newElement);
		pathLookup.put(relativePath, newElement);

		// Wenn ein neuer Ordner erstellt wurde, müssen wir ihn ebenfalls überwachen!
		if (isDir) {
			try {
				registerSingleDirectory(fullPath);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void handleDeleteEvent(RepositoryElement parent, String relativePath)
	{
		RepositoryElement removedElement = pathLookup.remove(relativePath);
		if (removedElement != null && parent.children != null) {
			parent.children.remove(removedElement);
			// Kaskadierend aus Lookup löschen, falls es ein Ordner mit Unterordnern war
			removeChildrenFromLookup(removedElement);
		}
	}

	private void handleModifyEvent(String relativePath) {
		RepositoryElement element = pathLookup.get(relativePath);
		if (element != null) {
			// Datei wurde geändert (z.B. Photoshop-Speicherung von Artists)
			element.gitStatus = determineGitStatus(relativePath);
			// Hier optional: Metadaten-Cache für Bilder zurücksetzen!
		}
	}

	private void removeChildrenFromLookup(RepositoryElement element) {
		if (element.children != null) {
			for (RepositoryElement child : element.children) {
				pathLookup.remove(child.path);
				removeChildrenFromLookup(child);
			}
		}
	}

	/**
	 * Registriert einen einzelnen Ordner beim OS-WatchService
	 */
	private void registerSingleDirectory(Path dir) throws IOException {
		WatchKey key = dir.register(watchService, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
		watchKeys.put(key, dir);
	}

	/**
	 * Registriert rekursiv alle Unterordner beim Start
	 */
	private void registerRecursive(Path start) throws IOException {
		Files.walk(start)
				.filter(Files::isDirectory)
				.filter(p -> !p.toString().contains(".git"))
				.forEach(p -> {
					String relativePath = rootPath.relativize(p).toString().replace("\\", "/");

					// 🛑 FILTER: Nur Ordner überwachen, die NICHT in der .gitignore stehen
					if (!isIgnored(relativePath, true)) {
						try {
							registerSingleDirectory(p);
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				});
	}

	// Dummy-Methode: Hier dockst du deine JGit Status-Prüfung an
	private GitStatus determineGitStatus(String relativePath)
	{
		return GitStatus.Clean;
	}

	@Override
	public String toString()
	{
		if (this.rootElement == null) {
			return "Cache ist leer (nicht initialisiert).";
		}
		StringBuilder sb = new StringBuilder();
		sb.append("=== REPOSITORY CACHE MAP SYSTEM ===\n");
		sb.append("Aktive Betriebssystem-Watcher (Ordner): ").append(watchKeys.size()).append("\n");
		sb.append("Gesamtelemente im RAM-Lookup-Index: ").append(pathLookup.size()).append("\n");
		sb.append("-----------------------------------\n");

		// Starte rekursive Generierung (Ebene 0)
		buildTreeString(this.rootElement, "", true, sb, 0);

		sb.append("-----------------------------------\n");
		sb.append("💡 Tipp: Tiefere Ebenen (> Ebene 3) wurden ausgeblendet, um den Terminal-Buffer zu schonen.\n");
		return sb.toString();
	}

	private void buildTreeString(RepositoryElement element, String indent, boolean isLast, StringBuilder sb, int level)
	{
		// HARTES ABSCHNEIDEN: Alles ab Ebene 3 wird nicht mehr gerendert
		if (level > 3) {
			if (isLast) {
				sb.append(indent).append("└── ... (Tiefere Assets ausgeblendet) \n");
			}
			return;
		}

		sb.append(indent);

		if (element.type == ElementType.ROOT) {
			sb.append("💻 ");
		} else {
			sb.append(isLast ? "└── " : "├── ");
		}

		String displayName = (element.name == null || element.name.isEmpty())
				? (element.type == ElementType.ROOT ? "ROOT" : "Unbekannt")
				: element.name;

		boolean isContainer = (element.type == ElementType.ROOT || element.type == ElementType.FOLDER);

		if (isContainer) {
			sb.append("📁 ").append(displayName).append("/");

			if (element.children == null) {
				sb.append(" [Lazy / Nicht geladen]");
			} else if (element.children.isEmpty()) {
				sb.append(" [Geladen / Leer]");
			} else {
				sb.append(" [Geladen: ").append(element.children.size()).append(" Elemente]");
			}
		} else {
			sb.append("📄 ").append(displayName);
		}

		if (element.gitStatus != null && element.gitStatus != GitStatus.Clean)
		{
			sb.append(" (").append(element.gitStatus).append(")");
		}
		sb.append("\n");

		// Rekursion für geladene Ordner
		if (isContainer && element.children != null) {
			String nextIndent = indent + (isLast ? "    " : "│   ");
			for (int i = 0; i < element.children.size(); i++) {
				boolean lastChild = (i == element.children.size() - 1);
				buildTreeString(element.children.get(i), nextIndent, lastChild, sb, level + 1);
			}
		}
	}
}