package tws.vivien.core;


import dagger.Module;
import dagger.Provides;

import javax.inject.Singleton;
import java.io.IOException;
import java.util.concurrent.locks.ReentrantReadWriteLock;

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
	public Cache serverCache()
	{
		try
		{
			return new Cache(cachePath);
		}
		catch(IOException e) { throw  new RuntimeException(e); }
	}

	@Provides
	@Singleton
	public ReentrantReadWriteLock gitLock()
	{
		return new ReentrantReadWriteLock();
	}
}
