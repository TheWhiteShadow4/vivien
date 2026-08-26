package tws.vivien.dto;

public class ServerError
{
	public String message;
	public String stacktrace;

	public ServerError(String message, String stacktrace)
	{
		this.message = message;
		this.stacktrace = stacktrace;
	}
}
