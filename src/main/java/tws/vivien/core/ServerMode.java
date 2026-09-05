package tws.vivien.core;

public enum ServerMode
{
	LOCAL,
	HOSTED,
	SETUP,
	SAFE;

	public static ServerMode fromString(String value)
	{
		return ServerMode.valueOf(value.trim().toUpperCase());
	}
}
