package tws.vivien.handlers;

import tws.vivien.core.Repository;
import tws.vivien.dto.FileObject;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.Path;

@Singleton
public class TextHandler implements IHandler
{
	@Inject public Repository repository;

	@Inject public TextHandler() {}

	@Override
	public FileObject generatePreview(String file) throws Exception
	{
		Path path = repository.resolveFile(file);
		if (path == null) throw new FileNotFoundException();

		String content = Files.readString(path);

		String mimeType;
		if (file.toLowerCase().endsWith("md"))
		{
			mimeType = "text/markdown";
		}
		else
		{
			mimeType = "text/plain";
		}

		var meta = new FileObject.FileObjectMeta();
		meta.mimeType = mimeType;
		meta.size = content.length();

		return new FileObject(content, path.getFileName().toString(), meta);
	}
}
