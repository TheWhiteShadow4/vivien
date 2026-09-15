package tws.vivien.api;

import io.javalin.http.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tws.vivien.core.*;
import tws.vivien.dto.FileLockRequest;
import tws.vivien.dto.FileLockResponse;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Objects;

@Singleton
public class FileLockApi implements Api
{
	private static final Logger LOG = LoggerFactory.getLogger(FileLockApi.class);

	@Inject public LockService lockService;
	@Inject public ErrorBacklog errorBacklog;

	@Inject public FileLockApi() {}

	@Override
	public void handle(Context ctx)
	{
		var request = ctx.bodyAsClass(FileLockRequest.class);
		if (request.file == null || request.user == null) return;

		var lockedByUser = lockService.fileLocks.get(request.file);
		if (request.lock)
		{
			if (lockedByUser == null || Objects.equals(request.user, lockedByUser) || Objects.equals(request.kickUser, lockedByUser))
			{
				lockService.lockFile(request.file, request.user);
				ctx.json(new FileLockResponse(true, lockedByUser));
			}
			else
			{
				ctx.json(new FileLockResponse(false, lockedByUser));
			}
		}
		else
		{
			if (lockedByUser == null || Objects.equals(lockedByUser, request.user))
			{
				lockService.freeUserLocks(request.user);
				ctx.json(new FileLockResponse(true, null));
			}
			else
			{
				ctx.json(new FileLockResponse(false, lockedByUser));
			}
		}
	}
}
