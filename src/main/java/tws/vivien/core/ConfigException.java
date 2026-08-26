package tws.vivien.core;

public class ConfigException extends Exception
{
	public ConfigException(String configName, String value)
	{
		this(configName, value, null);
	}

	public ConfigException(String configName, String value, Exception cause)
	{
		super("Ungültiger Wert '" + value + "' für Parameter: " + configName, cause);
	}

	public ConfigException(String configName)
	{
		this(configName, (Exception)null);
	}

	public ConfigException(String configName, Exception cause)
	{
		super("Parameter '" + configName + "' ist nicht gesetzt.", cause);
	}
}
