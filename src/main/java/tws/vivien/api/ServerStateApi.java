package tws.vivien.api;

import io.javalin.http.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tws.vivien.core.Config;
import tws.vivien.core.ErrorBacklog;
import tws.vivien.dto.ServerError;
import tws.vivien.dto.ServerState;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.stream.Stream;

@Singleton
public class ServerStateApi implements Api
{
	private static final Logger LOG = LoggerFactory.getLogger(ServerStateApi.class);

	@Inject	public Config config;
	@Inject	public ErrorBacklog errorBacklog;

	@Inject
	public ServerStateApi() {}

	@Override
	public void handle(Context ctx)
	{
		var state = new ServerState();
		state.mode = config.mode;
		state.view = getViewName(ctx);
		state.serverErrors = Stream.concat(errorBacklog.getSystemErrors().stream(), errorBacklog.readRequestErrors().stream()).map(ServerError::fromError).toList();
		ctx.json(state);
	}

	private static String getViewName(Context ctx)
	{
		String view = ctx.header("X-App-View");
		if (view == null) view = "admin";
		return view;
	}
}
