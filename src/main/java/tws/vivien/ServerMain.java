package tws.vivien;

import tws.vivien.core.Config;
import tws.vivien.core.Server;
import tws.vivien.core.ServerMode;

import java.awt.*;
import java.net.URI;

public class ServerMain
{
	private Server server;

	static void main() throws Exception
	{
		System.setProperty("org.slf4j.simpleLogger.log.io.javalin", "warn");
		System.setProperty("org.slf4j.simpleLogger.log.org.eclipse.jetty", "warn");

		Config config = new Config();

		Server server = new Server(config);
		if (!config.errors.isEmpty())
		{
			server.persistedErors.addAll(config.errors);
		}
		server.start();

		if (config.mode != ServerMode.HOSTED)
		{
			server.openBrowser();
		}
	}

	private static void openBrowser(String url)
	{
		if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE))
		{
			try {
				Desktop.getDesktop().browse(new URI(url));
				System.out.println("Standard-Browser wurde automatisch geöffnet.");
			} catch (Exception e) {
				System.err.println("Browser konnte nicht automatisch geöffnet werden: " + e.getMessage());
			}
		}
	}
}
