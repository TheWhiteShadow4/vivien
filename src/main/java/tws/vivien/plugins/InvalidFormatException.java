package tws.vivien.plugins;

public class InvalidFormatException extends Exception
{
	public InvalidFormatException(String message)
	{
		super(message);
	}

	public InvalidFormatException(String message, Throwable cause)
	{
		super(message, cause);
	}
}
