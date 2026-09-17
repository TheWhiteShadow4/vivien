package tws.vivien.test;

import dagger.Component;
import tws.vivien.api.*;
import tws.vivien.core.*;

import javax.inject.Singleton;

@Singleton
@Component(modules = {TestDaggerModule.class})
public interface TestComponent
{
	Config config();
	LockService lockService();
	Repository repository();
	RepositoryApi repositoryApi();
	//PreviewApi previewApi();
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
}
