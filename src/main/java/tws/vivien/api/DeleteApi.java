package tws.vivien.api;

import io.javalin.http.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tws.vivien.core.*;
import tws.vivien.dto.GitStageRequest;
import tws.vivien.dto.ServerError;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.nio.file.Files;
import java.nio.file.Path;

@Singleton
public class DeleteApi implements Api
{
	private static final Logger LOG = LoggerFactory.getLogger(DeleteApi.class);

	@Inject
	public Config config;
	@Inject public Repository repository;
	@Inject public LockService lockService;
	@Inject public ErrorBacklog errorBacklog;

	@Inject public DeleteApi() {}

	@Override
	public void handle(Context ctx)
	{
		try
		{
			lockService.gitLock.readLock().lock();
			var request = ctx.bodyAsClass(GitStageRequest.class);
			Path file = repository.resolve(request.file);

			String viewName = Server.getViewName(ctx);
			ConfigView view = config.getView(viewName);
			if (view.getFilter().isReadonly(request.file))
			{
				ctx.status(403);
				ctx.json(new ServerError("Keine Schreibberechtigung.", null));
				return;
			}

			LOG.info("Delete: {}", file);
			Files.deleteIfExists(file);
		}
		catch (Exception e)
		{
			LOG.error("delete", e);
			ctx.status(500);
			ctx.json(ServerError.fromError(e));
		}
		finally
		{
			lockService.gitLock.readLock().unlock();
		}
	}
}