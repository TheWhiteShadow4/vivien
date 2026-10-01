package tws.vivien.dto;

import jakarta.annotation.Nullable;

public class LoginRequest
{
	public String user;
	public String pass;
	public String view; // Der initiale User View
	@Nullable
	public String path; // Der Pfad, der nach dem Login aufgerufen wird
}
