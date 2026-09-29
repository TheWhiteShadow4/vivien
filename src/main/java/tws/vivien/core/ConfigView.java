package tws.vivien.core;

import tws.vivien.dto.ElementType;

import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.ArrayList;
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
		private final List<PathMatcher> writesMatchers = new ArrayList<>();
		private final List<PathMatcher> includeMatchers = new ArrayList<>();
		private final List<PathMatcher> excludeMatchers = new ArrayList<>();

		public ViewFilter(ConfigView config)
		{
			if (config.writes != null)
			{
				for (String pattern : config.writes)
				{
					writesMatchers.add(createMatcher(pattern));
				}
			}

			if (config.includes != null)
			{
				for (String pattern : config.includes)
				{
					includeMatchers.add(createMatcher(pattern));
				}
			}

			if (config.excludes != null)
			{
				for (String pattern : config.excludes)
				{
					excludeMatchers.add(createMatcher(pattern));
				}
			}
		}

		private PathMatcher createMatcher(String pattern)
		{
			String exp = pattern;
			if (exp.endsWith("/"))
			{
				exp = exp + "**";
			}

			if (pattern.startsWith("/"))
			{
				exp = "glob:" + exp.substring(1);
			}
			else
			{
				exp = "glob:**" + exp;
			}

			System.out.println("Matcher für " + pattern + " = " + exp);
			return FileSystems.getDefault().getPathMatcher(exp);
		}

		public boolean isIncluded(Path path, ElementType type)
		{
			// 1. Exclude-Filter prüfen (Sobald ein Exclude-Pattern matcht -> direkt aussortieren)
			for (PathMatcher matcher : excludeMatchers)
			{
				if (matcher.matches(path)) return false;
			}

			// Wenn keine Includes definiert sind, lassen wir standardmäßig alles durch (außer Excludes).
			if (includeMatchers.isEmpty()) return true;

			if (type == ElementType.FOLDER) return true;

			// Falls Includes definiert sind, MUSS mindestens eines davon matchen

			for (PathMatcher matcher : includeMatchers)
			{
				if (matcher.matches(path)) return true;
			}
			return false;
		}

		public boolean isReadonly(String file)
		{
			if (writesMatchers.isEmpty()) return false;

			var path = Path.of(file);
			System.out.println(path);

			for (PathMatcher matcher : writesMatchers)
			{
				if (matcher.matches(path)) return false;
			}
			return true;
		}
	}
}
