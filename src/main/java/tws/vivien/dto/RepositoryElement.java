package tws.vivien.dto;

import jakarta.annotation.Nullable;

import java.util.Collections;
import java.util.List;

public class RepositoryElement
{
	public String name;
	public String path;
	public ElementType type;

	@Nullable
	public List<RepositoryElement> children;
	public GitStatus gitStatus;

	// Client Only Attribute
	@Nullable
	public RepositoryElement parent;

	public RepositoryElement flatCopyWithChildren()
	{
		var copy = flatCopy();
		if (children != null)
		{
			copy.children = children.stream().map(RepositoryElement::flatCopy).toList();
		}
		return copy;
	}

	public RepositoryElement flatCopy()
	{
		var copy = new RepositoryElement();
		copy.name = this.name;
		copy.path = this.path;
		copy.type = this.type;
		copy.gitStatus = this.gitStatus;
		// Kein lazy loading, bei leeren Listen
		if (children != null && children.isEmpty())
			copy.children = Collections.emptyList();
		return copy;
	}
}
