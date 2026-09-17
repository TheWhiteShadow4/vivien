package tws.vivien.test;

import dagger.Module;
import dagger.Provides;
import org.mockito.Mockito;
import tws.vivien.core.Cache;
import tws.vivien.core.Config;
import tws.vivien.core.Repository;

import javax.inject.Singleton;
import java.nio.file.Path;

@Module
public class TestDaggerModule
{
	private final Path repositoryPath;

	public TestDaggerModule(Path repositoryPath)
	{
		this.repositoryPath = repositoryPath;
	}

	@Provides
	@Singleton
	public Config getConfig()
	{
		Config config = new Config();
		config.repository = repositoryPath;
		return config;
	}

	@Provides
	@Singleton
	public Repository serverRepository(Config config)
	{
		return new Repository().create(config);
	}

	@Provides
	@Singleton
	public Cache serverCache()
	{
		return Mockito.mock(Cache.class);
	}
}
