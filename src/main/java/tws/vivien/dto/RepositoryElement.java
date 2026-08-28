package tws.vivien.dto;

import java.util.List;

public class RepositoryElement
{
	public String name;
	public ElementType type;
	public List<RepositoryElement> children;
	public GitStatus gitStatus;

	// Client Only Attribute
	public boolean lazy;
	public boolean selected;
}
