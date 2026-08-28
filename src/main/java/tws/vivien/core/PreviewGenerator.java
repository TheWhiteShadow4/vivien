package tws.vivien.core;

import io.javalin.http.Context;
import tws.vivien.dto.FileObject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;

public class PreviewGenerator
{
	private Cache cache;

	public PreviewGenerator(Cache cache)
	{
		this.cache = cache;
	}

	public boolean isSupported(String file)
	{
		String fileExt = file.substring(file.length() - 3);
		return fileExt.equals("png");
	}

	public void sendPreview(Path path, String hash, Context ctx) throws IOException
	{
		var entry = cache.get(hash);
		FileObject fileObject;
		if (entry != null)
		{
			fileObject = new FileObject(pathToUrl(path), hash, (String) entry.metadata.get("mimeType"));
			fileObject.width = (Integer) entry.metadata.get("width");
			fileObject.height = (Integer) entry.metadata.get("height");
		}
		else
		{
			fileObject = generatePreviewImage(path, hash);
		}
		ctx.json(fileObject);
	}

	private FileObject generatePreviewImage(Path path, String hash) throws IOException
	{
		BasicFileAttributes attr = Files.readAttributes(path, BasicFileAttributes.class);
		if (attr.size() > 4096)
		{

		}
		else
		{

		}
		return null;
	}

	private String pathToUrl(Path path)
	{
		return path.toString();
	}
}
