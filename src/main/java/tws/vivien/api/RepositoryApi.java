package tws.vivien.api;

import io.javalin.http.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tws.vivien.core.Config;
import tws.vivien.core.ConfigView;
import tws.vivien.core.Repository;
import tws.vivien.dto.ServerError;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;

@Singleton
public class RepositoryApi
{
	private static final Logger LOG = LoggerFactory.getLogger(RepositoryApi.class);

	@Inject public Config config;
	@Inject public Repository repository;

	@Inject public RepositoryApi() {}

	public void handle(Context ctx)
	{
		try
		{
			String q = ctx.queryParam("q");
			String viewName = getViewName(ctx);
			ConfigView view = config.getView(viewName);

			if (q != null)
			{
				ctx.json(repository.searchFiles(view, q));
				return;
			}

			String path = ctx.queryParam("path");

			ctx.header("Cache-Control", "no-cache");
			ctx.json(repository.getView(view, path));
		}
		catch (IOException e)
		{
			LOG.error("Fehler bei Request", e);
			ctx.status(500);
			ctx.json(ServerError.fromError(e));
		}
	}

	private static String getViewName(Context ctx)
	{
		String view = ctx.header("X-App-View");
		if (view == null) view = "admin";
		return view;
	}
}
