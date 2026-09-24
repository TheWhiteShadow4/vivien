package tws.vivien.plugins;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tws.vivien.dto.TypedData;

import java.io.FileOutputStream;
import java.io.IOException;
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
	static String IMPORT_TEMPLATE = "/unity-texture-meta.temp";

	static int SUPPORTED_TEXTURE_FORMAT = 13;
	static int SUPPORTED_MODEL_FORMAT = 24200;

	private final ObjectMapper yamlMapper;

	public Unity()
	{
		YAMLFactory yamlFactory = new YAMLFactory();
		this.yamlMapper = new ObjectMapper(yamlFactory);
	}

	@Override
	public List<TypedData> getImportData(Path file)
	{
		String filename = file.getFileName().toString();
		AssetType type = getAssetType(filename);
		if (type == AssetType.OTHER) return null;

		Path importFile = file.getParent().resolve(filename + IMPORT_PATTERN);
		if (!Files.exists(importFile)) return null;
		try
		{
			if (type == AssetType.TEXTURE)
				return getTextureSettings(importFile);
			else
				return getModelSettings(importFile);
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
		String filename = file.getFileName().toString();
		AssetType type = getAssetType(filename);
		if (type == AssetType.OTHER) return false;

		Path importFile = file.getParent().resolve(filename + IMPORT_PATTERN);
		if (!Files.exists(importFile)) return false;

		LOG.info("import {}", settings);
		try
		{
			if (type == AssetType.TEXTURE)
				setTextureSettings(importFile, settings);
			else
				setModelSettings(importFile, settings);
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
			LOG.error("Fehler in Unity Plugin", e);
		}
	}

	private List<TypedData> getTextureSettings(Path importFile) throws Exception
	{
		List<TypedData> settings = new ArrayList<>();

		ObjectNode rootNode = (ObjectNode) yamlMapper.readTree(importFile.toFile());
		var textureImporter = verifySettingsType(rootNode, "TextureImporter", SUPPORTED_TEXTURE_FORMAT);

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
		settings.add(TypedData.asInt("filterMode","Filter", filterMode)
							   .withOptions(List.of("Point", "Bilinear", "Trilinear")));

		LOG.info("export {}", settings);

		return settings;
	}

	private void setTextureSettings(Path importFile, List<TypedData> settings) throws Exception
	{
		Map<String, TypedData> map = convertToMap(settings);

		ObjectNode rootNode = (ObjectNode) yamlMapper.readTree(importFile.toFile());
		var textureImporter = verifySettingsType(rootNode, "TextureImporter", SUPPORTED_TEXTURE_FORMAT);

		textureImporter.put("textureType", map.get("textureType").intValue());

		if (textureImporter.has("platformSettings"))
		{
			ObjectNode defaultPlatform = (ObjectNode) textureImporter.get("platformSettings").get(0);
			defaultPlatform.put("maxTextureSize", map.get("maxTextureSize").intValue());
			defaultPlatform.put("textureCompressions", map.get("textureCompression").intValue());
			defaultPlatform.put("compressionQuality", map.get("compressionQuality").intValue());
		}

		ObjectNode mipmaps = (ObjectNode) textureImporter.get("mipmaps");
		mipmaps.put("sRGBTexture", map.get("sRGBTexture").intValue());

		ObjectNode textureSettings = (ObjectNode) textureImporter.get("textureSettings");
		textureSettings.put("filterMode", map.get("filterMode").intValue());

		writeSettings(importFile, rootNode);
	}

	private List<TypedData> getModelSettings(Path importFile) throws Exception
	{
		List<TypedData> settings = new ArrayList<>();
		ObjectNode rootNode = (ObjectNode) yamlMapper.readTree(importFile.toFile());

		var modelImporter = verifySettingsType(rootNode, "ModelImporter", SUPPORTED_MODEL_FORMAT);
		JsonNode meshes = modelImporter.get("meshes");

		settings.add(TypedData.asFloat("globalScale", "Scale", meshes.get("globalScale").floatValue()));
		settings.add(TypedData.asBool("useFileUnits", "Use File Units", meshes.get("useFileUnits").intValue()));
		settings.add(TypedData.asInt("meshCompression", "Compression", meshes.get("meshCompression").intValue()));
		settings.add(TypedData.asBool("swapUVChannels", "Swap UVs", meshes.get("swapUVChannels").intValue()));
		settings.add(TypedData.asBool("keepQuads", "Keep Quads", meshes.get("keepQuads").intValue()));
		settings.add(TypedData.asBool("weldVertices", "Weld Vertices", meshes.get("weldVertices").intValue()));
		settings.add(TypedData.asBool("generateMeshLods", "LODs", meshes.get("generateMeshLods").intValue()));
		settings.add(TypedData.asInt("maximumMeshLod", "Max LODs", meshes.get("maximumMeshLod").intValue()));

		settings.add(TypedData.asBool("importAnimation", "Scale", modelImporter.get("importAnimation").intValue()));
		//JsonNode animations = modelImporter.get("animations");

		return settings;
	}

	private void setModelSettings(Path importFile, List<TypedData> settings) throws Exception
	{
		Map<String, TypedData> map = convertToMap(settings);

		ObjectNode rootNode = (ObjectNode) yamlMapper.readTree(importFile.toFile());

		var modelImporter = verifySettingsType(rootNode, "ModelImporter", SUPPORTED_MODEL_FORMAT);
		ObjectNode meshNode = (ObjectNode) modelImporter.get("meshes");

		meshNode.put("globalScale", map.get("globalScale").floatValue());
		meshNode.put("useFileUnits", map.get("useFileUnits").intValue());
		meshNode.put("meshCompression", map.get("meshCompression").intValue());
		meshNode.put("swapUVChannels", map.get("swapUVChannels").intValue());
		meshNode.put("keepQuads", map.get("keepQuads").intValue());
		meshNode.put("weldVertices", map.get("weldVertices").intValue());
		meshNode.put("generateMeshLods", map.get("generateMeshLods").intValue());
		meshNode.put("maximumMeshLod", map.get("maximumMeshLod").intValue());

		modelImporter.put("importAnimation", map.get("importAnimation").intValue());
		//JsonNode animations = modelImporter.get("animations");

		writeSettings(importFile, rootNode);
	}

	private ObjectNode verifySettingsType(ObjectNode rootNode, String importerName, int version) throws InvalidFormatException
	{
		ObjectNode importer = (ObjectNode) rootNode.get(importerName);
		if (importer == null) throw new InvalidFormatException("Ungültiges Asset Format");
		if (importer.get("serializedVersion").asInt() != version)
			throw new InvalidFormatException("Ungültiges Asset Format");
		return importer;
	}

	private Map<String, TypedData> convertToMap(List<TypedData> settings)
	{
		return settings.stream().collect(Collectors.toMap(TypedData::key, d -> d));
	}

	private void writeSettings(Path importFile, ObjectNode rootNode) throws IOException
	{
		try(FileOutputStream out = new FileOutputStream(importFile.toFile()))
		{
			System.out.println(yamlMapper.writerWithDefaultPrettyPrinter());
			var writer = yamlMapper.writerWithDefaultPrettyPrinter().writeValues(out);
			writer.write(rootNode);
		}
	}

	private AssetType getAssetType(String filename)
	{
		return switch (FilenameUtils.getExtension(filename))
		{
			case "png", "jpg", "jpeg", "tif", "tiff", "tga" -> AssetType.TEXTURE;
			case "fbx", "glb", "gltf", "obj" -> AssetType.MODEL;
			default -> AssetType.OTHER;
		};
	}

	private enum AssetType
	{
		TEXTURE,
		MODEL,
		OTHER
	}
}
