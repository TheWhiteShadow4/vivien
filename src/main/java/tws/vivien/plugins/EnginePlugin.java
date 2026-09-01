package tws.vivien.plugins;

import tws.vivien.dto.ImportSetting;

import java.nio.file.Path;
import java.util.List;

public interface EnginePlugin
{
	List<ImportSetting> getImportData(Path file);

	boolean setImportData(Path file, List<ImportSetting> settings);
}
