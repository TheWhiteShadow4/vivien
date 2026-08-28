package tws.vivien.view;

import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import tws.vivien.core.*;
import tws.vivien.dto.ServerError;
import tws.vivien.dto.ServerState;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Server
{
	private final Config config;
	public List<Exception> errors = new ArrayList<>();
	private Repository repository;
	private Cache serverCache;
	//private String clientView = "Admin";

	public Server(Config config)
	{
		this.config = config;
		this.serverCache = new Cache();
	}

	public void start()
	{
		Javalin app = Javalin.create(c -> {
			// Sagt Vivien, dass sie im Ordner "public" nach statischen Dateien (HTML/JS) suchen soll
			c.staticFiles.add("/public");

			c.bundledPlugins.enableCors(cors ->
				cors.addRule(rule -> {
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
			c.routes.before("/api/*", ctx -> {
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

			c.routes.get("/api/state", ctx -> {
				var state = new ServerState();
				state.mode = config.mode;
				state.view = getViewName(ctx);
				state.serverErrors = errors.stream().map(ServerError::fromError).toList();
				ctx.header("Cache-Control", "no-cache");
				ctx.json(state);
			});
		});
		app.start(config.port);

		System.out.println("Vivien läuft auf http://localhost:" + config.port);
	}

	public void shutdown() throws IOException
	{
		if (repository != null)
			repository.close();
	}

	private Repository getOrCreateRepository() throws IOException
	{
		if (repository == null)
		{
			repository = new Repository(config.repository);
		}
		return repository;
	}

	private void getRepository(Context ctx)
	{
		String viewname = getViewName(ctx);
		try
		{
			ConfigView view = config.getView(viewname);
			ctx.header("Cache-Control", "no-cache");
			ctx.json(getOrCreateRepository().getView(view));
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

		var generator = new PreviewGenerator(serverCache);
		if (!generator.isSupported(file))
		{
			ctx.status(404);
			return;
		}

		try
		{
			Path path = getOrCreateRepository().resolve(file);
			if (path != null)
			{
				generator.sendPreview(path, "", ctx);
			}
		}
		catch (IOException e)
		{
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
