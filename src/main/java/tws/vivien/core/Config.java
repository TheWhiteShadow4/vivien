package tws.vivien.core;


import com.electronwill.nightconfig.core.file.FileConfig;
import org.eclipse.jgit.merge.MergeStrategy;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tws.vivien.plugins.EnginePlugin;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Function;

@Singleton
public class Config
{
	private static final Logger LOG = LoggerFactory.getLogger(Config.class);

	private static final String CONFIG_FILE_NAME = "vivien-server.toml";

	public Path webRoot;
	public ServerMode mode;
	public SecurityMode security;
	public String serverHost = "localhost";
	public int port = 8080;
	public Object cert = null;
	public String user = null;
	public String password = null;
	public List<String> validFileformats;
	public List<String> validUsers;

	public String previewFormat;
	public float previewCompression;

	public String gitRemote = null;
	public String gitBranch = null;
	public MergeStrategy mergeStrategy;
	public UsernamePasswordCredentialsProvider credentials;
	public Map<String, ConfigView> views = new HashMap<>();
	public Path repository;
	public EnginePlugin enginePlugin;

	public List<ConfigException> errors = new ArrayList<>();

	@Inject
	public Config()
	{
		webRoot = Paths.get(".").toAbsolutePath();
		File configFile = new File(CONFIG_FILE_NAME);

		if (!configFile.exists()) {
			System.out.println("⚠ Keine Konfigurationsdatei gefunden. Wechsle in SETUP-Modus.");
			initSetupConfig();
		}

		try
		{
			FileConfig reader = FileConfig.of(configFile);
			reader.load();
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

		gitRemote = CReader.readString(this, config, "git.remote").get();
		gitBranch = CReader.readString(this, config, "git.branch").get();
		mergeStrategy = CReader.readString(this, config, "git.resolve").map(this::mapMergeStrategy).withDefault(MergeStrategy.OURS).get();

		var defaultSecurity = mode == ServerMode.HOSTED ? SecurityMode.STRICT : SecurityMode.LAX;
		security = CReader.readString(this, config, "server.security")
						  .map(SecurityMode::fromString).withDefault(defaultSecurity).get();

		serverHost = CReader.readString(this, config, "server.host").withDefault(serverHost).get();
		port = CReader.<Integer>read(this, config, "server.port").withDefault(port).get();

		user = CReader.readString(this, config, "server.user").get();
		password = CReader.readString(this, config, "server.password").get();

		previewFormat = CReader.readString(this, config, "preview.format").get();
		previewCompression = CReader.<Double>read(this, config, "preview.compression")
				.map(Double::floatValue).withDefault(0.5f).get();

		validFileformats = CReader.readString(this, config, "server.formats")
				.map(s -> Arrays.stream(s.split(",")).map(String::trim).toList()).get();



		var gitToken = CReader.readString(this, config, "git.token").get();
		if (gitToken != null)
		{
			//var gitUser = CReader.readString(this, config, "git.user").get();
			credentials = new UsernamePasswordCredentialsProvider("access_token", gitToken);
		}

		loadUsers(config);
		loadViews(config);

		loadEnginePlugin(config);
		validateRepository(repository);
	}

	private MergeStrategy mapMergeStrategy(String value)
	{
		return switch (value.toUpperCase())
		{
			case "OURS" -> MergeStrategy.OURS;
			case "THEIRS" -> MergeStrategy.THEIRS;
			case "RESOLVE" -> MergeStrategy.RESOLVE;
			default ->
			{
				errors.add(new ConfigException("git.resolve", value));
				yield MergeStrategy.OURS;
			}
		};
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
					String root = (String)viewTable.getOptional("root").orElse("");
					List<String> includes = viewTable.get("includes");
					List<String> excludes = viewTable.get("excludes");

					views.put(name, new ConfigView(displayName, root, includes, excludes));

				}
				catch (Exception e)
				{
					errors.add(new ConfigException(entry.getKey(), e));
				}
			}
		}
	}

	private void loadUsers(FileConfig config)
	{
		try
		{
			validUsers = config.get("server.users");
		}
		catch (Exception e)
		{
			errors.add(new ConfigException("server.users", e));
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
				LOG.error("Engine Plugin nicht geladen", e);
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
			throw new RuntimeException("Repository Pfad '" + repository + "' nicht gefunden.");
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

		public static <S> CReader<S, S> read(Config config, FileConfig toml, String configName)
		{
			CReader<S, S> reader = new CReader<>();
			reader.config = config;
			reader.configName = configName;
			try
			{
				reader.inputValue = toml.get(configName);
			}
			catch(Exception e)
			{
				LOG.error("Ungültiger Parameter {}", configName, e);
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
			if (value != null)
			{
				try
				{
					mappedValue = func.apply(value);
				}
				catch (Exception e)
				{
					if (error == null)
					{
						LOG.error("Ungültiger Parameter {}", configName, e);
						error = new ConfigException(configName, Objects.toString(inputValue), e);
					}
				}
			}
			var result = new CReader<S, R>();
			result.config = config;
			result.configName = configName;
			result.inputValue = inputValue;
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
