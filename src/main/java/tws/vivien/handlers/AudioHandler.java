package tws.vivien.handlers;

import org.apache.commons.io.FilenameUtils;
import tws.vivien.core.Cache;
import tws.vivien.core.Config;
import tws.vivien.core.Repository;
import tws.vivien.dto.FileObject;

import javax.inject.Inject;
import java.io.FileNotFoundException;
import java.nio.file.Path;

public class AudioHandler implements IHandler
{
	@Inject public Config config;
	@Inject public Repository repository;
	@Inject public Cache cache;

	@Inject public AudioHandler() {}

	@Override
	public FileObject generatePreview(String file) throws Exception
	{
		Path path = repository.resolveFile(file);
		if (path == null) throw new FileNotFoundException();

		var extension = FilenameUtils.getExtension(path.toString());
		String mimeType = switch (extension)
		{
			case "wav" -> "audio/wav";
			case "ogg" -> "audio/ogg";
			case "aac" -> "audio/aac";
			case "mp3" -> "audio/mpeg"; // Wichtig: mpeg, nicht mp3!
			default -> throw new IllegalArgumentException("Ungültige Datei");
		};

		var meta = new FileObject.FileObjectMeta();
		meta.mimeType = mimeType;

		return new FileObject(repository.getUrl(path), path.getFileName().toString(), meta);
	}
}
