package tws.vivien.plugins;

import tws.vivien.dto.TypedData;

import java.nio.file.Path;
import java.util.List;

public interface EnginePlugin
{
	List<TypedData> getImportData(Path file);

	boolean setImportData(Path file, List<TypedData> settings);

	void createImportData(Path file);
}
