package tws.vivien.dto;

import jakarta.annotation.Nullable;

public class LoginResult
{
	public ServerState state;
	@Nullable
	public RepositoryElement element;

	public LoginResult(ServerState state, @Nullable RepositoryElement element)
	{
		this.state = state;
		this.element = element;
	}
}
