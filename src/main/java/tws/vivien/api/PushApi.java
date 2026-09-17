package tws.vivien.api;

import io.javalin.http.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tws.vivien.core.LockService;
import tws.vivien.core.Repository;
import tws.vivien.dto.ServerError;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class PushApi implements Api
{
	private static final Logger LOG = LoggerFactory.getLogger(PushApi.class);

	@Inject public Repository repository;
	@Inject public LockService lockService;
	@Inject public GitStatusApi gitStatusApi;

	@Inject public PushApi() {}

	@Override
	public void handle(Context ctx)
	{
		try
		{
			repository.push();
			gitStatusApi.handle(ctx);
		}
		catch (Exception e)
		{
			ctx.status(500);
			ctx.json(ServerError.fromError(e));
		}
	}
}