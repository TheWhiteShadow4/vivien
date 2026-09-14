package tws.vivien.dto;

import jakarta.annotation.Nullable;

public class FileLockResponse
{
	public boolean success;
	@Nullable
	public String lockHolder;

	public FileLockResponse(boolean success, @Nullable String lockHolder)
	{
		this.success = success;
		this.lockHolder = lockHolder;
	}
}
