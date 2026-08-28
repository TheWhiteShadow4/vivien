package tws.vivien.dto;

public class FileObject
{
	public String url;
	public String hash;
	public String mimeType;
	public int size;
	public int width;
	public int height;
	
	public FileObject(String url, String hash, String mimeType)
	{
		this.url = url;
		this.hash = hash;
		this.mimeType = mimeType;
	}
}
