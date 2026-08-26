package tws.vivien.dto;

import java.util.List;

public class Repository
{
	public List<RepositoryElement> elements;

	public static class RepositoryElement
	{
		public String name;
		public ElementType type;
		public List<RepositoryElement> childs;
	}

	public static enum ElementType
	{
		FOLDER,
		FILE
	}
}
