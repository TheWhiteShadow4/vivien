package tws.vivien.api;

import io.javalin.http.Context;
import org.eclipse.jgit.transport.RemoteRefUpdate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tws.vivien.core.*;
import tws.vivien.dto.CommitRequest;
import tws.vivien.dto.ServerError;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class CommitApi implements Api
{
	private static final Logger LOG = LoggerFactory.getLogger(CommitApi.class);

	@Inject public Config config;
	@Inject public Repository repository;
	@Inject public LockService lockService;
	@Inject	public ErrorBacklog errorBacklog;
	@Inject public GitStatusApi gitStatusApi;

	@Inject public CommitApi() {}

	@Override
	public void handle(Context ctx)
	{
		try
		{
			lockService.gitLock.writeLock().lock();
			CommitRequest request = ctx.bodyAsClass(CommitRequest.class);

			var status = repository.getBranchStatus();
			if (status.hasStagedFiles()) // Haben wir Änderungen in der Stage
			{
				lockService.freeUserLocks(ctx.header(Server.APP_USER));
				repository.commit(request);
			}
			if (config.gitRemote != null)
			{
				var result = repository.push();
				if (result == RemoteRefUpdate.Status.REJECTED_NONFASTFORWARD)
				{
					ctx.status(409);
					ctx.json(new ServerError("Speichern fehlgeschlagen. Aktualisierung erforderlich.", null));
				}
			}
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
			lockService.gitLock.writeLock().unlock();
		}
	}
}