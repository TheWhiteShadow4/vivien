package tws.vivien.test;

import io.javalin.Javalin;
import io.javalin.testtools.JavalinTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

public class ServerTests
{
	@TempDir
	Path tempDir;
	TestComponent component;

	@BeforeEach
	void setupGitRepository() throws Exception
	{
		component = DaggerTestComponent.builder().testDaggerModule(new TestDaggerModule(tempDir)).build();
	}

	@Test
	public void testeCommit()
	{
		var api = component.commitApi();

		var app = Javalin.create(c -> {
			c.routes.post("/api/commit", api::handle);
		});

		JavalinTest.test(app, (server, client) -> {
			var response = client.get("/api/commit");
			//assertThen(response.code()).isEqualTo(200);
		});
	}
}
