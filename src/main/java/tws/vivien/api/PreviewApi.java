package tws.vivien.api;

import io.javalin.http.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tws.vivien.core.Cache;
import tws.vivien.core.Config;
import tws.vivien.core.Repository;
import tws.vivien.dto.FileObject;
import tws.vivien.dto.ServerError;
import tws.vivien.handlers.IHandler;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.FileNotFoundException;
import java.util.Map;

@Singleton
public class PreviewApi implements Api
{
	private static final Logger LOG = LoggerFactory.getLogger(PreviewApi.class);

	private final Map<String, IHandler> handlerMap;

	@Inject
	public PreviewApi(Map<String, IHandler> handlerMap)
	{
		this.handlerMap = handlerMap;
	}

	@Inject public Config config;
	@Inject public Repository repository;
	@Inject public Cache cache;

	public void handle(Context ctx)
	{
		String file = ctx.queryParam("file");
		if (file == null)
		{
			ctx.status(400);
			return;
		}

		try
		{

			IHandler handler = forFile(file);
			if (handler == null)
			{
				ctx.status(404);
				return;
			}

			FileObject obj = handler.generatePreview(file);
			ctx.json(obj);
		}
		catch(FileNotFoundException e)
		{
			ctx.status(410);
			ctx.json(ServerError.fromError(e));
		}
		catch (Exception e)
		{
			LOG.error("getPreview", e);
			ctx.status(500);
			ctx.json(ServerError.fromError(e));
		}
	}

	public IHandler forFile(String file)
	{
		String fileExt = file.substring(file.lastIndexOf(".")+1).toLowerCase();
		return handlerMap.get(fileExt);
	}
}
