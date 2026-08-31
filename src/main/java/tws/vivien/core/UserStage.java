package tws.vivien.core;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

public class UserStage
{
	public Set<Path> added = new HashSet<>();
	public Set<Path> removed = new HashSet<>();

	public boolean isEmpty()
	{
		return added.isEmpty() && removed.isEmpty();
	}
}
