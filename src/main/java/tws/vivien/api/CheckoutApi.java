package tws.vivien.api;

import io.javalin.http.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tws.vivien.core.Config;
import tws.vivien.core.Repository;
import tws.vivien.dto.CheckoutRequest;
import tws.vivien.dto.ServerError;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Singleton
public class CheckoutApi implements Api
{
	private static final Logger LOG = LoggerFactory.getLogger(CheckoutApi.class);

	@Inject public Config config;
	@Inject public Repository repository;
	@Inject public ReentrantReadWriteLock gitLock;
	@Inject public GitStatusApi gitStatusApi;

	@Inject public CheckoutApi() {}

	@Override
	public void handle(Context ctx)
	{
		try
		{
			var request = ctx.bodyAsClass(CheckoutRequest.class);

			if (request.branch == null) throw new NullPointerException("branch ist null");

			gitLock.writeLock().lock();
			repository.checkout(request.branch);

			gitStatusApi.handle(ctx);
		}
		catch (Exception e)
		{
			LOG.error("checkout", e);
			ctx.status(500);
			ctx.json(ServerError.fromError(e));
		}
		finally
		{
			gitLock.writeLock().unlock();
		}
	}
}