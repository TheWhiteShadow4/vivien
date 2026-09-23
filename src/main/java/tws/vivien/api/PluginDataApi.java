package tws.vivien.api;

import io.javalin.http.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tws.vivien.core.Config;
import tws.vivien.core.ErrorBacklog;
import tws.vivien.core.LockService;
import tws.vivien.core.Repository;
import tws.vivien.dto.PluginRequest;
import tws.vivien.dto.ServerError;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.nio.file.Path;
import java.util.Map;

@Singleton
public class PluginDataApi implements Api
{
	private static final Logger LOG = LoggerFactory.getLogger(PluginDataApi.class);

	@Inject public Config config;
	@Inject public Repository repository;
	@Inject public LockService lockService;
	@Inject public ErrorBacklog errorBacklog;
	@Inject public GitStatusApi gitStatusApi;

	@Inject public PluginDataApi() {}

	@Override
	public void handle(Context ctx)
	{
		if (config.enginePlugin == null)
		{
			ctx.status(500);
			ctx.json(new ServerError("Kein Plugin gesetzt", null));
			return;
		}

		try
		{
			lockService.gitLock.readLock().lock();
			PluginRequest request = ctx.bodyAsClass(PluginRequest.class);
			if (request == null)
			{
				ctx.status(400);
				return;
			}
			LOG.info(String.valueOf(request.data));

			Path path = repository.resolveFile(request.file);
			config.enginePlugin.setImportData(path, (Map<String, Object>) request.data);
		}
		catch (Exception e)
		{
			LOG.error("Fehler bei der Verarbeitung von Plugin Daten", e);
			ctx.status(500).json(ServerError.fromError(e));
		}
		finally
		{
			lockService.gitLock.readLock().unlock();
		}
	}
}
