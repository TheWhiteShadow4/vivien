package tws.vivien.core;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.treewalk.FileTreeIterator;
import org.eclipse.jgit.treewalk.TreeWalk;
import tws.vivien.dto.ElementType;
import tws.vivien.dto.GitBranchStatus;
import tws.vivien.dto.GitFileStatus;
import tws.vivien.dto.RepositoryElement;

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
	private final Git gitApi;
	private final RepositoryCache cache;

	public Repository(Path rootPath) throws IOException, GitAPIException
	{
		this.rootPath = rootPath;
		this.gitApi = Git.open(rootPath.toFile());
		cache = new RepositoryCache(rootPath, gitApi.getRepository());
		IO.println(cache.toString());
	}

	public Git getApi()
	{
		return gitApi;
	}

	public RepositoryElement getView(ConfigView view, String path) throws IOException
	{
		return cache.getDirectory(path).flatCopyWithChildren();
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
					.map(s -> mapToElement(s))
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
		try (TreeWalk treeWalk = new TreeWalk(gitApi.getRepository()))
		{
			// Wir hängen einen FileTreeIterator an, der das Arbeitsverzeichnis simuliert
			treeWalk.addTree(new FileTreeIterator(gitApi.getRepository()));
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

	private RepositoryElement mapToElement(Path path)
	{
		RepositoryElement element = new RepositoryElement();
		element.name = path.getFileName().toString();
		element.path = rootPath.relativize(path).toString();

		if (Files.isDirectory(path)) {
			element.type = ElementType.FOLDER;
			// Rekursion für die nächste Ebene
			element.children = null;//createElements(path, filter);
		} else {
			element.type = ElementType.FILE;
			element.children = new ArrayList<>(); // Der wichtige Schutz gegen null
		}

		return element;
	}

	public List<RepositoryElement> searchFiles(String query)
	{
		String lowerQuery = query.toLowerCase();

		// Nutzt den performanten RAM-Lookup ohne Festplatten-I/O
		return cache.getPathLookup().values().stream()
				.filter(element -> element.type == ElementType.FILE) // Nur Dateien durchsuchen
				.filter(element -> element.name.toLowerCase().contains(lowerQuery))
				.map(RepositoryElement::flatCopy)
				.toList();
	}

	public GitBranchStatus getBranchStatus() throws Exception
	{
		Status status = gitApi.status().call();

		var result = new GitBranchStatus();
		result.branch = gitApi.getRepository().getBranch();
		result.modified = status.hasUncommittedChanges();
		result.changed = status.getUncommittedChanges();
		result.added = status.getAdded();
		result.missing = status.getMissing();
		result.conflicts = status.getConflicting();
		return result;
	}

	public GitFileStatus getStatus(Path path) throws Exception
	{
		Status status = gitApi.status().addPath(path.toString()).call();
		if (!status.isClean())
			return GitFileStatus.Clean;
		if (!status.getUntracked().isEmpty())
			return GitFileStatus.Untracked;
		if (!status.getAdded().isEmpty())
			return GitFileStatus.Added;
		if (!status.getChanged().isEmpty())
			return GitFileStatus.Modified;
		if (!status.getConflicting().isEmpty())
			return GitFileStatus.Conflict;
		if (!status.getMissing().isEmpty())
			return GitFileStatus.Deleted;
		throw new Error("Unbekannter Git State");
	}

	@Override
	public void close()
	{
		gitApi.close();
	}

	public Path resolve(String file)
	{
		if (file.startsWith("/")) file = file.substring(1);
		Path path = rootPath.resolve(Path.of(file));
		if (Files.isRegularFile(path))
			return path;
		else
			return null;
	}
}
