package tws.vivien.core;


import dagger.Module;
import dagger.Provides;

import javax.inject.Singleton;
import java.io.IOException;

@Module
public class DependencyModule
{
	private final String cachePath;

	public DependencyModule(String cachePath)
	{
		this.cachePath = cachePath;
	}

	@Provides
	@Singleton
	public Config serverConfig()
	{
		return new Config().load();
	}

	@Provides
	@Singleton
	public Repository serverRepository(Config config)
	{
		return new Repository().open(config);
	}

	@Provides
	@Singleton
	public Cache serverCache()
	{
		try
		{
			return new Cache(cachePath);
		}
		catch(IOException e) { throw  new RuntimeException(e); }
	}
}
