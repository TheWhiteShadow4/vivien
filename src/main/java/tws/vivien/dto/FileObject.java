package tws.vivien.dto;

import jakarta.annotation.Nullable;

import java.io.Serializable;
import java.util.Map;

public class FileObject
{
	public String url;
	public String filename;
	public FileObjectMeta metadata;
	public Map<String, Object> fileParams;
	
	public FileObject(String url, String filename, FileObjectMeta metadata)
	{
		this.url = url;
		this.filename = filename;
		this.metadata = metadata;
	}

	public static class FileObjectMeta implements Serializable
	{
		public String mimeType;
		public int size;
		public int width;
		public int height;
		public int srcWidth;
		public int srcHeight;
		@Nullable
		public String lockHolder;
		@Nullable
		public String additional;
	}
}
