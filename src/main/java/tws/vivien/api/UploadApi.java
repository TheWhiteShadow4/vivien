package tws.vivien.api;

import io.javalin.http.Context;
import io.javalin.util.FileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tws.vivien.core.Config;
import tws.vivien.core.ErrorBacklog;
import tws.vivien.core.Repository;
import tws.vivien.dto.ServerError;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.locks.ReentrantReadWriteLock;


@Singleton
public class UploadApi implements Api
{
	private static final Logger LOG = LoggerFactory.getLogger(UploadApi.class);

	@Inject public Config config;
	@Inject public Repository repository;
	@Inject public ReentrantReadWriteLock gitLock;
	@Inject public ErrorBacklog errorBacklog;

	@Inject public UploadApi() {}

	@Override
	public void handle(Context ctx)
	{
		String email = ctx.formParam("email");
		String fileOrFolder = ctx.formParam("fileOrFolder");
		if (email == null)
		{
			ctx.status(400);
			ctx.json(new ServerError("Parameter email nicht gesetzt.", null));
			return;
		}
		if (fileOrFolder == null)
		{
			ctx.status(400);
			ctx.json(new ServerError("Parameter fileOrFolder nicht gesetzt.", null));
			return;
		}

		Path targetPath = repository.resolve(fileOrFolder);

		try
		{
			gitLock.readLock().lock();
			if (Files.isDirectory(targetPath)) // Multi Upload in Ordner
			{
				for (var file : ctx.uploadedFiles("files"))
				{
					Path fullPath = targetPath.resolve(file.filename());
					if (isInvalidUploadFile(file.filename()))
					{
						ctx.status(400);
						ctx.json(new ServerError("Unerlaubter Dateityp '" + file.filename() + "'", null));
						return;
					}
					FileUtil.streamToFile(file.content(), fullPath.toString());
					repository.trackFile(fullPath);
				}
			}
			else if (Files.isRegularFile(targetPath)) // Single Upload
			{
				var file = ctx.uploadedFiles("files").getFirst();
				if (isInvalidUploadFile(file.filename()))
				{
					ctx.status(400);
					ctx.json(new ServerError("Unerlaubter Dateityp '" + file.filename() + "'", null));
					return;
				}
				FileUtil.streamToFile(file.content(), targetPath.toString());
				repository.trackFile(targetPath);
			}
			gitStatusApi.handle(ctx);
		}
		catch(Exception e)
		{
			LOG.error("uploadFiles", e);
			ctx.status(500);
			ctx.json(ServerError.fromError(e));
		}
		finally
		{
			gitLock.readLock().unlock();
		}
	}

	private boolean isInvalidUploadFile(String filename)
	{
		return filename.endsWith("gif");
	}
}