package tws.vivien.core;

import tws.vivien.handlers.IHandler;
import tws.vivien.handlers.ImageHandler;
import tws.vivien.handlers.TextHandler;

import java.util.HashMap;
import java.util.Map;

public class PreviewGenerator
{
	private static final Map<String, Class<? extends IHandler>> handler;

	static
	{
		handler = new HashMap<>();
		handler.put("png", ImageHandler.class);
		handler.put("jpg", ImageHandler.class);
		//handler.put("gif", ImageHandler.class);
		handler.put("tif", ImageHandler.class);
		handler.put("tga", ImageHandler.class);
		handler.put("txt", TextHandler.class);
		handler.put("md", TextHandler.class);
		handler.put("json", TextHandler.class);
		handler.put("yaml", TextHandler.class);
		handler.put("xml", TextHandler.class);
		handler.put("html", TextHandler.class);
	}

	public static IHandler forFile(String file)
	{
		String fileExt = file.substring(file.lastIndexOf(".")+1).toLowerCase();
		try
		{
			var fileHandler = handler.get(fileExt);
			if (fileHandler != null)
			{
				return fileHandler.getConstructor().newInstance();
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return null;
	}
}
