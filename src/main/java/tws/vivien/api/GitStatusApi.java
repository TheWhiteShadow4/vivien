package tws.vivien.api;

import io.javalin.http.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tws.vivien.core.Config;
import tws.vivien.core.ErrorBacklog;
import tws.vivien.core.Repository;
import tws.vivien.dto.GitBranchStatus;
import tws.vivien.dto.ServerError;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class GitStatusApi implements Api
{
	private static final Logger LOG = LoggerFactory.getLogger(GitStatusApi.class);

	@Inject public Config config;
	@Inject public Repository repository;
	@Inject public ErrorBacklog errorBacklog;

	@Override
	public void handle(Context ctx)
	{
		GitBranchStatus state;
		try
		{
			state = repository.getBranchStatus(config);
		}
		catch(Exception e)
		{
			LOG.error("Request fehlgeschlagen", e);
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
			LOG.error("Request teilweise fehlgeschlagen", e);
			errorBacklog.addRequestError(e);
		}
		ctx.json(state);
	}
}