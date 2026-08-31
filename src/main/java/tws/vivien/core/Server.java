package tws.vivien.core;

import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import io.javalin.http.staticfiles.Location;
import io.javalin.util.FileUtil;
import org.eclipse.jgit.api.errors.GitAPIException;
import tws.vivien.dto.*;
import tws.vivien.handlers.IHandler;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.List;

public class Server
{
	private final Config config;
	private final Cache serverCache;
	public List<Exception> errors = new ArrayList<>();
	private Repository repository;
	private Path webRoot;
	private Map<String, UserStage> userStages = new HashMap<>();
	//private String clientView = "Admin";

	public Server(Config config)
	{
		this.config = config;
		this.serverCache = new Cache();
	}

	public void start()
	{
		IO.println(System.getProperty("user.dir"));
		webRoot = Paths.get("./public").toAbsolutePath();

		try
		{
			this.repository = new Repository(config.repository);
		}
		catch(IOException | GitAPIException e)
		{
			e.printStackTrace();
		}

		Javalin app = Javalin.create(c ->
		{
			// Sagt Vivien, dass sie im Ordner "public" nach statischen Dateien (HTML/JS) suchen soll
			//c.staticFiles.add("./public", Location.EXTERNAL);
			c.staticFiles.add(staticFiles ->
			{
				staticFiles.hostedPath = "/";              // URL-Basis im Browser (Root)
				staticFiles.directory = "public";          // Ordnername (Lass das "./" weg!)
				staticFiles.location = Location.EXTERNAL;  // Dateisystem statt JAR-Classpath

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
				if (config.user != null)
				{
					var credentials = ctx.basicAuthCredentials();
					if (credentials != null
							&& Objects.equals(config.user, credentials.getUsername())
							&& Objects.equals(config.password, credentials.getPassword()))
					{
						return; // Zugriff erlaubt, Filter wird verlassen
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
				state.serverErrors = errors.stream().map(ServerError::fromError).toList();
				ctx.json(state);
			});

			c.routes.get("/api/git", this::getBranchStatus);

			c.routes.post("/api/commit", this::commit);
			c.routes.post("/api/push", this::push);
			c.routes.post("/api/pull", this::pull);
			c.routes.post("/api/stash", this::stash);
			c.routes.post("/api/unstash", this::unstash);

			c.routes.post("/api/upload", this::uploadFiles);
		});
		app.start(config.port);

		System.out.println("Vivien läuft auf http://localhost:" + config.port);
	}

	public void shutdown() throws IOException
	{
		if (repository != null)
			repository.close();
	}

	private void getRepository(Context ctx)
	{
		String q = ctx.queryParam("q");

		if (q != null)
		{
			ctx.json(repository.searchFiles(q));
			return;
		}
		String viewName = getViewName(ctx);
		String path = ctx.queryParam("path");
		try
		{
			ConfigView view = config.getView(viewName);
			ctx.header("Cache-Control", "no-cache");
			ctx.json(repository.getView(view, path));
		}
		catch (IOException e)
		{
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

			FileObject obj = handler.generatePreview(webRoot, repository, serverCache, file);
			ctx.json(obj);
		}
		catch (Exception e)
		{
			ctx.status(500);
			ctx.json(ServerError.fromError(e));
		}
	}

	private void getBranchStatus(Context ctx)
	{
		GitBranchStatus state;
		try
		{
			state = repository.getBranchStatus();
		}
		catch(Exception e)
		{
			e.printStackTrace();
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
			e.printStackTrace();
			errors.add(e);
		}
		ctx.json(state);
	}

	private void uploadFiles(Context ctx)
	{
		String email = ctx.formParam("email");
		String folderName = ctx.formParam("folderName");
		if (email == null)
		{
			ctx.status(400);
			ctx.json(new ServerError("Parameter email nicht gesetzt.", null));
			return;
		}
		if (folderName == null)
		{
			ctx.status(400);
			ctx.json(new ServerError("Parameter folderName nicht gesetzt.", null));
			return;
		}

		Path folderPath = repository.resolveFolder(folderName);

		UserStage userstage = userStages.computeIfAbsent(email, k -> new UserStage());

		ctx.uploadedFiles("files").forEach(file -> {
			Path path = folderPath.resolve(file.filename());
			FileUtil.streamToFile(file.content(), path.toString());
			userstage.added.add(path);
		});

		ctx.json(new StageInfo(userstage));
	}

	private void commit(Context ctx)
	{
		try
		{
			CommitRequest request = ctx.bodyAsClass(CommitRequest.class);
			repository.commit(request, userStages.get(request.email));
		}
		catch (Exception e)
		{
			e.printStackTrace();
			ctx.status(500);
			ctx.json(ServerError.fromError(e));
		}
	}

	private void push(Context ctx)
	{
		try
		{
			repository.push();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			ctx.status(500);
			ctx.json(ServerError.fromError(e));
		}
	}

	private void pull(Context ctx)
	{
		try
		{
			repository.pull();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			ctx.status(500);
			ctx.json(ServerError.fromError(e));
		}
	}

	private void stash(Context ctx)
	{
		try
		{
			repository.stash();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			ctx.status(500);
			ctx.json(ServerError.fromError(e));
		}
	}

	private void unstash(Context ctx)
	{
		try
		{
			repository.unstash();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			ctx.status(500);
			ctx.json(ServerError.fromError(e));
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
