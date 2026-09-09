package tws.vivien.core;

import dagger.Module;
import dagger.Provides;
import dagger.multibindings.IntoMap;
import dagger.multibindings.StringKey;
import tws.vivien.handlers.IHandler;
import tws.vivien.handlers.ImageHandler;
import tws.vivien.handlers.TextHandler;

@Module
public class PreviewModule
{
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
	@StringKey("tif")
	public IHandler provideTifHandler(ImageHandler handler) { return handler; }

	@Provides
	@IntoMap
	@StringKey("tga")
	public IHandler provideTgaHandler(ImageHandler handler) { return handler; }

	@Provides
	@IntoMap
	@StringKey("txt")
	public IHandler provideTxtHandler(TextHandler handler) { return handler; }

	@Provides
	@IntoMap
	@StringKey("md")
	public IHandler provideMdHandler(TextHandler handler) { return handler; }

	@Provides
	@IntoMap
	@StringKey("json")
	public IHandler provideJsonHandler(TextHandler handler) { return handler; }

	@Provides
	@IntoMap
	@StringKey("yaml")
	public IHandler provideYamlHandler(TextHandler handler) { return handler; }

	@Provides
	@IntoMap
	@StringKey("xml")
	public IHandler provideXmlHandler(TextHandler handler) { return handler; }

	@Provides
	@IntoMap
	@StringKey("html")
	public IHandler provideHtmlHandler(TextHandler handler) { return handler; }
}
