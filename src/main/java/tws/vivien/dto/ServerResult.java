package tws.vivien.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;

@JsonSubTypes({
		@JsonSubTypes.Type(value = ServerResult.Success.class),
		@JsonSubTypes.Type(value = ServerResult.Failure.class)
})
public class ServerResult
{
	public boolean success;
	public String message;
	public ServerError error;

	public static ServerResult Success(String message)
	{
		var result = new ServerResult();
		result.success = true;
		result.message = message;
		return result;
	}

	public static ServerResult Failure(String errorMessage)
	{
		var result = new ServerResult();
		result.success = false;
		result.error = new ServerError(errorMessage, null);
		return result;
	}

	public static class Success
	{
		public True success;
		public String message; // Im Erfolgsfall NIEMALS null

		public Success(String message)
		{
			this.message = message;
		}
	}

	public static class Failure
	{
		public False success;
		public ServerError error; // Im Fehlerfall NIEMALS null

		public Failure(String errorMessage)
		{
			this.error = new ServerError(errorMessage, null);
		}
	}

	public static class True {}
	public static class False {}
}
