package tws.vivien.core;

import java.util.List;

public class ConfigView
{
	public String name;
	public List<String> includes;
	public List<String> excludes;

	public ConfigView(String name, List<String> includes, List<String> excludes)
	{
		this.name = name;
		this.includes = includes;
		this.excludes = excludes;
	}
}
