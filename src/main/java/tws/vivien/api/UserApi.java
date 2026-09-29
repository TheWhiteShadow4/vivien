package tws.vivien.api;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import io.javalin.http.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tws.vivien.core.Config;
import tws.vivien.core.Server;
import tws.vivien.dto.ServerError;
import tws.vivien.dto.UserRequest;

import javax.inject.Inject;
import java.time.Instant;

public class UserApi implements Api
{
	private static final Logger LOG = LoggerFactory.getLogger(UserApi.class);

	@Inject public Config config;

	@Inject public UserApi() {}

	@Override
	public void handle(Context ctx)
	{
		try
		{
			UserRequest request = ctx.bodyAsClass(UserRequest.class);

			int expires = 60 * 60 * 48;
			String token = JWT.create()
				  .withClaim("user", Server.getUserName(ctx)) // User kann nicht geändert werden.
				  .withClaim("view", request.view)
				  .withExpiresAt(Instant.now().plusSeconds(expires))
				  .sign(Algorithm.HMAC256(config.secret));

			ctx.header("Set-Cookie", "auth_token=" + token + "; HttpOnly; SameSite=Strict; Path=/; Max-Age=" + expires);
		}
		catch(Exception e)
		{
			LOG.error("Request fehlgeschlagen", e);
			ctx.status(500);
			ctx.json(ServerError.fromError(e));
		}
	}
}
