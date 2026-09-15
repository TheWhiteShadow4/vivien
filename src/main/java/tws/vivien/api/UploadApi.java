package tws.vivien.api;

import io.javalin.http.Context;
import io.javalin.http.UploadedFile;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tws.vivien.core.*;
import tws.vivien.dto.ServerError;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;


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

		try
		{
			lockService.gitLock.readLock().lock();
			if (config.mode == ServerMode.SETUP && "setup.toml".equals(fileOrFolder))
			{
				acceptSetupConfig(ctx);
				return;
			}

			Path targetPath = repository.resolve(fileOrFolder);
			if (Files.isDirectory(targetPath)) // Multi Upload in Ordner
			{
				for (var file : ctx.uploadedFiles("files"))
				{
					String holder = lockService.fileLocks.get(fileOrFolder);
					if (holder != null) throw new IOException("Die Datei ist von " + holder + " gesperrt.");

					Path fullPath = targetPath.resolve(file.filename());
					writeFile(ctx, fullPath, file);
				}
			}
			else if (Files.isRegularFile(targetPath)) // Single Upload
			{
				String holder = lockService.fileLocks.get(fileOrFolder);
				if (holder != null) throw new IOException("Die Datei ist von " + holder + " gesperrt.");

				if (Objects.equals(ctx.formParam("unlock"),"true"))
				{
					lockService.freeUserLocks(ctx.header(Server.APP_USER));
				}
				var file = ctx.uploadedFiles("files").getFirst();
				writeFile(ctx, targetPath, file);
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

	private void writeFile(Context ctx, Path path, UploadedFile file) throws Exception
	{
		if (!isValidUploadFile(file.filename()))
		{
			ctx.status(400);
			ctx.json(new ServerError("Unerlaubter Dateityp '" + file.filename() + "'", null));
			return;
		}
		FileUtils.copyInputStreamToFile(file.content(), path.toFile());
		repository.trackFile(path);
	}

	private void acceptSetupConfig(Context ctx) throws IOException
	{
		var file = ctx.uploadedFiles("files").getFirst();
		FileUtils.copyInputStreamToFile(file.content(), new File(Config.CONFIG_FILE_NAME));
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