package tws.vivien.core;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectInserter;
import org.eclipse.jgit.treewalk.FileTreeIterator;
import org.eclipse.jgit.treewalk.TreeWalk;
import tws.vivien.dto.ElementType;
import tws.vivien.dto.GitStatus;
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
	private RepositoryCache cache;

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
		RepositoryElement element = cache.getDirectory(path);
		if (element != null)
		{
			element = element.flatCopyWithChildren();
		}
		/*RepositoryRoot repoView = .flat();
		Path basePath = this.rootPath;
		if (path != null && !path.equals("/"))
		{
			basePath = rootPath.resolve(path);
		}
		repoView.children = createElements(basePath, view.getFilter());*/
		return element;
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

	public String getHash(Path file) throws Exception
	{
		try(ObjectInserter inserter = gitApi.getRepository().newObjectInserter())
		{
			byte[] fileBytes = Files.readAllBytes(file);

			// Berechnet den Hash genau wie Git es intern tut, ohne die Datei im Repo zu speichern
			return inserter.idFor(Constants.OBJ_BLOB, fileBytes).toString();
		}
	}

	public GitStatus getStatus(Path path) throws Exception
	{
		Status status = gitApi.status().addPath(path.toString()).call();
		if (!status.isClean())
			return GitStatus.Clean;
		if (!status.getUntracked().isEmpty())
			return GitStatus.Untracked;
		if (!status.getAdded().isEmpty())
			return GitStatus.Added;
		if (!status.getChanged().isEmpty())
			return GitStatus.Modified;
		if (!status.getConflicting().isEmpty())
			return GitStatus.Conflict;
		if (!status.getMissing().isEmpty())
			return GitStatus.Deleted;
		throw new Error("Unbekannter Git State");
	}

	@Override
	public void close()
	{
		gitApi.close();
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
