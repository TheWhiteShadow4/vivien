package tws.vivien.dto;

public class LoginRequest
{
	// Die Feldnamen müssen exakt zu den JSON-Keys passen, die Vue sendet
	public String username;
	public String password;

	// Ein leerer Konstruktor ist für Jackson zwingend erforderlich
	public LoginRequest() {}

	public LoginRequest(String username, String password)
	{
		this.username = username;
		this.password = password;
	}
}