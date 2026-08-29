package tws.vivien.core;

import tws.vivien.dto.ElementType;
import tws.vivien.dto.RepositoryElement;

import java.util.List;

public class RepositorySearchService
{
	private final RepositoryCache cacheManager;

	public RepositorySearchService(RepositoryCache cacheManager)
	{
		this.cacheManager = cacheManager;
	}

	public List<RepositoryElement> searchFiles(String query)
	{
		String lowerQuery = query.toLowerCase();

		// Nutzt den performanten RAM-Lookup ohne Festplatten-I/O
		return cacheManager.getPathLookup().values().stream()
				.filter(element -> element.type == ElementType.FILE) // Nur Dateien durchsuchen
				.filter(element -> element.name.toLowerCase().contains(lowerQuery))
				.map(RepositoryElement::flatCopy)
				.toList();
	}
}
