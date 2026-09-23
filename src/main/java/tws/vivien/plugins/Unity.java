package tws.vivien.plugins;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tws.vivien.dto.TypedData;

import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Unity implements EnginePlugin
{
	private static final Logger LOG = LoggerFactory.getLogger(Unity.class);
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
	public List<TypedData> getImportData(Path file)
	{
		Path importFile = file.getParent().resolve(file.getFileName().toString() + IMPORT_PATTERN);
		if (!Files.exists(importFile)) return null;
		try
		{
			List<TypedData> settings = new ArrayList<>();

			JsonNode rootNode = yamlMapper.readTree(importFile.toFile());

			if (rootNode.has("TextureImporter"))
			{
				JsonNode textureImporter = rootNode.get("TextureImporter");
				int textureType = textureImporter.path("textureType").asInt();

				JsonNode mipmaps = textureImporter.get("mipmaps");
				int sRGBTexture = mipmaps.get("sRGBTexture").asInt();

				JsonNode textureSettings = textureImporter.get("textureSettings");
				int filterMode = textureSettings.get("filterMode").asInt();

				JsonNode defaultPlatform = textureImporter.get("platformSettings").get(0);
				int maxTextureSize = defaultPlatform.path("maxTextureSize").asInt();
				int textureCompression = defaultPlatform.path("textureCompression").asInt();
				int compressionQuality = defaultPlatform.path("compressionQuality").asInt();

				settings.add(TypedData.asInt("textureType", "Type", textureType)
						.withOptions(List.of("Default", "Normal", "Editor GUI", "Cookie", "-", "-", "-Lightmap", "Cursor", "Sprite (2D)")));
				settings.add(TypedData.asEnum("maxTextureSize", "Max Size", maxTextureSize)
						.withOptions(List.of("32", "64", "128", "256", "512", "1024", "2048", "4096", "8192", "16384")));
				settings.add(TypedData.asInt("textureCompression", "Compression", textureCompression)
						.withOptions(List.of("None", "Normal", "High", "Low")));
				settings.add(TypedData.asInt("compressionQuality", "Compressor Quality", compressionQuality));
				settings.add(TypedData.asBool("sRGBTexture", "sRGB", sRGBTexture));
				settings.add( TypedData.asInt("filterMode","Filter", filterMode)
						.withOptions(List.of("Point", "Bilinear", "Trilinear")));
			}
			LOG.info("export {}", settings);

			return settings;
		}
		catch(Exception e)
		{
			LOG.error("Fehler in Unity Plugin", e);
		}

		return null;
	}

	@Override
	public boolean setImportData(Path file, List<TypedData> settings)
	{
		LOG.info("import {}", settings);

		Path importFile = file.getParent().resolve(file.getFileName().toString() + IMPORT_PATTERN);
		if (!Files.exists(importFile)) return false;
		try
		{
			Map<String, TypedData> settingsMap = settings.stream().collect(Collectors.toMap(TypedData::key, d -> d));

			ObjectNode rootNode = (ObjectNode) yamlMapper.readTree(importFile.toFile());

			if (rootNode.has("TextureImporter"))
			{
				ObjectNode textureImporter = (ObjectNode) rootNode.get("TextureImporter");
				textureImporter.put("textureType", settingsMap.get("textureType").intValue());

				if (textureImporter.has("platformSettings"))
				{
					ObjectNode defaultPlatform = (ObjectNode) textureImporter.get("platformSettings").get(0);
					defaultPlatform.put("maxTextureSize", settingsMap.get("maxTextureSize").intValue());
					defaultPlatform.put("textureCompressions", settingsMap.get("textureCompression").intValue());
					defaultPlatform.put("compressionQuality", settingsMap.get("compressionQuality").intValue());
				}

				ObjectNode mipmaps = (ObjectNode) textureImporter.get("mipmaps");
				mipmaps.put("sRGBTexture", settingsMap.get("sRGBTexture").intValue());

				ObjectNode textureSettings = (ObjectNode) textureImporter.get("textureSettings");
				textureSettings.put("filterMode", settingsMap.get("filterMode").intValue());

				try(FileOutputStream out = new FileOutputStream(importFile.toFile()))
				{
					var writer = yamlMapper.writerWithDefaultPrettyPrinter().writeValues(out);
					writer.write(rootNode);
				}
			}
			return true;
		}
		catch(Exception e)
		{
			LOG.error("Fehler in Unity Plugin", e);
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
