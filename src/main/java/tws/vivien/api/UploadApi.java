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
import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
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
			if (Server.isAdmin(ctx) && Config.CONFIG_FILE_NAME.equals(fileOrFolder))
			{
				acceptSystemFile(ctx);
				gitStatusApi.handle(ctx);
				return;
			}

			Path targetPath = repository.resolve(fileOrFolder);
			if (Files.isDirectory(targetPath)) // Multi Upload in Ordner
			{
				List<ServerError> errors = new ArrayList<>();
				for (var file : ctx.uploadedFiles("files"))
				{
					checkUserLock(ctx, fileOrFolder);
					Path fullPath = targetPath.resolve(file.filename());
					try
					{
						writeFile(ctx, fullPath, file);
					}
					catch(IOException e)
					{
						errors.add(ServerError.fromError(e));
					}
				}
				if (errors.size() > 0)
				{
					LOG.error("Upload Fehler {}", errors.getFirst());
					ctx.status(500);
					ctx.json(errors.stream().toList());
				}
			}
			else if (Files.isRegularFile(targetPath)) // Single Upload
			{
				checkUserLock(ctx, fileOrFolder);

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
			LOG.error("Request Fehler", e);
			ctx.status(500);
			ctx.json(List.of(ServerError.fromError(e)));
		}
		finally
		{
			lockService.gitLock.readLock().unlock();
		}
	}

	private void checkUserLock(Context ctx, String file) throws IOException
	{
		String holder = lockService.fileLocks.get(file);
		if (holder != null && !Objects.equals(holder, ctx.header(Server.APP_USER)))
			throw new IOException("Die Datei ist von " + holder + " gesperrt.");
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

	private void acceptSystemFile(Context ctx) throws IOException
	{
		var file = ctx.uploadedFiles("files").getFirst();

		if (file.filename().equals(Config.CONFIG_FILE_NAME))
		{
			var buf = new BufferedInputStream(file.content());
			buf.mark(0);
			// Wir testen zunächst, ob die neue Konfig gültig ist. Shadow-Load
			Config config = new Config().load(buf);
			if (!config.errors.isEmpty())
			{
				ctx.status(400);
				ctx.json(config.errors.stream().map(ServerError::fromError).toList());
				return;
			}
			buf.reset();
			config.load(buf);
			LOG.info("Neue Konfiguration geladen");
			buf.reset();
		}
		FileUtils.copyInputStreamToFile(file.content(), new File(file.filename()));
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