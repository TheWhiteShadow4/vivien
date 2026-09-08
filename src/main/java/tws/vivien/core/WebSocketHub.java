package tws.vivien.core;

import io.javalin.config.JavalinConfig;
import io.javalin.http.UnauthorizedResponse;
import io.javalin.websocket.WsContext;
import tws.vivien.dto.RepositoryElement;

import java.util.Base64;
import java.util.concurrent.ConcurrentLinkedQueue;

public class WebSocketHub
{
	private final ConcurrentLinkedQueue<WsContext> sessions = new ConcurrentLinkedQueue<>();
	private static final String SESSION_KEY = "SESSION";

	public void configure(JavalinConfig config)
	{
		config.routes.wsBefore("/events", ws -> {
			ws.onConnect(ctx -> {
				// Der Browser sendet die Daten im "Sec-WebSocket-Protocol" Header
				String subProtocol = ctx.header("Sec-WebSocket-Protocol");

				if (subProtocol == null || !subProtocol.startsWith("Basic-")) {
					throw new UnauthorizedResponse("Zutritt verweigert");
				}

				try {
					// "Basic-" abschneiden und Base64 decodieren
					String base64Credentials = subProtocol.substring(6);
					String credentials = new String(Base64.getDecoder().decode(base64Credentials));

					// credentials ist nun "username:password"
					String[] values = credentials.split(":", 2);
					String user = values[0];
					String pass = values[1];

					if (!"admin".equals(user) || !"geheim".equals(pass)) {
						throw new UnauthorizedResponse();
					}
				} catch (Exception e) {
					throw new UnauthorizedResponse("Fehlerhafte Authentifizierung");
				}
			});
		});

		config.routes.ws("/events", ws -> {
			ws.onConnect(ctx -> {

				WebsocketSession session = new WebsocketSession();
				ctx.attribute(SESSION_KEY, session);

				sessions.add(ctx);
			});

			ws.onClose(sessions::remove);
			ws.onError(sessions::remove);

			ws.onMessage(ctx ->
			{
				WebsocketSession session = ctx.attribute(SESSION_KEY);
				session.onMessage(ctx.message());
			});
		});
	}

	public void broadcastFileChanges(RepositoryElement element)
	{
		for (WsContext context : sessions)
		{
			if (context.session.isOpen())
			{
				context.send(element);
			}
		}
	}

	static class WebsocketSession
	{
		void onMessage(String message)
		{

		}
	}
}
