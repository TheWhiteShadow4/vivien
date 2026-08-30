package tws.vivien.handlers;

import tws.vivien.core.Cache;
import tws.vivien.core.Repository;
import tws.vivien.dto.FileObject;

import java.nio.file.Files;
import java.nio.file.Path;

public class TextHandler implements IHandler
{
	@Override
	public FileObject generatePreview(Path webRoot, Repository repository, Cache _cache, String file) throws Exception
	{
		Path path = repository.resolve(file);

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
