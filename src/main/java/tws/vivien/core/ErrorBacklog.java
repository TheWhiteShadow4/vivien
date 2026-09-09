package tws.vivien.core;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.*;

@Singleton
public class ErrorBacklog
{
	private final List<Exception> systemErrors = Collections.synchronizedList(new ArrayList<>());

	// Für jeden User
	private final Map<String, List<Exception>> requestErrors = Collections.synchronizedMap(new HashMap<>());

	@Inject
	public ErrorBacklog() {}

	public void addSystemError(Exception error)
	{
		systemErrors.add(error);
	}

	public void addSystemErrors(Collection<? extends Exception> errors)
	{
		systemErrors.addAll(errors);
	}

	public List<Exception> getSystemErrors()
	{
		return List.copyOf(systemErrors);
	}

	public void addRequestError(Exception error)
	{
		requestErrors.computeIfAbsent("all", k -> new ArrayList<>()).add(error);
	}

	public List<Exception> readRequestErrors()
	{
		var list = requestErrors.remove("all");
		return list != null ? list : Collections.emptyList();
	}
}
