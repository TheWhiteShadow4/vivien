package tws.vivien.api;

import io.javalin.http.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tws.vivien.core.Config;
import tws.vivien.core.ErrorBacklog;
import tws.vivien.core.Repository;
import tws.vivien.dto.ServerError;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Singleton
public class PullApi implements Api
{
	private static final Logger LOG = LoggerFactory.getLogger(PullApi.class);

	@Inject public Config config;
	@Inject public Repository repository;
	@Inject public ReentrantReadWriteLock gitLock;
	@Inject	public ErrorBacklog errorBacklog;
	@Inject public GitStatusApi gitStatusApi;

	@Inject public PullApi() {}

	@Override
	public void handle(Context ctx)
	{
		try
		{
			gitLock.writeLock().lock();
			if (config.gitRemote != null)
			{
				repository.fetch(config);
				repository.pull(config);
				gitStatusApi.handle(ctx);
			}
			else
			{
				ctx.json(repository.getCachedStatus());
			}
		}
		catch (Exception e)
		{
			LOG.error("pull", e);
			ctx.status(500);
			ctx.json(ServerError.fromError(e));
		}
		finally
		{
			gitLock.writeLock().unlock();
		}
	}
}