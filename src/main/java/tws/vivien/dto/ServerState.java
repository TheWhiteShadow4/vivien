package tws.vivien.dto;

import jakarta.annotation.Nullable;
import tws.vivien.core.ServerMode;

import java.util.List;

public class ServerState
{
	public String view;
	public ServerMode mode;
	@Nullable
	public ServerUser user;
	public List<ServerError> serverErrors;
}
