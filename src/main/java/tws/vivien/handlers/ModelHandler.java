package tws.vivien.handlers;

import org.apache.commons.io.FilenameUtils;
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

		var filename = path.getFileName().toString();
		var ext = FilenameUtils.getExtension(file);

		FileObject.FileObjectMeta meta = new FileObject.FileObjectMeta();
		meta.mimeType = "application/" + ext;
		String url = repository.getUrl(path);

		if ("obj".equals(ext))
		{
			Path additionalPath = path.getParent().resolve(filename.replace("obj", "mtl"));
			if (Files.exists(additionalPath))
			{
				meta.additional = "mtl:" + repository.getUrl(additionalPath);
			}
		}

		return new FileObject(url, filename, meta);
	}
}
