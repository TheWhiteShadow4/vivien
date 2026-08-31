package tws.vivien.dto;

import jakarta.annotation.Nullable;

/// Client seitiger Benutzer
public class UserSettings
{
	// Git User
	@Nullable public String username;
	@Nullable public String email;

	public String view;
	public boolean sidebar;
}
