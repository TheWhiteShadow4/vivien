package tws.vivien.core;

public enum SecurityMode
{
	STRICT,
	LAX;

	public static SecurityMode fromString(String value)
	{
		return SecurityMode.valueOf(value.trim().toUpperCase());
	}
}
