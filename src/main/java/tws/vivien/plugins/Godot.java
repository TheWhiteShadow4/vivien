package tws.vivien.plugins;

import java.nio.file.Path;
import java.util.Map;

public class Godot implements EnginePlugin
{
	static String IMPORT_PATTERN = ".import";

	public Map<String, Object> getImportData(Path file)
	{
		Path importFile = file.getParent().resolve(file.getFileName().toString() + IMPORT_PATTERN);
		return null;
	}
}
