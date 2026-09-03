package tws.vivien.plugins;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import tws.vivien.dto.ImportSetting;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class Unity implements EnginePlugin
{
	static String IMPORT_PATTERN = ".meta";
	static String IMPORT_TEMPLATE = "/unity-meta.temp";

	private final ObjectMapper yamlMapper;

	public Unity()
	{
		YAMLFactory yamlFactory = new YAMLFactory();
		this.yamlMapper = new ObjectMapper(yamlFactory);
		// Den ObjectMapper konfigurieren
		yamlMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
	}

	@Override
	public Map<String, ImportSetting> getImportData(Path file)
	{
		Path importFile = file.getParent().resolve(file.getFileName().toString() + IMPORT_PATTERN);
		if (!Files.exists(importFile)) return null;
		try
		{
			Map<String, ImportSetting> settings = new HashMap<>();

			JsonNode rootNode = yamlMapper.readTree(importFile.toFile());

			if (rootNode.has("TextureImporter"))
			{
				JsonNode textureImporter = rootNode.get("TextureImporter");
				int textureType = textureImporter.path("textureType").asInt();
				int maxTextureSize = textureImporter.path("maxTextureSize").asInt();
				int compressionQuality = textureImporter.path("compressionQuality").asInt();

				JsonNode mipmaps = textureImporter.get("mipmaps");
				int sRGBTexture = mipmaps.get("sRGBTexture").asInt();

				JsonNode textureSettings = textureImporter.get("textureSettings");
				int filterMode = textureSettings.get("filterMode").asInt();

				settings.put("textureType", new ImportSetting("Type", textureType));
				settings.put("maxTextureSize", new ImportSetting("Max Size", maxTextureSize));
				settings.put("compressionQuality", new ImportSetting("Compression", compressionQuality));
				settings.put("sRGBTexture", new ImportSetting("sRGB", sRGBTexture));
				settings.put("filterMode", new ImportSetting("Filter", filterMode));
			}
			return settings;
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}

		return null;
	}

	@Override
	public boolean setImportData(Path file, Map<String, ImportSetting> settings)
	{
		Path importFile = file.getParent().resolve(file.getFileName().toString() + IMPORT_PATTERN);
		if (!Files.exists(importFile)) return false;
		try
		{
			ObjectNode rootNode = (ObjectNode) yamlMapper.readTree(importFile.toFile());

			if (rootNode.has("TextureImporter"))
			{
				ObjectNode textureImporter = (ObjectNode) rootNode.get("TextureImporter");
				textureImporter.put("textureType", settings.get("textureType").intValue());
				textureImporter.put("maxTextureSize", settings.get("maxTextureSize").intValue());
				textureImporter.put("compressionQuality", settings.get("compressionQuality").intValue());

				ObjectNode mipmaps = (ObjectNode) textureImporter.get("mipmaps");
				mipmaps.put("sRGBTexture", settings.get("sRGBTexture").intValue());

				ObjectNode textureSettings = (ObjectNode) textureImporter.get("textureSettings");
				textureSettings.put("filterMode", settings.get("filterMode").intValue());
			}
			return true;
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}

		return false;
	}

	@Override
	public void createImportData(Path file)
	{
		Path importFile = file.getParent().resolve(file.getFileName().toString() + IMPORT_PATTERN);

		try
		{
			var input = Unity.class.getResourceAsStream(IMPORT_TEMPLATE);
			if (input == null) throw new NullPointerException();
			Files.copy(input, importFile);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
}
