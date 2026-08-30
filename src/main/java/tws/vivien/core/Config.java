package tws.vivien.core;


import com.electronwill.nightconfig.core.file.FileConfig;
import tws.vivien.plugins.EnginePlugin;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Function;

public class Config
{
	private static final String CONFIG_FILE_NAME = "vivien-server.toml";

	public ServerMode mode;
	public SecurityMode security;
	public String serverHost = "localhost";
	public int port = 8080;
	public Object cert = null;
	public String user = null;
	public String password = null;
	public Map<String, ConfigView> views = new HashMap<>();
	public Path repository;
	public EnginePlugin enginePlugin;

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
			FileConfig reader = FileConfig.of(configFile);
			reader.load();
			//TomlParseResult result = TomlParser.parse(configFile.toPath());
			/*if (reader.hasErrors())
			{
				System.err.println("❌ FEHLER beim Parsen der " + CONFIG_FILE_NAME + ": " + result.errors());
				initSafeConfig();
			}*/
			readConfig(reader);
		}
		catch (Exception e)
		{
			System.out.println("⚠ Fehlerhafte Konfiguration (" + e.getMessage() + "). Wechsle in SETUP-Modus.");
			initSafeConfig();
		}
	}

	public ConfigView getView(String name)
	{
		ConfigView view = views.get(name);
		if (view == null)
		{
			System.err.println("View "+name+" ist nicht in der Server Konfig.");
			return new ConfigView("Admin");
		}
		return view;
	}

	private void readConfig(FileConfig config)
	{
		mode = CReader.readString(this, config, "mode")
					  .map(ServerMode::fromString).withDefault(ServerMode.LOCAL).get();

		repository = CReader.readString(this, config, "repo_path").required("")
							.map(Path::of).get();

		var defaultSecurity = mode == ServerMode.HOSTED ? SecurityMode.STRICT : SecurityMode.LAX;
		security = CReader.readString(this, config, "server.security")
						  .map(SecurityMode::fromString).withDefault(defaultSecurity).get();

		serverHost = CReader.readString(this, config, "server.host").withDefault(serverHost).get();
		port = CReader.readInt(this, config, "server.port").withDefault(port).get();

		user = CReader.readString(this, config, "server.user").get();
		password = CReader.readString(this, config, "server.password").get();

		loadEnginePlugin(config);

		loadViews(config);

		//errors.add(new ConfigException("waifu", "pantsu", null));

		validateRepository(repository);
		IO.println("Repository Pfad: " + repository);
	}

	private void loadViews(FileConfig config)
	{
		var table = (com.electronwill.nightconfig.core.Config) config.get("views");
		if (table != null)
		{
			for(var entry : table.entrySet())
			{
				try
				{
					String name = entry.getKey();
					var viewTable = (com.electronwill.nightconfig.core.Config) entry.getValue();
					String displayName = (String)viewTable.getOptional("name").orElse(name);
					List<String> includes = viewTable.get("includes");
					List<String> excludes = viewTable.get("excludes");

					views.put(name, new ConfigView(displayName, includes, excludes));

				}
				catch (Exception e)
				{
					errors.add(new ConfigException(entry.getKey(), e));
				}
			}
			IO.println(views);
		}
	}

	private void loadEnginePlugin(FileConfig config)
	{
		String cls = CReader.readString(this, config, "engine_plugin").get();
		if (cls != null)
		{
			if (!cls.contains("."))
			{
				cls = "tws.vivien.plugins." + cls;
			}
			try
			{
				enginePlugin = (EnginePlugin) Class.forName(cls).getConstructor().newInstance();
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
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

		public static CReader<String, String> readString(Config config, FileConfig toml, String configName)
		{
			CReader<String, String> reader = new CReader<>();
			reader.config = config;
			reader.configName = configName;
			reader.inputValue = toml.get(configName);
			reader.value = reader.inputValue;
			return reader;
		}

		public static CReader<Integer, Integer> readInt(Config config, FileConfig toml, String configName)
		{
			CReader<Integer, Integer> reader = new CReader<>();
			reader.config = config;
			reader.configName = configName;
			try
			{
				reader.inputValue = toml.get(configName);
			}
			catch(Exception e)
			{
				e.printStackTrace();
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
				value = fallbackValue;
			}

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
				if (error == null)
				{
					e.printStackTrace();
					error = new ConfigException(configName, Objects.toString(inputValue), e);
				}
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
