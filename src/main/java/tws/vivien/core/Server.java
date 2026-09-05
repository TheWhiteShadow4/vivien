package tws.vivien.core;

import io.javalin.Javalin;
import io.javalin.compression.CompressionStrategy;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import io.javalin.http.staticfiles.Location;
import io.javalin.util.FileUtil;
import org.eclipse.jgit.transport.RemoteRefUpdate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tws.vivien.dto.*;
import tws.vivien.handlers.IHandler;

import java.awt.*;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Stream;

public class Server
{
	private static final Logger LOG = LoggerFactory.getLogger(Server.class);

	private final Config config;
	private final Cache serverCache;
	private final Repository repository;
	//private final Map<String, UserStage> userStages = new HashMap<>();
	private final boolean productionMode;
	// Lock um Git Operationen(write) gegenüber kleine Datei Operationen(read) abzusichern.
	private final ReentrantReadWriteLock gitLock = new ReentrantReadWriteLock();

	public List<Exception> persistedErrors = new ArrayList<>();
	public List<Exception> requestErrors = new ArrayList<>();

	public Server(Config config, boolean productionMode) throws Exception
	{
		this.config = config;
		this.serverCache = new Cache("cache");
		this.repository = new Repository(config.repository);
		this.productionMode = productionMode;
	}

	public void start()
	{
		LOG.debug("Working Directory: {}", System.getProperty("user.dir"));
		LOG.debug("Cache Directory: {}", serverCache.getCacheFolder());

		/*try
		{
			var c = new CommitRequest();
			c.name = "Anna";
			c.email = "anna@exampl.de";
			c.message = "Hallo";
			var s = new UserStage();
			s.added.add("tennis-gal.png");
			repository.commit(c, s);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}*/

		Javalin app = Javalin.create(c ->
		{
			c.startup.showJavalinBanner = false;
			c.http.compressionStrategy = CompressionStrategy.GZIP;
			// Brotli braucht eine weitere Abhängigkeit
			//c.http.compressionStrategy = new CompressionStrategy(new Brotli(4), new Gzip(6));

			if (productionMode)
			{
				c.spaRoot.addFile("/", "/public/index.html", Location.CLASSPATH);
				c.staticFiles.add(staticFiles ->
				  {
					  staticFiles.hostedPath = "/";
					  staticFiles.directory = "public";
					  staticFiles.location = Location.CLASSPATH;
				  });
			}

			c.staticFiles.add(staticFiles -> {
				staticFiles.hostedPath = "/cache";
				staticFiles.directory = serverCache.getCacheFolder().toString();
				staticFiles.location = Location.EXTERNAL;

				staticFiles.headers = Map.of("Cache-Control", "public, max-age=86400, immutable");
			});

			c.bundledPlugins.enableCors(cors ->
					cors.addRule(rule ->
					{
						if (config.mode == ServerMode.SETUP || config.security == SecurityMode.LAX)
						{
							rule.anyHost(); // Aktiviert Cross-Origin-Requests
						}
						else
						{
							rule.reflectClientOrigin = true;
							rule.allowCredentials = true;
						}
					}));

			// Der Before-Filter für geschützte Routen
			c.routes.before("/api/*", ctx ->
			{
				if (config.password != null)
				{
					var credentials = ctx.basicAuthCredentials();
					if (credentials != null && Objects.equals(config.password, credentials.getPassword()))
					{
						if (config.validUsers == null || config.validUsers.contains(credentials.getUsername()))
						{
							return; // OK
						}
					}
					// Der Header 'WWW-Authenticate' sagt dem Browser, dass es Basic Auth ist
					ctx.header("WWW-Authenticate", "Basic realm=\"Protected Area\"");
					ctx.status(HttpStatus.UNAUTHORIZED).result("Zugriff verweigert");
				}
			});

			c.routes.get("/api/repo", this::getRepository);

			c.routes.get("/api/preview", this::getPreview);

			c.routes.get("/api/state", ctx ->
			{
				var state = new ServerState();
				state.mode = config.mode;
				state.view = getViewName(ctx);
				//state.user = new ServerUser(config.user);
				state.serverErrors = Stream.concat(persistedErrors.stream(), requestErrors.stream()).map(ServerError::fromError).toList();
				ctx.json(state);
				requestErrors.clear();
			});

			c.routes.get("/api/git", this::getBranchStatus);
			c.routes.get("/api/download", this::downloadFile);

			c.routes.post("/api/delete", this::delete);
			c.routes.post("/api/staged", this::staged);
			c.routes.post("/api/checkout", this::checkout);
			c.routes.post("/api/reset", this::reset);
			c.routes.post("/api/commit", this::commit);
			c.routes.post("/api/pull", this::pull);
			c.routes.post("/api/stash", this::stash);
			c.routes.post("/api/unstash", this::unstash);
			c.routes.post("/api/upload", this::uploadFiles);
		});
		app.start(config.port);

		System.out.println("Vivien läuft auf " + config.serverHost + ":" + config.port);
	}

	public void shutdown() throws IOException
	{
		repository.close();
	}

	private void getRepository(Context ctx)
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
			LOG.error("getRepository", e);
			ctx.status(500);
			ctx.json(ServerError.fromError(e));
		}
	}

	private void getPreview(Context ctx)
	{
		String file = ctx.queryParam("file");
		if (file == null)
		{
			ctx.status(400);
			return;
		}

		try
		{
			IHandler handler = PreviewGenerator.forFile(file);
			if (handler == null)
			{
				ctx.status(404);
				return;
			}

			FileObject obj = handler.generatePreview(config, repository, serverCache, file);
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

	private void getBranchStatus(Context ctx)
	{
		GitBranchStatus state;
		try
		{
			state = repository.getBranchStatus(config);
		}
		catch(Exception e)
		{
			LOG.error("getBranchStatus", e);
			ctx.status(500);
			ctx.json(ServerError.fromError(e));
			return;
		}

		try
		{
			state.remote = repository.getRemoteStatus(config, state.branch);
		}
		catch(Exception e)
		{
			LOG.error("getBranchStatus", e);
			requestErrors.add(e);
		}
		ctx.json(state);
	}

	private void uploadFiles(Context ctx)
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
			getBranchStatus(ctx);
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

	private void downloadFile(Context ctx)
	{
		try
		{
			String file = ctx.queryParam("file");
			Path path = repository.resolveFile(file);
			if (path == null)
			{
				ctx.status(404);
				return;
			}

			gitLock.readLock().lock();
			ctx.header("Content-Disposition", "attachment; filename=\"" + path.getFileName().toString() + "\"");
			ctx.contentType(Files.probeContentType(path));

			// Datei als Stream an die Antwort übergeben
			ctx.result(Files.newInputStream(path));
		}
		catch (Exception e)
		{
			LOG.error("uploadFiles", e);
			requestErrors.add(e);
			ctx.status(500);
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

	private void delete(Context ctx)
	{
		try
		{
			gitLock.readLock().lock();
			var request = ctx.bodyAsClass(GitStageRequest.class);
			Path file = repository.resolve(request.file);
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
			gitLock.readLock().unlock();
		}
	}

	private void staged(Context ctx)
	{
		try
		{
			var request = ctx.bodyAsClass(GitStageRequest.class);

			if (request.email == null) throw new NullPointerException("email ist null");
			if (request.file == null) throw new NullPointerException("file ist null");

			gitLock.readLock().lock();
			Path file = repository.resolve(request.file);
			IO.println("staged " + request.op + " File: " + request.file + " => "+ file);

			switch (request.op)
			{
				case GitStageOperation.Track:
					repository.trackFile(file);
					break;
				case GitStageOperation.Untrack:
					repository.untrackFile(file);
					break;
				case GitStageOperation.Delete:
					repository.deleteFile(file);
					break;
				case GitStageOperation.Undelete:
					repository.undeleteFile(file);
					break;
			}
			getBranchStatus(ctx);
		}
		catch (Exception e)
		{
			LOG.error("staged", e);
			ctx.status(500);
			requestErrors.add(e);
		}
		finally
		{
			gitLock.readLock().unlock();
		}
	}

	private void checkout(Context ctx)
	{
		try
		{
			var request = ctx.bodyAsClass(CheckoutRequest.class);

			if (request.branch == null) throw new NullPointerException("branch ist null");

			gitLock.writeLock().lock();
			repository.checkout(request.branch);
			getBranchStatus(ctx);
		}
		catch (Exception e)
		{
			LOG.error("checkout", e);
			ctx.status(500);
			ctx.json(ServerError.fromError(e));
		}
		finally
		{
			gitLock.writeLock().unlock();
		}
	}

	private void reset(Context ctx)
	{
		try
		{
			gitLock.writeLock().lock();
			repository.reset();
			getBranchStatus(ctx);
		}
		catch (Exception e)
		{
			LOG.error("reset", e);
			ctx.status(500);
			ctx.json(ServerError.fromError(e));
		}
		finally
		{
			gitLock.writeLock().unlock();
		}
	}

	private void commit(Context ctx)
	{
		try
		{
			CommitRequest request = ctx.bodyAsClass(CommitRequest.class);

			gitLock.writeLock().lock();
			var status = repository.getCachedStatus();
			if (status == null) status = repository.getBranchStatus(config);

			if (status.changed.size() > 0) // Haben wir Änderungen in der Stage
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
			getBranchStatus(ctx);
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

	/*private void push(Context ctx)
	{
		try
		{
			repository.push(config);
			getBranchStatus(ctx);
		}
		catch (Exception e)
		{
			ctx.status(500);
			ctx.json(ServerError.fromError(e));
		}
	}*/

	private void pull(Context ctx)
	{
		try
		{
			gitLock.writeLock().lock();
			if (config.gitRemote != null)
			{
				repository.fetch(config);
				repository.pull(config);
				getBranchStatus(ctx);
			}
			else
			{
				ctx.json(repository.getCachedStatus());
			}
		}
		catch (Exception e)
		{
			LOG.error("pull", e);
			ctx.status(500);
			ctx.json(ServerError.fromError(e));
		}
		finally
		{
			gitLock.writeLock().unlock();
		}
	}

	private void stash(Context ctx)
	{
		try
		{
			gitLock.writeLock().lock();
			repository.stash();
			getBranchStatus(ctx);
		}
		catch (Exception e)
		{
			LOG.error("stash", e);
			ctx.status(500);
			ctx.json(ServerError.fromError(e));
		}
		finally
		{
			gitLock.writeLock().unlock();
		}
	}

	private void unstash(Context ctx)
	{
		try
		{
			gitLock.writeLock().lock();
			repository.unstash();
			getBranchStatus(ctx);
		}
		catch (Exception e)
		{
			LOG.error("unstash", e);
			ctx.status(500);
			ctx.json(ServerError.fromError(e));
		}
		finally
		{
			gitLock.writeLock().unlock();
		}
	}

	private String getViewName(Context ctx)
	{
		String view = ctx.header("X-App-View");
		if (view == null) view = "admin";
		return view;
	}

	public void openBrowser()
	{
		if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE))
		{
			//String protocol = config.cert != null ? "https//" : "http//";
			try {
				Desktop.getDesktop().browse(new URI(config.serverHost + ":" + config.port));
				System.out.println("Standard-Browser wurde automatisch geöffnet.");
			} catch (Exception e) {
				System.err.println("Browser konnte nicht automatisch geöffnet werden: " + e.getMessage());
			}
		}
	}
}
