package tws.vivien.api;

import io.javalin.http.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tws.vivien.core.Config;
import tws.vivien.core.Repository;
import tws.vivien.dto.ElementType;
import tws.vivien.dto.RepositoryElement;
import tws.vivien.dto.ServerError;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Singleton
public class CreateApi implements Api
{
	private static final Logger LOG = LoggerFactory.getLogger(CreateApi.class);

	@Inject
	public Config config;
	@Inject public Repository repository;
	@Inject public ReentrantReadWriteLock gitLock;

	@Inject public CreateApi() {}

	@Override
	public void handle(Context ctx)
	{
		try
		{
			gitLock.readLock().lock();
			var request = ctx.bodyAsClass(RepositoryElement.class);
			Path path = repository.resolve(request.path);
			if (!Files.isDirectory(path)) throw new FileNotFoundException();

			Path newElement = path.resolve(request.name);
			LOG.info("Create: {}", newElement);

			if (request.type == ElementType.FOLDER)
			{
				Files.createDirectory(newElement);
			}
			else
			{
				Files.createFile(newElement);
			}
		}
		catch (Exception e)
		{
			LOG.error("Request fehlgeschlagen", e);
			ctx.status(500);
			ctx.json(ServerError.fromError(e));
		}
		finally
		{
			gitLock.readLock().unlock();
		}
	}
}
