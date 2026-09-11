package tws.vivien.api;

import io.javalin.http.Context;
import org.eclipse.jgit.transport.RemoteRefUpdate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tws.vivien.core.Config;
import tws.vivien.core.ErrorBacklog;
import tws.vivien.core.Repository;
import tws.vivien.dto.CommitRequest;
import tws.vivien.dto.ServerError;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Singleton
public class CommitApi implements Api
{
	private static final Logger LOG = LoggerFactory.getLogger(CommitApi.class);

	@Inject public Config config;
	@Inject public Repository repository;
	@Inject public ReentrantReadWriteLock gitLock;
	@Inject	public ErrorBacklog errorBacklog;
	@Inject public GitStatusApi gitStatusApi;

	@Inject public CommitApi() {}

	@Override
	public void handle(Context ctx)
	{
		try
		{
			CommitRequest request = ctx.bodyAsClass(CommitRequest.class);

			gitLock.writeLock().lock();
			var status = repository.getCachedStatus();
			if (status == null) status = repository.getBranchStatus(config);

			if (status.added.size() > 0 || status.changed.size() > 0 || status.removed.size() > 0) // Haben wir Änderungen in der Stage
			{
				repository.commit(request);
			}
			if (config.gitRemote != null)
			{
				var result = repository.push(config);
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
			LOG.error("commit", e);
			ctx.status(500);
			ctx.json(ServerError.fromError(e));
		}
		finally
		{
			gitLock.writeLock().unlock();
		}
	}
}