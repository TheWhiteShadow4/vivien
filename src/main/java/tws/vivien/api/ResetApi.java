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

@Singleton
public class ResetApi implements Api
{
	private static final Logger LOG = LoggerFactory.getLogger(ResetApi.class);

	@Inject public Config config;
	@Inject public Repository repository;
	@Inject public ReentrantReadWriteLock gitLock;
	@Inject public GitStatusApi gitStatusApi;

	@Inject public ResetApi() {}

	@Override
	public void handle(Context ctx)
	{
		try
		{
			gitLock.writeLock().lock();
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
			gitLock.writeLock().unlock();
		}
	}
}