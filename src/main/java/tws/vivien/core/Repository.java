package tws.vivien.core;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ResetCommand;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.BranchTrackingStatus;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.transport.FetchResult;
import org.eclipse.jgit.transport.RemoteConfig;
import tws.vivien.dto.*;

import java.io.Closeable;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class Repository implements Closeable
{
	private final Path rootPath;
	private final Git gitApi;
	private final RepositoryCache cache;
	private GitBranchStatus branchStatus;

	public Repository(Path rootPath) throws IOException, GitAPIException
	{
		this.rootPath = rootPath;
		this.gitApi = Git.open(rootPath.toFile());
		cache = new RepositoryCache(rootPath, gitApi.getRepository());
	}

	public Path getRoot() { return rootPath; }

	public Git getApi()
	{
		return gitApi;
	}

	public RepositoryElement getView(ConfigView view, String path) throws IOException
	{
		RepositoryElement element;

		// Reroot
		/*if (!view.root.isEmpty() && "/".equals(path))
		{
			element = cache.getDirectory(view.root).flatCopyWithChildren();
		}
		else*/
		{
			element = cache.getDirectory(path).flatCopyWithChildren();
		}
		// Filter
		if (element.children != null && !element.children.isEmpty())
		{
			element.children = element.children.stream().filter(e -> view.getFilter().isIncluded(Path.of(e.path), e.type)).toList();
		}
		return element;
	}

	public List<RepositoryElement> searchFiles(ConfigView view, String query)
	{
		String lowerQuery = query.toLowerCase();

		// Nutzt den performanten RAM-Lookup ohne Festplatten-I/O
		return cache.getPathLookup().values().stream()
				.filter(element -> element.type == ElementType.FILE) // Nur Dateien durchsuchen
				.filter(element -> element.name.toLowerCase().contains(lowerQuery))
				.filter(e -> view.getFilter().isIncluded(Path.of(e.path), e.type))
				.map(RepositoryElement::flatCopy)
				.toList();
	}

	public GitBranchStatus getBranchStatus() throws Exception
	{
		Status status = gitApi.status().call();

		var result = new GitBranchStatus();
		result.branch = gitApi.getRepository().getBranch();
		result.modified = status.hasUncommittedChanges();
		result.untracked = status.getUntracked();
		result.added = status.getAdded();
		result.changed = status.getChanged();
		result.removed = status.getRemoved();
		result.missing = status.getMissing();
		result.conflicts = status.getConflicting();
		this.branchStatus = result;
		return result;
	}

	public RemoteGitStatus getRemoteStatus(Config config, String branch) throws Exception
	{
		List<RemoteConfig> remotes = RemoteConfig.getAllRemoteConfigs(gitApi.getRepository().getConfig());
		if (remotes.isEmpty()) return null;

		FetchResult _result = gitApi.fetch().setCredentialsProvider(config.credentials).call();
		var trackingStatus = BranchTrackingStatus.of(gitApi.getRepository(), branch);

		if (trackingStatus == null) return null;

		// Commits, die dem Server fehlen und Commits, die noch nicht gepusht sind
		return new RemoteGitStatus(trackingStatus.getBehindCount(), trackingStatus.getAheadCount());
	}


	public void trackFile(Path file) throws Exception
	{
		gitApi.add().addFilepattern(getRelativePath(file)).call();
	}

	public void untrackFile(Path file) throws Exception
	{
		gitApi.reset().setRef(Constants.HEAD).addPath(getRelativePath(file)).call();
	}

	public void deleteFile(Path file) throws Exception
	{
		gitApi.rm().addFilepattern(getRelativePath(file)).call();
	}

	public void undeleteFile(Path file) throws Exception
	{
		gitApi.checkout()
		   .setStartPoint(Constants.HEAD)
		   .addPath(getRelativePath(file))
		   .call();
	}

	public void checkout(String branch) throws Exception
	{
		gitApi.checkout().setName(branch).call();
	}

	public void reset() throws Exception
	{
		gitApi.reset().setRef(Constants.HEAD).setMode(ResetCommand.ResetType.MIXED).call();
	}

	public void commit(CommitRequest request) throws Exception
	{
		if (request.name == null) throw new NullPointerException("name ist null");
		if (request.email == null) throw new NullPointerException("email ist null");
		if (request.message == null) throw new NullPointerException("message ist null");
		//if (stage == null || stage.isEmpty()) throw new NullPointerException("stage ist leer");

		/*var add = gitApi.add();
		var rm = gitApi.rm();

		for(String pathStr : stage.added)
		{
			add.addFilepattern(pathStr);
		}
		for(String pathStr : stage.removed)
		{
			rm.addFilepattern(pathStr);
		}
		if (!stage.added.isEmpty()) add.call();
		if (!stage.removed.isEmpty()) rm.call();*/

		gitApi.commit().setAuthor(request.name, request.email).setMessage(request.message).call();
	}

	public void push() throws Exception
	{
		gitApi.push().call();
	}

	public void fetch() throws Exception
	{
		gitApi.fetch().call();
	}

	public void stash() throws Exception
	{
		gitApi.stashCreate().call();
	}

	public void unstash() throws Exception
	{
		gitApi.stashApply().call();
	}

	/*public GitFileStatus getStatus(Path path) throws Exception
	{
		Status status = gitApi.status().addPath(path.toString()).call();
		if (!status.isClean())
			return GitFileStatus.Clean;
		if (!status.getUntracked().isEmpty())
			return GitFileStatus.Untracked;
		if (!status.getAdded().isEmpty())
			return GitFileStatus.Added;
		if (!status.getChanged().isEmpty())
			return GitFileStatus.Modified;
		if (!status.getConflicting().isEmpty())
			return GitFileStatus.Conflict;
		if (!status.getMissing().isEmpty())
			return GitFileStatus.Deleted;
		throw new Error("Unbekannter Git State");
	}*/

	@Override
	public void close()
	{
		gitApi.close();
	}

	public Path resolveFile(String file)
	{
		Path path = resolve(file);
		if (Files.isRegularFile(path))
			return path;
		else
			return null;
	}

	public Path resolve(String folder)
	{
		if (folder.startsWith("/")) folder = folder.substring(1);
		return rootPath.resolve(Path.of(folder));
	}

	public String getRelativePath(Path path)
	{
		return rootPath.relativize(path).toString().replace("\\", "/");
	}
}
