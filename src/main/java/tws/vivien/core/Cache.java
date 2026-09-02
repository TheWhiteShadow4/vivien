package tws.vivien.core;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

public class Cache
{
	private final ConcurrentHashMap<String, CacheEntry> entries = new ConcurrentHashMap<>();
	private final Path cacheFolder;

	public Path getCacheFolder() { return cacheFolder; }

	public Cache(String path) throws IOException
	{
		cacheFolder = Path.of(path).toAbsolutePath();
		IO.println("Cache: " + cacheFolder);
		Files.createDirectories(cacheFolder);
	}

	public CacheEntry get(String hash)
	{
		CacheEntry entry = entries.get(hash);
		if (entry != null)
		{
			entry.lastAccess = LocalDate.now();
		}
		return entry;
	}

	public void add(Path path, String hash)
	{
		add(path, hash, null);
	}

	public void add(Path path, String hash, Object metadata)
	{
		if (entries.size() > 100)
		{
			Cleanup(Duration.ofMinutes(10));
		}
		entries.put(hash, new CacheEntry(path, metadata));
	}

	public void Cleanup(Duration maxAccessTime)
	{
		var deadline = LocalDate.now().minus(maxAccessTime);
		var cleanupList = new ArrayList<Path>(100);

		var iter = entries.keySet().iterator();
		while (iter.hasNext())
		{
			var key = iter.next();
			var entry = entries.get(key);
			if (entry.lastAccess.isBefore(deadline))
			{
				cleanupList.add(entry.path);
				iter.remove();
			}
		}
		if (!cleanupList.isEmpty())
		{
			Thread.ofVirtual().factory().newThread(() ->
			{
			   for(Path path : cleanupList)
			   {
				   try
				   {
					   Files.deleteIfExists(path);
				   }
				   catch (IOException _) {}
			   }
			});
		}
	}

	public static class CacheEntry
	{
		public Path path;
		public Object metadata;
		public LocalDate lastAccess;

		public CacheEntry(Path path, Object metadata)
		{
			this.path = path;
			this.metadata = metadata;
			this.lastAccess = LocalDate.now();
		}
	}
}
