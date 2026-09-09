package tws.vivien;

import tws.vivien.core.Server;

public class ServerMain
{
	private Server server;

	static void main() throws Exception
	{
		System.setProperty("org.slf4j.simpleLogger.log.io.javalin", "warn");
		System.setProperty("org.slf4j.simpleLogger.log.org.eclipse.jetty", "warn");

		String protocol = ServerMain.class.getResource("ServerMain.class").getProtocol();
		boolean productionMode = "jar".equals(protocol);

		Server server = new Server(productionMode);
		server.start();
	}
}
