package tws.vivien.api;

import io.javalin.http.Context;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tws.vivien.core.Config;
import tws.vivien.core.ErrorBacklog;
import tws.vivien.core.LockService;
import tws.vivien.core.Repository;
import tws.vivien.dto.ServerError;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.nio.file.Files;
import java.nio.file.Path;


@Singleton
public class UploadApi implements Api
{
	private static final Logger LOG = LoggerFactory.getLogger(UploadApi.class);

	@Inject public Config config;
	@Inject public Repository repository;
	@Inject public LockService lockService;
	@Inject public ErrorBacklog errorBacklog;
	@Inject public GitStatusApi gitStatusApi;

	@Inject public UploadApi() {}

	@Override
	public void handle(Context ctx)
	{
		String fileOrFolder = ctx.formParam("fileOrFolder");
		if (fileOrFolder == null)
		{
			ctx.status(400);
			ctx.json(new ServerError("Parameter fileOrFolder nicht gesetzt.", null));
			return;
		}

		Path targetPath = repository.resolve(fileOrFolder);

		try
		{
			lockService.gitLock.readLock().lock();
			if (Files.isDirectory(targetPath)) // Multi Upload in Ordner
			{
				for (var file : ctx.uploadedFiles("files"))
				{
					Path fullPath = targetPath.resolve(file.filename());
					if (!isValidUploadFile(file.filename()))
					{
						ctx.status(400);
						ctx.json(new ServerError("Unerlaubter Dateityp '" + file.filename() + "'", null));
						return;
					}
					FileUtils.copyInputStreamToFile(file.content(), fullPath.toFile());
					repository.trackFile(fullPath);
				}
			}
			else if (Files.isRegularFile(targetPath)) // Single Upload
			{
				var file = ctx.uploadedFiles("files").getFirst();
				if (!isValidUploadFile(file.filename()))
				{
					ctx.status(400);
					ctx.json(new ServerError("Unerlaubter Dateityp '" + file.filename() + "'", null));
					return;
				}
				FileUtils.copyInputStreamToFile(file.content(), targetPath.toFile());
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
			lockService.gitLock.readLock().unlock();
		}
	}

	private boolean isValidUploadFile(String filename)
	{
		if (config.validFileformats == null) return true;
		for(var format : config.validFileformats)
		{
			if (filename.endsWith(format)) return true;
		}
		return false;
	}
}