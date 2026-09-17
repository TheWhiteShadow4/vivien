package tws.vivien.handlers;

import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.dircache.DirCache;
import org.eclipse.jgit.dircache.DirCacheEntry;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectInserter;
import tws.vivien.core.Cache;
import tws.vivien.core.Config;
import tws.vivien.core.Repository;
import tws.vivien.dto.FileObject;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.Path;

@Singleton
public class ModelHandler implements IHandler
{
	@Inject public Config config;
	@Inject public Repository repository;
	@Inject public Cache cache;

	@Inject public ModelHandler() {}

	@Override
	public FileObject generatePreview(String file) throws Exception
	{
		Path path = repository.resolveFile(file);
		if (path == null) throw new FileNotFoundException();

		var ext = file.substring(file.lastIndexOf('.')+1);
		var gitRepo = repository.getApi().getRepository();
		String hash = null;
		byte[] fileBytes = null;

		DirCache index = gitRepo.readDirCache();
		DirCacheEntry entry = index.getEntry(path.toString());
		if (entry != null)
		{
			hash = entry.getObjectId().name();
		}
		if (hash == null)
		{
			try(ObjectInserter inserter = gitRepo.newObjectInserter())
			{
				fileBytes = Files.readAllBytes(path);

				// Berechnet den Hash genau wie Git es intern tut, ohne die Datei im Repo zu speichern
				hash = inserter.idFor(Constants.OBJ_BLOB, fileBytes).name();
			}
		}

		var cacheEntry = cache.get(hash);
		if (cacheEntry != null)
		{
			var meta = (FileObject.FileObjectMeta) cacheEntry.metadata;
			return new FileObject(pathToUrl(config.webRoot, cacheEntry.path), path.getFileName().toString(), meta);
		}
		else
		{
			if (fileBytes == null) fileBytes = Files.readAllBytes(path);

			FileObject.FileObjectMeta meta = new FileObject.FileObjectMeta();
			meta.size = fileBytes.length;
			meta.mimeType = "application/" + ext;

			Path outputPath = config.webRoot.resolve("cache/" + hash + "." + ext).toAbsolutePath();
			Files.createDirectories(outputPath.getParent());
			FileUtils.writeByteArrayToFile(outputPath.toFile(), fileBytes);
			cache.add(outputPath, hash, meta);

			return new FileObject(pathToUrl(config.webRoot, outputPath), path.getFileName().toString(), meta);
		}
	}

	private String pathToUrl(Path webRoot, Path path)
	{
		Path relativePath = webRoot.relativize(path);
		return "/" + relativePath.toString().replace("\\", "/");
	}
}
