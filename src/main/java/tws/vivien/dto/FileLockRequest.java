package tws.vivien.dto;

import jakarta.annotation.Nullable;

public class FileLockRequest
{
	public String user;
	public String file;
	public boolean lock;
	@Nullable
	public String kickUser;
}
