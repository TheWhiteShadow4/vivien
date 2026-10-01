package tws.vivien.core;

import org.eclipse.jgit.ignore.FastIgnoreRule;
import org.eclipse.jgit.ignore.IgnoreNode;


import java.util.ArrayList;
import java.util.List;

public class PathMatcher
{
	private final IgnoreNode ignoreNode;

	public PathMatcher(List<String> patterns)
	{
		List<FastIgnoreRule> rules = new ArrayList<>();
		for(String rule : patterns)
		{
			rules.add(new FastIgnoreRule(rule));
		}
		ignoreNode = new IgnoreNode(rules);
	}

	public boolean matches(String path, boolean isDirectory)
	{
		if (ignoreNode.isIgnored(path, isDirectory) == IgnoreNode.MatchResult.IGNORED)
		{
			return true;
		}

		String currentPath = path;
		int lastSlash;

		while ((lastSlash = currentPath.lastIndexOf('/')) != -1)
		{
			currentPath = currentPath.substring(0, lastSlash);
			
			if (ignoreNode.isIgnored(currentPath, true) == IgnoreNode.MatchResult.IGNORED)
			{
				return true;
			}
		}
		return false;
	}
}
