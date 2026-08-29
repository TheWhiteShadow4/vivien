package tws.vivien.dto;

import jakarta.annotation.Nullable;

import java.util.Map;

public class FileObject
{
	public String url;
	public String filename;
	public FileObjectMeta metadata;
	
	public FileObject(String url, String filename, FileObjectMeta metadata)
	{
		this.url = url;
		this.filename = filename;
		this.metadata = metadata;
	}

	public static class FileObjectMeta
	{
		public String mimeType;
		public int size;
		public int width;
		public int height;
		public int srcWidth;
		public int srcHeight;
		@Nullable
		public Map<String, Object> importProps;
	}
}
