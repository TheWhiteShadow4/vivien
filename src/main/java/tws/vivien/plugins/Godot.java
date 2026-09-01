package tws.vivien.plugins;

import org.apache.commons.configuration2.INIConfiguration;
import org.apache.commons.configuration2.ex.ConfigurationException;
import tws.vivien.dto.ImportSetting;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public class Godot implements EnginePlugin
{
	static String IMPORT_PATTERN = ".import";

	@Override
	public List<ImportSetting> getImportData(Path file)
	{
		Path importFile = file.getParent().resolve(file.getFileName().toString() + IMPORT_PATTERN);

		INIConfiguration ini = new INIConfiguration();
		try (FileReader reader = new FileReader(importFile.toFile()))
		{
			ini.read(reader);

			// Wert sicher auslesen (Sektion "params", Key "texture/filter")
			String filterValue = ini.getSection("params").getString("texture/filter");

			// Wert ändern und wieder mit Anführungszeichen versehen, da Godot das so erwartet
			ini.getSection("params").setProperty("texture/filter", "\"1\"");

		}
		catch(IOException | ConfigurationException e)
		{
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public boolean setImportData(Path file, List<ImportSetting> settings)
	{
		Path importFile = file.getParent().resolve(file.getFileName().toString() + IMPORT_PATTERN);

		INIConfiguration ini = new INIConfiguration();
		try (FileWriter writer = new FileWriter(importFile.toFile()))
		{

			ini.write(writer);
		}
		catch(IOException | ConfigurationException e)
		{
			e.printStackTrace();
		}

		return false;
	}
}
