package tws.vivien.core;

public enum SecurityMode
{
	STRICT,
	LAX;

	public static SecurityMode fromString(String value)
	{
		if (value == null) return null;
		return SecurityMode.valueOf(value.trim().toUpperCase());
	}
}
