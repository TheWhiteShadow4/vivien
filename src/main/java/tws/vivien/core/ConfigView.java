package tws.vivien.core;

import tws.vivien.dto.ElementType;

import java.util.List;

public class ConfigView
{
	public String name;
	public List<String> writes;
	public List<String> includes;
	public List<String> excludes;
	private final ViewFilter viewFilter;

	public ConfigView(String name, List<String> writes, List<String> includes, List<String> excludes)
	{
		this.name = name;
		this.writes = writes;
		this.includes = includes;
		this.excludes = excludes;
		this.viewFilter = new ViewFilter(this);
	}

	public ViewFilter getFilter()
	{
		return viewFilter;
	}

	@Override
	public String toString()
	{
		return "ConfigView{" +
				"name='" + name + '\'' +
				", writes=" + writes +
				", includes=" + includes +
				", excludes=" + excludes +
				'}';
	}

	public static class ViewFilter
	{
		private PathMatcher writesMatcher;
		private PathMatcher includeMatcher;
		private PathMatcher excludeMatcher;

		public ViewFilter(ConfigView config)
		{
			if (config.writes != null)
			{
				writesMatcher = new PathMatcher(config.writes);
			}

			if (config.includes != null)
			{
				includeMatcher = new PathMatcher(config.includes);
			}

			if (config.excludes != null)
			{
				excludeMatcher = new PathMatcher(config.excludes);
			}
		}

		public boolean isIncluded(String path, ElementType type)
		{
			boolean isDir = type != ElementType.FILE;
			if (excludeMatcher != null && excludeMatcher.matches(path, isDir)) return false;

			if (includeMatcher == null) return true;
			if (type == ElementType.FOLDER) return true;

			if (includeMatcher.matches(path, isDir)) return true;
			return false;
		}
		public boolean isReadonly(String file)
		{
			return isReadonly(file, false);
		}

		public boolean isReadonly(String file, boolean isDirectory)
		{
			System.out.println(file);

			if (writesMatcher == null) return false;
			if (writesMatcher.matches(file, isDirectory)) return false;
			return true;
		}
	}
}
