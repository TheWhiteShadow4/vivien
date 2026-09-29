package tws.vivien.dto;

import jakarta.annotation.Nullable;

public class LoginResult
{
	public ServerState state;
	@Nullable
	public RepositoryElement folder;
	public int selected;

	public LoginResult(ServerState state, @Nullable RepositoryElement folder, int selected)
	{
		this.state = state;
		this.folder = folder;
		this.selected = selected;
	}
}
