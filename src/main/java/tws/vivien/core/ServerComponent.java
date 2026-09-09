package tws.vivien.core;

import dagger.Component;
import tws.vivien.api.PreviewApi;
import tws.vivien.api.RepositoryApi;

import javax.inject.Singleton;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Singleton
@Component(modules = {DependencyModule.class})
public interface ServerComponent
{
	Config config();
	Repository repository();
	RepositoryApi repositoryApi();
	PreviewApi previewApi();
	Cache serverCache();
	ErrorBacklog errorBacklog();

	//@Named("gitLock")
	ReentrantReadWriteLock gitLock();
}
