package tws.vivien.handlers;

import org.eclipse.jgit.dircache.DirCache;
import org.eclipse.jgit.dircache.DirCacheEntry;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectInserter;
import tws.vivien.core.Cache;
import tws.vivien.core.Repository;
import tws.vivien.dto.FileObject;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class ImageHandler implements IHandler
{
	@Override
	public FileObject generatePreview(Path webRoot, Repository repository, Cache cache, String file) throws Exception
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
			return new FileObject(pathToUrl(webRoot, cacheEntry.path), path.getFileName().toString(), meta);
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

			return new FileObject(pathToUrl(webRoot, outputPath), path.getFileName().toString(), meta);
		}
	}

	private String pathToUrl(Path webRoot, Path path)
	{
		Path relativePath = webRoot.relativize(path);
		return "/" + relativePath.toString().replace("\\", "/");
	}
}
