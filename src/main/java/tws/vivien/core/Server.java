package tws.vivien.core;

import io.javalin.Javalin;
import io.javalin.compression.CompressionStrategy;
import io.javalin.http.HttpStatus;
import io.javalin.http.staticfiles.Location;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.net.URI;
import java.util.Map;
import java.util.Objects;

public class Server
{
	private static final Logger LOG = LoggerFactory.getLogger(Server.class);

	private final Config config;
	private final Cache serverCache;
	private final ServerComponent component;
	private final boolean productionMode;

	public Server(boolean productionMode) throws Exception
	{
		this.productionMode = productionMode;

		var module = new DependencyModule("cache");
		this.component = DaggerServerComponent.builder()
				.dependencyModule(module)
				.previewModule(new PreviewModule())
				.build();

		this.config = component.config();
		this.serverCache = component.serverCache();
		var errorBacklog = component.errorBacklog();

		if (!config.errors.isEmpty())
		{
			errorBacklog.addSystemErrors(config.errors);
		}
	}

	public void start()
	{
		LOG.debug("Working Directory: {}", System.getProperty("user.dir"));
		LOG.debug("Cache Directory: {}", serverCache.getCacheFolder());

		Javalin app = Javalin.create(c ->
		{
			c.startup.showJavalinBanner = false;
			c.http.compressionStrategy = CompressionStrategy.GZIP;

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
					try
					{
						var credentials = ctx.basicAuthCredentials();
						if (credentials != null && Objects.equals(config.password, credentials.getPassword()))
						{
							if (config.validUsers == null || config.validUsers.contains(credentials.getUsername()))
							{
								return; // OK
							}
						}
					}
					catch(Exception e)
					{
						// Der Header 'WWW-Authenticate' sagt dem Browser, dass es Basic Auth ist
						ctx.header("WWW-Authenticate", "Basic realm=\"Protected Area\"");
						ctx.status(HttpStatus.UNAUTHORIZED).result("Zugriff verweigert");
					}
				}
			});

			c.routes.get("/api/state", ctx -> component.serverStateApi().handle(ctx));
			c.routes.get("/api/repo", ctx -> component.repositoryApi().handle(ctx));
			c.routes.get("/api/preview", ctx -> component.previewApi().handle(ctx));

			c.routes.get("/api/git", ctx -> component.gitStaturApi().handle(ctx));
			c.routes.get("/api/download", ctx -> component.downloadApi().handle(ctx));

			c.routes.post("/api/delete", ctx -> component.deleteApi().handle(ctx));
			c.routes.post("/api/staged", ctx -> component.stageApi().handle(ctx));
			c.routes.post("/api/checkout", ctx -> component.checkoutApi().handle(ctx));
			c.routes.post("/api/reset", ctx -> component.resetApi().handle(ctx));
			c.routes.post("/api/commit", ctx -> component.commitApi().handle(ctx));
			c.routes.post("/api/pull", ctx -> component.pullApi().handle(ctx));
			c.routes.post("/api/stash", ctx -> component.stashApi().handle(ctx));
			c.routes.post("/api/unstash", ctx -> component.unstashApi().handle(ctx));
			c.routes.post("/api/upload", ctx -> component.uploadApi().handle(ctx));
		});
		app.start(config.port);

		System.out.println("Vivien läuft auf " + config.serverHost + ":" + config.port);
		if (config.mode != ServerMode.HOSTED)
		{
			openBrowser();
		}
	}

	public void openBrowser()
	{
		if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE))
		{
			try {
				Desktop.getDesktop().browse(new URI(config.serverHost + ":" + config.port));
				System.out.println("Standard-Browser wurde automatisch geöffnet.");
			} catch (Exception e) {
				System.err.println("Browser konnte nicht automatisch geöffnet werden: " + e.getMessage());
			}
		}
	}
}
