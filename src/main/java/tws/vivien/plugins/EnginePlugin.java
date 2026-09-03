package tws.vivien.plugins;

import tws.vivien.dto.ImportSetting;

import java.nio.file.Path;
import java.util.Map;

public interface EnginePlugin
{
	Map<String, ImportSetting> getImportData(Path file);

	boolean setImportData(Path file, Map<String, ImportSetting> settings);

	void createImportData(Path file);
}
