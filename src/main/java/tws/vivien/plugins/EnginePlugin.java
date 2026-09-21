package tws.vivien.plugins;

import tws.vivien.dto.TypedData;

import java.nio.file.Path;
import java.util.Map;

public interface EnginePlugin
{
	Map<String, TypedData> getImportData(Path file);

	boolean setImportData(Path file, Map<String, TypedData> settings);

	void createImportData(Path file);
}
