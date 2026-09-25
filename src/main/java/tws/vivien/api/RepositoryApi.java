package tws.vivien.api;

import io.javalin.http.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tws.vivien.core.Config;
import tws.vivien.core.ConfigView;
import tws.vivien.core.Repository;
import tws.vivien.core.Server;
import tws.vivien.dto.ElementType;
import tws.vivien.dto.RepositoryElement;
import tws.vivien.dto.ServerError;

import javax.inject.Inject;
import javax.inject.Singleton;

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
			String viewName = Server.getViewName(ctx);
			ConfigView view = config.getView(viewName);

			if (q != null)
			{
				RepositoryElement root = new RepositoryElement("query", "", ElementType.VIRTUAL);
				if (":config".equals(q))
				{
					root.children = config.getConfigFileList();
				}
				else if (":history".equals(q))
				{
					root.children = repository.getHistory(10);
				}
				else
				{
					root.children = repository.searchFiles(view, q);
				}
				ctx.json(root);
			}
			else
			{
				String path = ctx.queryParam("path");

				ctx.header("Cache-Control", "no-cache");
				ctx.json(repository.getView(view, path));
			}
		}
		catch (Exception e)
		{
			LOG.error("Fehler bei Request", e);
			ctx.status(500);
			ctx.json(ServerError.fromError(e));
		}
	}
}
