package tws.vivien.core;

import dagger.Component;
import tws.vivien.api.*;

import javax.inject.Singleton;

/**
 * Das hier ist die Registry für alle Dependency Injection Klassen, die über Dagger verwaltet werden.
 * Die Module stellen Methoden zur Objekt-Erzeugung bereit, wenn der Default Konstruktor nicht ausreicht.
 * Fast alle Klassen sind als Singleton ausgeführt, da sie Stateless arbeiten oder einen Shared State besitzen.
 */
@Singleton
@Component(modules = {DependencyModule.class, PreviewModule.class})
public interface ServerComponent
{
	Config config();
	LockService lockService();
	Repository repository();
	RepositoryApi repositoryApi();
	PreviewApi previewApi();
	Cache serverCache();
	ErrorBacklog errorBacklog();
	ServerStateApi serverStateApi();
	GitStatusApi gitStaturApi();
	DownloadApi downloadApi();
	DeleteApi deleteApi();
	StageApi stageApi();
	CheckoutApi checkoutApi();
	ResetApi resetApi();
	CommitApi commitApi();
	PullApi pullApi();
	StashApi stashApi();
	UnstashApi unstashApi();
	UploadApi uploadApi();
	CreateApi createApi();
	MoveApi moveApi();
	FileLockApi fileLockApi();
	PluginDataApi pluginDataApi();
}
