package tws.vivien.dto;

import java.util.List;

public class RepositoryView
{
	public List<RepositoryElement> elements;

	public static class RepositoryElement
	{
		public String name;
		public ElementType type;
		public List<RepositoryElement> children;
	}
}
