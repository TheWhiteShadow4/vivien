package tws.vivien.test;

import io.javalin.http.Context;
import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.merge.MergeStrategy;
import org.eclipse.jgit.transport.URIish;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import tws.vivien.api.Api;
import tws.vivien.core.Server;
import tws.vivien.dto.CommitRequest;
import tws.vivien.dto.GitBranchStatus;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ServerTests
{
	@TempDir  Path tempDir;
	@TempDir  Path remoteTempDir;
	TestComponent component;

	@BeforeEach
	void setupGitRepository() throws Exception
	{
		component = DaggerTestComponent.builder().testDaggerModule(new TestDaggerModule(tempDir)).build();
	}

	private Path createFile(String name) throws IOException
	{
		Path file = component.repository().getRoot().resolve(name);
		FileUtils.writeStringToFile(file.toFile(), name, Charset.defaultCharset());
		return file;
	}

	@Test
	public void testeCommit() throws Exception
	{
		var api = component.commitApi();
		try(var remote = Git.init().setDirectory(remoteTempDir.toFile()).call())
		{
			var file = createFile("test.txt");
			api.repository.trackFile(file);
			api.repository.getApi().remoteAdd().setName("origin").setUri(new URIish(remoteTempDir.toUri().toURL())).call();
			api.config.gitRemote = "origin";
			api.config.mergeStrategy = MergeStrategy.OURS;

			var req = new CommitRequest();
			req.name = "Mimi";
			req.email = "mimi@mini.mi";
			req.message = "Commit Test";

			// Erfolgreicher Aufruf
			GitBranchStatus status = testeApiRoute(api, req, req.name);
			assertFalse(status.uncommited, "Git zeigt Änderungen nach Commit");

			// Erzeuge Merge Konflikt
			Path remoteFile = remoteTempDir.resolve("test.txt");
			GitTestUtils.modifyFile(remoteFile);
			remote.commit().setAll(true).setMessage("").call();
			GitTestUtils.modifyFile(file);
			api.repository.getApi().add().setAll(true).call();

			// Fehlerhafter Aufruf mit Merge Konflikt
			testeApiRoute(api, req, 409, req.name);

			status = api.repository.getBranchStatus();
			assertFalse(status.uncommited, "Git zeigt Änderungen nach Commit");
			api.repository.pull();

			// Konflikt durch Pull behoben, jetzt Erfolgreich
			status = testeApiRoute(api, req, req.name);
			assertFalse(status.uncommited, "Git zeigt Änderungen nach Commit");
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			api.repository.close();
		}
	}

	private void testeApiRoute(Api api, Object data, int status, String user)
	{
		var context = Mockito.mock(Context.class);
		when(context.bodyAsClass(Mockito.any())).thenReturn(data);
		when(context.header(Server.APP_USER)).thenReturn(user);

		api.handle(context);

		verify(context).status(status);
	}

	private <T> T testeApiRoute(Api api, Object data, String user)
	{
		var context = Mockito.mock(Context.class);
		when(context.bodyAsClass(Mockito.any())).thenReturn(data);
		when(context.header(Server.APP_USER)).thenReturn(user);

		api.handle(context);

		var c = ArgumentCaptor.captor();
		verify(context).json(c.capture());

		return (T) c.getValue();
	}
}
