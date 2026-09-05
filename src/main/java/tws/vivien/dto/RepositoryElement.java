package tws.vivien.dto;

import jakarta.annotation.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class RepositoryElement
{
	public String name;
	public String path;
	public ElementType type;
	@Nullable
	public Map<String, Object> importProps;

	@Nullable
	public List<RepositoryElement> children;

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
		copy.importProps = this.importProps;
		// Kein lazy loading, bei leeren Listen
		if (children != null && children.isEmpty())
			copy.children = Collections.emptyList();
		return copy;
	}

	@Override
	public String toString()
	{
		return "RepositoryElement{" +
				"path='" + path + "'(" + type +
				"), children=" + (children != null ? children.size() : null) +
				'}';
	}
}
