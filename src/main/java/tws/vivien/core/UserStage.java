package tws.vivien.core;

import java.util.HashSet;
import java.util.Set;

public class UserStage
{
	public Set<String> added = new HashSet<>();
	public Set<String> removed = new HashSet<>();

	public boolean isEmpty()
	{
		return added.isEmpty() && removed.isEmpty();
	}
}
