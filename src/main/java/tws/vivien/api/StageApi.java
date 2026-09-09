package tws.vivien.api;

import io.javalin.http.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tws.vivien.core.Config;
import tws.vivien.core.ErrorBacklog;
import tws.vivien.core.Repository;
import tws.vivien.dto.GitStageOperation;
import tws.vivien.dto.GitStageRequest;
import tws.vivien.dto.ServerError;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.nio.file.Path;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Singleton
public class StageApi implements Api
{
	private static final Logger LOG = LoggerFactory.getLogger(StageApi.class);

	@Inject
	public Config config;
	@Inject public Repository repository;
	@Inject public ReentrantReadWriteLock gitLock;
	@Inject public ErrorBacklog errorBacklog;
	@Inject public GitStatusApi gitStatusApi;

	@Inject public StageApi() {}

	@Override
	public void handle(Context ctx)
	{
		try
		{
			var request = ctx.bodyAsClass(GitStageRequest.class);

			if (request.email == null) throw new NullPointerException("email ist null");
			if (request.file == null) throw new NullPointerException("file ist null");

			gitLock.readLock().lock();
			Path file = repository.resolve(request.file);
			IO.println("staged " + request.op + " File: " + request.file + " => "+ file);

			switch (request.op)
			{
				case GitStageOperation.Track:
					repository.trackFile(file);
					break;
				case GitStageOperation.Untrack:
					repository.untrackFile(file);
					break;
				case GitStageOperation.Delete:
					repository.deleteFile(file);
					break;
				case GitStageOperation.Undelete:
					repository.undeleteFile(file);
					break;
			}
			gitStatusApi.handle(ctx);
		}
		catch (Exception e)
		{
			LOG.error("staged", e);
			ctx.status(500);
			ctx.json(ServerError.fromError(e));
		}
		finally
		{
			gitLock.readLock().unlock();
		}
	}
}