package tws.vivien.dto;

import java.util.Arrays;

public class ServerError
{
	public String message;
	public String stacktrace;

	public ServerError(String message, String stacktrace)
	{
		this.message = message;
		this.stacktrace = stacktrace;
	}

	public static ServerError fromError(Exception e)
	{
		return new ServerError(e.getMessage(),
			 String.join("\n",
				 Arrays.stream(e.getStackTrace())
					   .map(StackTraceElement::toString).toList())
		);
	}
}
