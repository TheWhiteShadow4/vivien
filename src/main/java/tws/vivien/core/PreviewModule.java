package tws.vivien.core;

import dagger.Module;
import dagger.Provides;
import dagger.multibindings.IntoMap;
import dagger.multibindings.StringKey;
import tws.vivien.handlers.*;

/**
 * Liste der Preview Handler pro Dateiendung.
 * Muss mit der Liste in <code>frontend/src/config.ts</code> übereinstimmen.
 */
@Module
public class PreviewModule
{
	@Provides
	@IntoMap
	@StringKey("toml")
	public IHandler provideTomlHandler(TextHandler handler) { return handler; }

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

	@Provides
	@IntoMap
	@StringKey("fbx")
	public IHandler provideFBXHandler(ModelHandler handler) { return handler; }

	@Provides
	@IntoMap
	@StringKey("gltf")
	public IHandler provideGltfHandler(ModelHandler handler) { return handler; }

	@Provides
	@IntoMap
	@StringKey("glb")
	public IHandler provideGlbHandler(ModelHandler handler) { return handler; }

	@Provides
	@IntoMap
	@StringKey("obj")
	public IHandler provideObjHandler(ModelHandler handler) { return handler; }

	@Provides
	@IntoMap
	@StringKey("wav")
	public IHandler provideWavHandler(AudioHandler handler) { return handler; }

	@Provides
	@IntoMap
	@StringKey("mp3")
	public IHandler provideMp3Handler(AudioHandler handler) { return handler; }

	@Provides
	@IntoMap
	@StringKey("ogg")
	public IHandler provideOggHandler(AudioHandler handler) { return handler; }

	@Provides
	@IntoMap
	@StringKey("aac")
	public IHandler provideAacHandler(AudioHandler handler) { return handler; }
}
