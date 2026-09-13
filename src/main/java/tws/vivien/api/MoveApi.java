package tws.vivien.api;

import io.javalin.http.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tws.vivien.core.Config;
import tws.vivien.core.Repository;
import tws.vivien.dto.MoveRequest;
import tws.vivien.dto.ServerError;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Singleton
public class MoveApi implements Api
{
	private static final Logger LOG = LoggerFactory.getLogger(MoveApi.class);

	@Inject public Config config;
	@Inject public Repository repository;
	@Inject public ReentrantReadWriteLock gitLock;
	@Inject public GitStatusApi gitStatusApi;

	@Inject public MoveApi() {}

	@Override
	public void handle(Context ctx)
	{
		try
		{
			gitLock.readLock().lock();
			var request = ctx.bodyAsClass(MoveRequest.class);

			LOG.info("Move: {} => {}", request.src, request.dst);
			repository.move(request.src, request.dst);
			gitStatusApi.handle(ctx);
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
