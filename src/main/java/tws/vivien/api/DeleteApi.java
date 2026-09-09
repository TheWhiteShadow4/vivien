package tws.vivien.api;

import io.javalin.http.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tws.vivien.core.Config;
import tws.vivien.core.ErrorBacklog;
import tws.vivien.core.Repository;
import tws.vivien.dto.GitStageRequest;
import tws.vivien.dto.ServerError;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Singleton
public class DeleteApi implements Api
{
	private static final Logger LOG = LoggerFactory.getLogger(DeleteApi.class);

	@Inject
	public Config config;
	@Inject public Repository repository;
	@Inject public ReentrantReadWriteLock gitLock;
	@Inject public ErrorBacklog errorBacklog;

	@Inject public DeleteApi() {}

	@Override
	public void handle(Context ctx)
	{
		try
		{
			gitLock.readLock().lock();
			var request = ctx.bodyAsClass(GitStageRequest.class);
			Path file = repository.resolve(request.file);
			LOG.info("Delete: {}", file);
			Files.deleteIfExists(file);
		}
		catch (Exception e)
		{
			LOG.error("delete", e);
			ctx.status(500);
			ctx.json(ServerError.fromError(e));
		}
		finally
		{
			gitLock.readLock().unlock();
		}
	}
}