package tws.vivien.core;

import tws.vivien.dto.ElementType;
import tws.vivien.dto.RepositoryView;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Repository
{
	private final Path rootPath;

	public Repository(Path rootPath)
	{
		this.rootPath = rootPath;
	}

	public RepositoryView getView()
	{
		RepositoryView view = new RepositoryView();
		view.elements = createElements(this.rootPath);
		return view;
	}


	private List<RepositoryView.RepositoryElement> createElements(Path currentPath)
	{
		if (!Files.exists(currentPath) || !Files.isDirectory(currentPath))
		{
			return new ArrayList<>();
		}

		try (Stream<Path> stream = Files.list(currentPath))
		{
			return stream
					// Professioneller Filter: Versteckten .git-Ordner ignorieren
					.filter(path -> !path.getFileName().toString().equals(".git"))
					.map(this::mapToElement)
					.collect(Collectors.toList());
		}
		catch (IOException e)
		{
			// Im echten Backend: Nutze hier ein Logging-Framework (z.B. SLF4J)
			System.err.println("Fehler beim Lesen des Pfads: " + currentPath + " - " + e.getMessage());
			return new ArrayList<>();
		}
	}

	private RepositoryView.RepositoryElement mapToElement(Path path)
	{
		var element = new RepositoryView.RepositoryElement();
		// Nur den Namen der Datei/des Ordners extrahieren, nicht den absoluten Pfad
		element.name = path.getFileName().toString();

		if (Files.isDirectory(path)) {
			element.type = ElementType.FOLDER;
			// Rekursiver Aufruf für Unterordner
			element.children = createElements(path);
		} else {
			element.type = ElementType.FILE;
			element.children = new ArrayList<>();
		}

		return element;
	}
}
