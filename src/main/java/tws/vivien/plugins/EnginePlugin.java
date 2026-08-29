package tws.vivien.plugins;

import java.nio.file.Path;
import java.util.Map;

public interface EnginePlugin
{
	Map<String, Object> getImportData(Path file);
}
