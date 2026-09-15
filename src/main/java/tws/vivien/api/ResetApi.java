package tws.vivien.api;

import io.javalin.http.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tws.vivien.core.LockService;
import tws.vivien.core.Repository;
import tws.vivien.core.Server;
import tws.vivien.dto.ServerError;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class ResetApi implements Api
{
	private static final Logger LOG = LoggerFactory.getLogger(ResetApi.class);

	@Inject public Repository repository;
	@Inject public LockService lockService;
	@Inject public GitStatusApi gitStatusApi;

	@Inject public ResetApi() {}

	@Override
	public void handle(Context ctx)
	{
		try
		{
			lockService.gitLock.writeLock().lock();
			lockService.freeUserLocks(ctx.header(Server.APP_USER));
			repository.reset();
			gitStatusApi.handle(ctx);
		}
		catch (Exception e)
		{
			LOG.error("reset", e);
			ctx.status(500);
			ctx.json(ServerError.fromError(e));
		}
		finally
		{
			lockService.gitLock.writeLock().unlock();
		}
	}
}