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
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class ImageHandler implements IHandler
{
	@Override
	public FileObject generatePreview(Path webRoot, Repository repository, Cache cache, String file) throws Exception
	{
		Path path = repository.resolveFile(file);

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

			Path outputPath;
			FileObject.FileObjectMeta meta = new FileObject.FileObjectMeta();
			meta.size = fileBytes.length;

			if (path.toString().endsWith(".gif"))
			{
				throw new UnsupportedOperationException("Gif is not supported");
				//outputPath = createGifCacheEntry(hash, new ByteArrayInputStream(fileBytes), meta);
			}
			else
			{
				outputPath = createDefaultCacheEntry(webRoot, hash, new ByteArrayInputStream(fileBytes), meta);
			}

			cache.add(outputPath, hash, meta);

			return new FileObject(pathToUrl(webRoot, outputPath), path.getFileName().toString(), meta);
		}
	}

	private Path createDefaultCacheEntry(Path webRoot, String hash, InputStream inputStream, FileObject.FileObjectMeta meta) throws IOException
	{
		String mimeType = "image/jpeg";

		Path outputPath = webRoot.resolve("cache/" + hash + ".jpg").toAbsolutePath();
		Files.createDirectories(outputPath.getParent());

		BufferedImage image = ImageIO.read(inputStream);
		int width = image.getWidth();
		int height = image.getHeight();

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
		if (!ImageIO.write(desBufferedImage, "jpg", outputPath.toFile()))
		{
			throw new IOException("File format not supported");
		}

		meta.mimeType = mimeType;
		meta.width = w;
		meta.height = h;
		meta.srcWidth = width;
		meta.srcHeight = height;

		return outputPath;
	}

	/*private Path createGifCacheEntry(String hash, InputStream inputStream, FileObject.FileObjectMeta meta) throws IOException
	{
		String mimeType = "image/gif";

		Path outputPath = Path.of("public/cache/" + hash + ".gif").toAbsolutePath();
		Files.createDirectories(outputPath.getParent());

		GifDecoder decoder = new GifDecoder();
		decoder.read(inputStream);
		var size = decoder.getFrameSize();

		float f = Math.max(size.width, size.height) / 512f;
		int w = Math.round(size.width / f);
		int h = Math.round(size.height / f);

		AnimatedGifEncoder encoder = new AnimatedGifEncoder();
		encoder.start(outputPath.toString());
		//encoder.setRepeat(decoder.getLoopCount());

		int frameCount = decoder.getFrameCount();

		for (int i = 0; i < frameCount; i++) {
			BufferedImage originalFrame = decoder.getFrame(i);

			// Einen neuen, leeren Frame in der Zielgröße erstellen
			BufferedImage resizedFrame = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);

			// Per Graphics2D den originalen Frame hoch- oder herunterskalieren
			Graphics2D g2d = resizedFrame.createGraphics();

			// Wichtig für gute Bildqualität beim Skalieren (Bicubic Interpolation)
			g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
			g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

			// Frame zeichnen
			g2d.drawImage(originalFrame, 0, 0, w, h, null);
			g2d.dispose();

			encoder.setDelay(decoder.getDelay(i));
			encoder.setRepeat(decoder.getLoopCount());
			encoder.addFrame(resizedFrame);
		}
		encoder.finish();

		meta.mimeType = mimeType;
		meta.width = w;
		meta.height = h;

		return outputPath;
	}*/

	private String pathToUrl(Path webRoot, Path path)
	{
		Path relativePath = webRoot.relativize(path);
		return "/" + relativePath.toString().replace("\\", "/");
	}
}
