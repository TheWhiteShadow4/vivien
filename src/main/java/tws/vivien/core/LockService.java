package tws.vivien.core;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Singleton
public class LockService
{
	// Lock um Git Operationen(write) gegenüber kleine Datei Operationen(read) abzusichern.
	public ReentrantReadWriteLock gitLock;
	// Um Dateien zum Bearbeiten zu Sperren.
	public Map<String, String> fileLocks;

	@Inject
	public LockService()
	{
		gitLock = new ReentrantReadWriteLock();
		fileLocks = new ConcurrentHashMap<>();
	}

	public void lockFile(String path, String user)
	{
		freeUserLocks(user);
		fileLocks.put(path, user);
	}

	public void freeUserLocks(String user)
	{
		fileLocks.values().removeIf(value -> value.equals(user));
	}
}
