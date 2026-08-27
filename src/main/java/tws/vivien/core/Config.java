package tws.vivien.core;

import org.tomlj.Toml;
import org.tomlj.TomlInvalidTypeException;
import org.tomlj.TomlParseResult;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

public class Config
{
	private static final String CONFIG_FILE_NAME = "vivien-server.toml";

	public ServerMode mode;
	public SecurityMode security;
	public String serverHost;
	public int port;
	public Object cert = null;
	public String user = null;
	public String password = null;
	public Path repository;

	public List<ConfigException> errors = new ArrayList<>();

	public Config()
	{
		File configFile = new File(CONFIG_FILE_NAME);

		if (!configFile.exists()) {
			System.out.println("⚠ Keine Konfigurationsdatei gefunden. Wechsle in SETUP-Modus.");
			initSetupConfig();
		}

		try
		{
			TomlParseResult result = Toml.parse(configFile.toPath());
			if (result.hasErrors())
			{
				System.err.println("❌ FEHLER beim Parsen der " + CONFIG_FILE_NAME + ": " + result.errors());
				initSafeConfig();
			}
			readConfig(result);
		}
		catch (IOException e)
		{
			System.out.println("⚠ Fehlerhafte Konfiguration (" + e.getMessage() + "). Wechsle in SETUP-Modus.");
			initSafeConfig();
		}
	}

	private void readConfig(TomlParseResult config)
	{
		mode = CReader.readString(this, config, "mode")
					  .map(ServerMode::fromString).withDefault(ServerMode.LOCAL).get();

		repository = CReader.readString(this, config, "repo_path").required("")
							.map(Path::of).get();

		var defaultSecurity = mode == ServerMode.HOSTED ? SecurityMode.STRICT : SecurityMode.LAX;
		security = CReader.readString(this, config, "server.security")
						  .map(SecurityMode::fromString).withDefault(defaultSecurity).get();

		serverHost = CReader.readString(this, config, "server.host").withDefault("localhost").get();
		port = CReader.readLong(this, config, "server.port").map(Long::intValue).withDefault(8080).get();

		user = CReader.readString(this, config, "server.user").get();
		password = CReader.readString(this, config, "server.password").get();

		CReader.readLong(this, config, "waifu").map(Long::intValue).withDefault(8080).get();

		validateRepository(repository);
	}

	private void validateRepository(Path repository)
	{
		if (repository == null)
		{
			throw new RuntimeException("Repository Pfad ist null.");
		}
		if (!Files.isDirectory(repository))
		{
			throw new RuntimeException("Repository Pfad '" + repository.toString() + "' nicht gefunden.");
		}
	}

	private static class CReader<S, T>
	{
		private Config config;
		private String configName;
		private S inputValue;
		private ConfigException error;
		private T value;

		public static CReader<String, String> readString(Config config, TomlParseResult toml, String configName)
		{
			CReader<String, String> reader = new CReader<>();
			reader.config = config;
			reader.configName = configName;
			reader.inputValue = toml.getString(configName);
			reader.value = reader.inputValue;
			return reader;
		}

		public static CReader<Long, Long> readLong(Config config, TomlParseResult toml, String configName)
		{
			CReader<Long, Long> reader = new CReader<>();
			reader.config = config;
			reader.configName = configName;
			try
			{
				reader.inputValue = toml.getLong(configName);
			}
			catch(TomlInvalidTypeException e)
			{
				reader.error = new ConfigException(configName, Objects.toString(toml.get(configName)), e);
			}
			reader.value = reader.inputValue;
			return reader;
		}

		public CReader<S, T> withDefault(T defaultValue)
		{
			if (value == null) value = defaultValue;
			return this;
		}

		public CReader<S, T> required(T fallbackValue)
		{
			if (value == null)
			{
				error = new ConfigException(configName);
			}
			value = fallbackValue;
			return this;
		}

		public <R> CReader<S, R> map(Function<T, R> func)
		{
			R mappedValue = null;
			try
			{
				mappedValue = func.apply(value);
			}
			catch(Exception e)
			{
				if (error == null) error = new ConfigException(configName, inputValue.toString(), e);
			}
			@SuppressWarnings("unchecked")
			var result = (CReader<S, R>) this;
			result.value = mappedValue;
			return result;
		}

		public T get()
		{
			if (error != null)
			{
				config.errors.add(error);
			}
			return value;
		}
	}

	// Abgesicherter Modus
	private void initSafeConfig()
	{
		mode = ServerMode.SAFE;
	}

	// Konfiguration Setup
	private void initSetupConfig()
	{
		mode = ServerMode.SETUP;
	}
}
