package tws.vivien.handlers;

import tws.vivien.core.LockService;
import tws.vivien.core.Repository;
import tws.vivien.dto.FileObject;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.nio.file.Files;
import java.nio.file.Path;

@Singleton
public class TextHandler implements IHandler
{
	@Inject public Repository repository;
	@Inject public LockService lockService;

	@Inject public TextHandler() {}

	@Override
	public FileObject generatePreview(String file, Path path) throws Exception
	{
		String content = Files.readString(path);
		String filename = file.toLowerCase();

		String mimeType;
		if (filename.endsWith(".md"))
		{
			mimeType = "text/markdown";
		}
		else if (filename.endsWith(".yaml"))
		{
			mimeType = "text/yaml";
		}
		else if (filename.endsWith(".json"))
		{
			mimeType = "text/json";
		}
		else if (filename.endsWith(".toml"))
		{
			mimeType = "text/toml";
		}
		else
		{
			mimeType = "text/plain";
		}

		var lockHolder = lockService.fileLocks.get(file);
		var meta = new FileObject.FileObjectMeta();
		meta.mimeType = mimeType;
		meta.size = content.length();
		meta.lockHolder = lockHolder;

		return new FileObject(content, path.getFileName().toString(), meta);
	}
}
