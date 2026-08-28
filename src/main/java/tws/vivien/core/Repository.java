package tws.vivien.core;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.treewalk.FileTreeIterator;
import org.eclipse.jgit.treewalk.TreeWalk;
import tws.vivien.dto.ElementType;
import tws.vivien.dto.RepositoryElement;
import tws.vivien.dto.RepositoryView;

import java.io.Closeable;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Repository implements Closeable
{
	private final Path rootPath;
	private final org.eclipse.jgit.lib.Repository jgitRepo;

	public Repository(Path rootPath) throws IOException
	{
		this.rootPath = rootPath;
		this.jgitRepo = Git.open(rootPath.toFile()).getRepository();
	}

	public RepositoryView getView(ConfigView view)
	{
		RepositoryView repoView = new RepositoryView();
		repoView.children = createElements(this.rootPath, view.getFilter());
		return repoView;
	}


	private List<RepositoryElement> createElements(Path currentPath, ConfigView.ViewFilter filter)
	{
		if (!Files.exists(currentPath) || !Files.isDirectory(currentPath))
		{
			return new ArrayList<>();
		}

		try (Stream<Path> stream = Files.list(currentPath))
		{
			return stream
					.filter(filter::isIncluded)
					.filter(path -> !isIgnoredByGit(path))
					.map(s -> mapToElement(s, filter))
					.collect(Collectors.toList());
		}
		catch (IOException e)
		{
			System.err.println("Fehler beim Lesen des Pfads: " + currentPath + " - " + e.getMessage());
			return new ArrayList<>();
		}
	}

	private boolean isIgnoredByGit(Path path)
	{
		// Berechne den relativen Pfad zum Repository-Root (z.B. "src/main.js")
		String relativePath = rootPath.relativize(path).toString().replace("\\", "/");

		// TreeWalk ist der JGit-Standardweg, um Pfade gegen .gitignore-Regeln zu matchen
		try (TreeWalk treeWalk = new TreeWalk(jgitRepo)) {
			// Wir hängen einen FileTreeIterator an, der das Arbeitsverzeichnis simuliert
			treeWalk.addTree(new FileTreeIterator(jgitRepo));
			treeWalk.setRecursive(false); // Wir prüfen Ebene für Ebene

			// Laufe durch das Git-Arbeitsverzeichnis, bis wir den gesuchten Pfad finden
			while (treeWalk.next()) {
				if (treeWalk.getPathString().equals(relativePath)) {
					// Hole den internen WorkingTreeIterator für das aktuelle Element
					FileTreeIterator fti = treeWalk.getTree(0, FileTreeIterator.class);
					// isEntryIgnored() ohne Parameter prüft das aktuell fokussierte Element
					return fti != null && fti.isEntryIgnored();
				}

				// Falls wir in einen Überordner gelaufen sind, betreten wir ihn im TreeWalk
				if (treeWalk.isSubtree() && relativePath.startsWith(treeWalk.getPathString() + "/")) {
					treeWalk.enterSubtree();
				}
			}
		} catch (IOException e) {
			// Im Fehlerfall vorsichtshalber nicht ignorieren
			System.err.println("Fehler bei der Gitignore-Prüfung für " + relativePath + ": " + e.getMessage());
		}
		return false;
	}

	private RepositoryElement mapToElement(Path path, ConfigView.ViewFilter filter)
	{
		RepositoryElement element = new RepositoryElement();
		element.name = path.getFileName().toString();
		element.path = rootPath.relativize(path).toString();

		if (Files.isDirectory(path)) {
			element.type = ElementType.FOLDER;
			// Rekursion für die nächste Ebene
			element.children = createElements(path, filter);
		} else {
			element.type = ElementType.FILE;
			element.children = new ArrayList<>(); // Der wichtige Schutz gegen null
		}

		return element;
	}

	@Override
	public void close() throws IOException
	{
		jgitRepo.close();
	}

	public Path resolve(String file)
	{
		Path path = rootPath.resolve(Path.of(file));
		if (Files.isRegularFile(path))
			return path;
		else
			return null;
	}
}
