package tws.vivien.api;

import io.javalin.http.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tws.vivien.core.Config;
import tws.vivien.core.Repository;
import tws.vivien.dto.ServerError;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Diese Api ist nur dazu da den Stash zu pushen.
 * @see tws.vivien.api.UnstashApi
 */

@Singleton
public class StashApi implements Api
{
	private static final Logger LOG = LoggerFactory.getLogger(StashApi.class);

	@Inject	public Config config;
	@Inject public Repository repository;
	@Inject public ReentrantReadWriteLock gitLock;
	@Inject public GitStatusApi gitStatusApi;

	@Inject public StashApi() {}

	@Override
	public void handle(Context ctx)
	{
		try
		{
			gitLock.writeLock().lock();
			repository.stash();
			gitStatusApi.handle(ctx);
		}
		catch (Exception e)
		{
			LOG.error("stash", e);
			ctx.status(500);
			ctx.json(ServerError.fromError(e));
		}
		finally
		{
			gitLock.writeLock().unlock();
		}
	}
}
