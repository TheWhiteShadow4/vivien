package tws.vivien.test;

import org.apache.commons.io.FileUtils;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.UUID;

public class GitTestUtils
{
	public static void modifyFile(Path file) throws IOException
	{
		String content = String.format("{ %s }", UUID.randomUUID());
		FileUtils.writeStringToFile(file.toFile(), content, Charset.defaultCharset());
	}
}
