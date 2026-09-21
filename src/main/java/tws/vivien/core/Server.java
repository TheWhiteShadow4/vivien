package tws.vivien.core;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import io.javalin.Javalin;
import io.javalin.compression.CompressionStrategy;
import io.javalin.http.Context;
import io.javalin.http.HandlerType;
import io.javalin.http.UnauthorizedResponse;
import io.javalin.http.staticfiles.Location;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tws.vivien.dto.LoginRequest;

import java.awt.*;
import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.Map;

public class Server
{
	private static final Logger LOG = LoggerFactory.getLogger(Server.class);

	public static final String APP_VIEW = "X-App-View";
	public static final String APP_USER = "X-App-User";
	public static final String DEFAULT_USER = "global";

	private static final java.util.List<String> REPO_ACCESS_FILES = List.of("fbx", "gltf", "glb", "obj", "bin", "mtl", "wav", "mp3", "ogg", "aac", "png", "jpg");

	private final Config config;
	private final Cache serverCache;
	private final ServerComponent component;
	private final boolean productionMode;
	private Javalin app;

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

		this.app = Javalin.create(c ->
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

			var repo = component.repository();
			if (repo != null && repo.getRoot() != null)
			{
				c.staticFiles.add(staticFiles ->
				{
					staticFiles.hostedPath = "/file";
					staticFiles.directory = repo.getRoot().toString();
					staticFiles.location = Location.EXTERNAL;
					staticFiles.skipFileFunction = (req) ->
					{
						var ext = FilenameUtils.getExtension(req.getRequestURI());
						return !REPO_ACCESS_FILES.contains(ext);
					};
				});
			}

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

			// Basic Auth Absicherung
			c.routes.post("/api/login", this::handleLogin);
			c.routes.before("/api/*", this::authFilter);
			c.routes.before("/cache/*", this::authFilter);
			c.routes.before("/file/*", this::authFilter);

			c.routes.get("/api/state", ctx -> component.serverStateApi().handle(ctx));
			c.routes.get("/api/preview", ctx -> component.previewApi().handle(ctx));
			c.routes.post("/api/upload", ctx -> component.uploadApi().handle(ctx));
			if (config.mode != ServerMode.SETUP)
			{
				c.routes.get("/api/repo", ctx -> component.repositoryApi().handle(ctx));
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
				c.routes.post("/api/create", ctx -> component.createApi().handle(ctx));
				c.routes.post("/api/move", ctx -> component.moveApi().handle(ctx));
				c.routes.post("/api/filelock", ctx -> component.fileLockApi().handle(ctx));
			}
		});
		app.start(config.port);

		System.out.println("Vivien läuft auf " + config.serverHost + ":" + config.port);
		if (config.mode != ServerMode.HOSTED)
		{
			openBrowser();
		}
	}

	private void authFilter(Context ctx)
	{
		String token = ctx.cookie("auth_token");
		if (token == null)
		{
			if ("/api/login".equals(ctx.path())) return;

			ctx.status(401).result("Nicht eingeloggt");
		}
		else
		{
			try
			{
				DecodedJWT jwt = JWT.require(Algorithm.HMAC256(config.secret)).build().verify(token);
				ctx.attribute("user", jwt.getClaim("user").asString());
				ctx.attribute("view", jwt.getClaim("view").asString());
			}
			catch (JWTVerificationException e)
			{
				ctx.status(401).result("Ungültiger Token");
			}
		}
	}

	private void handleLogin(Context ctx)
	{
		if (ctx.method() == HandlerType.POST)
		{
			LoginRequest request = ctx.bodyAsClass(LoginRequest.class);
			if (request.user != null)
			{
				if (config.password != null && !config.password.equals(request.pass)) throw new UnauthorizedResponse("Login Fehler");
				if (config.validUsers != null && !config.validUsers.contains(request.user)) throw new UnauthorizedResponse("Login Fehler");

				// TODO: Passwort pro User konfigurierbar machen.

				int expires = 60 * 60 * 48;
				String token = JWT.create()
						.withClaim("user", request.user)
						.withClaim("view", request.view)
						.withExpiresAt(Instant.now().plusSeconds(expires))
						.sign(Algorithm.HMAC256(config.secret));

				ctx.header("Set-Cookie", "auth_token=" + token + "; HttpOnly; SameSite=Strict; Path=/; Max-Age=" + expires);
				return;
			}
		}
		ctx.status(401).result("Login Fehler");
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

	/// TODO: Maximale Sicherheit!
	/// Aktuell kann der User selber bestimmen, ob er Admin sein will.
	public static boolean isAdmin(Context ctx)
	{
		return "admin".equals(getViewName(ctx));
	}

	/**
	 * Liest den Benutzer View aus dem Header aus.
	 */
	public static String getViewName(Context ctx)
	{
		String view = ctx.header(APP_VIEW);
		if (view == null) view = "admin";
		return view;
	}

	/**
	 * Liest den Benutzer Name aus dem Header aus.
	 */
	public static String getUserName(Context ctx)
	{
		String view = ctx.header(APP_USER);
		if (view == null) view = DEFAULT_USER;
		return view;
	}
}
