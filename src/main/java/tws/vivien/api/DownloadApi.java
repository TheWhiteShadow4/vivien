package tws.vivien.api;

import io.javalin.http.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tws.vivien.core.ErrorBacklog;
import tws.vivien.core.Repository;
import tws.vivien.dto.ServerError;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Singleton
public class DownloadApi implements Api
{
	private static final Logger LOG = LoggerFactory.getLogger(DownloadApi.class);

	@Inject public Repository repository;
	@Inject public ReentrantReadWriteLock gitLock;
	@Inject public ErrorBacklog errorBacklog;

	@Inject public DownloadApi() {}

	@Override
	public void handle(Context ctx)
	{
		try
		{
			String file = ctx.queryParam("file");
			Path path = repository.resolveFile(file);
			if (path == null)
			{
				ctx.status(404);
				return;
			}

			gitLock.readLock().lock();
			ctx.header("Content-Disposition", "attachment; filename=\"" + path.getFileName().toString() + "\"");
			ctx.contentType(Files.probeContentType(path));

			// Datei als Stream an die Antwort übergeben
			ctx.result(Files.newInputStream(path));
		}
		catch (Exception e)
		{
			LOG.error("Fehler bei Request", e);
			ctx.status(500);
			ctx.json(ServerError.fromError(e));
		}
		finally
		{
			gitLock.readLock().unlock();
		}
	}
}