package tws.vivien.api;

import io.javalin.http.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tws.vivien.core.Config;
import tws.vivien.core.LockService;
import tws.vivien.core.Repository;
import tws.vivien.dto.ServerError;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Diese Api ist nur dazu da den Stash zu poppen.
 * @see tws.vivien.api.StashApi
 */

@Singleton
public class UnstashApi implements Api
{
	private static final Logger LOG = LoggerFactory.getLogger(UnstashApi.class);

	@Inject public Config config;
	@Inject	public Repository repository;
	@Inject public LockService lockService;
	@Inject	public GitStatusApi gitStatusApi;

	@Inject
	public UnstashApi()  {}

	@Override
	public void handle(Context ctx)
	{
		try
		{
			lockService.gitLock.writeLock().lock();
			repository.unstash();
			gitStatusApi.handle(ctx);
		}
		catch (Exception e)
		{
			LOG.error("unstash", e);
			ctx.status(500);
			ctx.json(ServerError.fromError(e));
		}
		finally
		{
			lockService.gitLock.writeLock().unlock();
		}
	}
}