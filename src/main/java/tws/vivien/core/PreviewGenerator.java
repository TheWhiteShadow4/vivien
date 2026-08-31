package tws.vivien.core;

import org.eclipse.jgit.dircache.DirCache;
import org.eclipse.jgit.dircache.DirCacheEntry;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectInserter;
import tws.vivien.dto.FileObject;
import tws.vivien.handlers.IHandler;
import tws.vivien.handlers.ImageHandler;
import tws.vivien.handlers.TextHandler;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class PreviewGenerator
{
	private final Path webRoot;
	private final Repository repository;
	private final Cache cache;

	private static final Map<String, Class<? extends IHandler>> handler;

	static
	{
		handler = new HashMap<>();
		handler.put("png", ImageHandler.class);
		handler.put("jpg", ImageHandler.class);
		handler.put("tga", ImageHandler.class);
		handler.put("txt", TextHandler.class);
		handler.put("md", TextHandler.class);
		handler.put("json", TextHandler.class);
		handler.put("yaml", TextHandler.class);
	}

	public PreviewGenerator(Path webRoot, Repository repository, Cache cache)
	{
		this.webRoot = webRoot;
		this.repository = repository;
		this.cache = cache;
	}

	public static IHandler forFile(String file)
	{
		String fileExt = file.substring(file.lastIndexOf(".")+1).toLowerCase();
		try
		{
			var fileHandler = handler.get(fileExt);
			if (fileHandler != null)
			{
				return fileHandler.getConstructor().newInstance();
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return null;
	}

	public boolean isSupported(String file)
	{
		String fileExt = file.substring(file.length() - 3);
		return handler.containsKey(fileExt);
	}

	/*public void sendPreview(Path path, String hash, Context ctx) throws IOException
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
			//fileObject = generatePreviewImage(path, hash);
		}
		ctx.json(fileObject);
	}*/

	public FileObject generatePreviewImage(String file) throws Exception
	{
		Path path = repository.resolve(file);
		//GitStatus status = repository.getStatus(path);
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
			return new FileObject(pathToUrl(cacheEntry.path), path.getFileName().toString(), meta);
		}
		else
		{
			if (fileBytes == null) fileBytes = Files.readAllBytes(path);
			BufferedImage image = ImageIO.read(new ByteArrayInputStream(fileBytes));
			int width = image.getWidth();
			int height = image.getHeight();
			String mimeType = "image/jpeg";

			Path outputPath = Path.of("public/cache/" + hash + ".jpg").toAbsolutePath();
			Files.createDirectories(outputPath.getParent());

			float f = Math.max(width, height) / 512f;
			int w = Math.round(width / f);
			int h = Math.round(height / f);
			BufferedImage desBufferedImage = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
			Graphics2D g2 = desBufferedImage.createGraphics();

			if (width > 512 || height > 512)
				g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
			else
				g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
			g2.drawImage(image, 0, 0, w, h, null);
			g2.dispose();
			ImageIO.write(desBufferedImage, "jpg", outputPath.toFile());

			var meta = new FileObject.FileObjectMeta();
			meta.mimeType = mimeType;
			meta.size = fileBytes.length;
			meta.width = w;
			meta.height = h;
			meta.srcWidth = width;
			meta.srcHeight = height;
			cache.add(outputPath, hash, meta);

			return new FileObject(pathToUrl(outputPath), path.getFileName().toString(), meta);
		}
	}

	private String pathToUrl(Path path)
	{
		Path relativePath = webRoot.relativize(path);
		return "/" + relativePath.toString().replace("\\", "/");
	}
}
