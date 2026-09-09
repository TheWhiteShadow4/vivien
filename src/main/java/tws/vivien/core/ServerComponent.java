package tws.vivien.core;

import dagger.Component;
import tws.vivien.api.*;

import javax.inject.Singleton;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Singleton
@Component(modules = {DependencyModule.class, PreviewModule.class})
public interface ServerComponent
{
	Config config();
	// Lock um Git Operationen(write) gegenüber kleine Datei Operationen(read) abzusichern.
	ReentrantReadWriteLock gitLock();
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
}
