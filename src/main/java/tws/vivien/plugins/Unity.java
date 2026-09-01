package tws.vivien.plugins;

import tws.vivien.dto.ImportSetting;

import java.nio.file.Path;
import java.util.List;

public class Unity implements EnginePlugin
{
	static String IMPORT_PATTERN = ".meta";

	@Override
	public List<ImportSetting> getImportData(Path file)
	{
		Path importFile = file.getParent().resolve(file.getFileName().toString() + IMPORT_PATTERN);



		return null;
	}

	@Override
	public boolean setImportData(Path file, List<ImportSetting> settings)
	{
		return false;
	}
}
