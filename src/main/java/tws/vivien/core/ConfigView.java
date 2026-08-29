package tws.vivien.core;

import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.ArrayList;
import java.util.List;

public class ConfigView
{
	public String name;
	public List<String> includes;
	public List<String> excludes;

	public ConfigView(String name)
	{
		this.name = name;
		this.includes = new ArrayList<>();
		this.excludes = new ArrayList<>();
	}

	public ConfigView(String name, List<String> includes, List<String> excludes)
	{
		this.name = name;
		this.includes = includes;
		this.excludes = excludes;
	}

	public ViewFilter getFilter()
	{
		return new ViewFilter(this);
	}

	@Override
	public String toString()
	{
		return "ConfigView{" +
				"name='" + name + '\'' +
				", includes=" + includes +
				", excludes=" + excludes +
				'}';
	}

	public static class ViewFilter
	{
		private final List<PathMatcher> includeMatchers = new ArrayList<>();
		private final List<PathMatcher> excludeMatchers = new ArrayList<>();

		public ViewFilter(ConfigView config)
		{
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
			if (pattern.endsWith("/"))
			{
				pattern = pattern.substring(0, pattern.length()-1);
			}

			String syntaxAndPattern;
			if (!pattern.startsWith("/"))
			{
				syntaxAndPattern = "glob:**/" + pattern;
			}
			else
			{
				syntaxAndPattern = "glob:" + pattern;
			}

			return FileSystems.getDefault().getPathMatcher(syntaxAndPattern);
		}

		public boolean isIncluded(Path path)
		{
			// 1. Exclude-Filter prüfen (Sobald ein Exclude-Pattern matcht -> direkt aussortieren)
			for (PathMatcher matcher : excludeMatchers)
			{
				if (matcher.matches(path)) {
					return false;
				}
			}

			// Wenn keine Includes definiert sind, lassen wir standardmäßig alles durch (außer Excludes).
			if (includeMatchers.isEmpty()) {
				return true;
			}

			// Falls Includes definiert sind, MUSS mindestens eines davon matchen
			for (PathMatcher matcher : includeMatchers) {
				if (Files.isDirectory(path) || matcher.matches(path)) {
					return true;
				}
			}
			return false;
		}
	}
}
