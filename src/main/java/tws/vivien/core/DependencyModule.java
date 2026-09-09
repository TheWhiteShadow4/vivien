package tws.vivien.core;


import dagger.Module;
import dagger.Provides;
import dagger.multibindings.IntoMap;
import dagger.multibindings.StringKey;
import tws.vivien.handlers.IHandler;
import tws.vivien.handlers.ImageHandler;
import tws.vivien.handlers.TextHandler;

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
	//@Named("gitLock")
	public ReentrantReadWriteLock gitLock()
	{
		return new ReentrantReadWriteLock();
	}

	@Provides
	@IntoMap
	@StringKey("png")
	public IHandler providePngHandler(ImageHandler handler) { return handler; }

	@Provides
	@IntoMap
	@StringKey("jpg")
	public IHandler provideJpgHandler(ImageHandler handler) { return handler; }

	@Provides
	@IntoMap
	@StringKey("txt")
	public IHandler provideTxtHandler(TextHandler handler) { return handler; }
}
