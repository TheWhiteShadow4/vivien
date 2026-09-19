package tws.vivien;

import tws.vivien.core.Server;

public class ServerMain
{
	public static boolean startFlag;

	public static void main(String[] args)
	{
		System.setProperty("org.slf4j.simpleLogger.log.io.javalin", "warn");
		System.setProperty("org.slf4j.simpleLogger.log.org.eclipse.jetty", "warn");

		String protocol = ServerMain.class.getResource("ServerMain.class").getProtocol();
		boolean productionMode = "jar".equals(protocol);

		startFlag = true;
		while(startFlag)
		{
			startFlag = false;
			try
			{
				Server server = new Server(productionMode);
				server.start();
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}
}
