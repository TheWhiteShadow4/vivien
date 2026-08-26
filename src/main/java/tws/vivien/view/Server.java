package tws.vivien.view;

import io.javalin.Javalin;
import io.javalin.http.HttpStatus;
import io.javalin.plugin.bundled.CorsPluginConfig;
import tws.vivien.core.Config;
import tws.vivien.core.SecurityMode;
import tws.vivien.dto.*;

import java.awt.*;
import java.net.URI;
import java.util.*;
import java.util.List;

public class Server
{
	private final Config config;
	public List<Exception> errors = new ArrayList<>();

	public Server(Config config)
	{
		this.config = config;
	}

	public void start()
	{
		Javalin app = Javalin.create(c -> {
			// Sagt Vivien, dass sie im Ordner "public" nach statischen Dateien (HTML/JS) suchen soll
			c.staticFiles.add("/public");

			c.bundledPlugins.enableCors(cors ->
				cors.addRule(rule -> {
					if (config.security == SecurityMode.LAX)
					{
						rule.anyHost(); // Aktiviert Cross-Origin-Requests
					}
					else
					{
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

			c.routes.get("/api/repo", ctx -> {
				ctx.result("Willkommen im Dashboard, " + config.user);
			});

			c.routes.get("/api/state", ctx -> {
				var state = new ServerState();
				state.mode = config.mode;
				state.view = "Admin";
				state.serverErrors = errors.stream()
					.map(e -> new ServerError(e.getMessage(),
						String.join("\n",
								Arrays.stream(e.getStackTrace())
									  .map(StackTraceElement::toString).toList())
						 )
					).toList();

				ctx.json(state);
			});
		});
		app.start(config.port);

		System.out.println("Vivien läuft auf http://localhost:" + config.port);
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
