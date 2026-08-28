package tws.vivien.dto;

import jakarta.annotation.Nullable;

import java.util.List;

public class RepositoryElement
{
	public String name;
	public String path;
	public ElementType type;
	public List<RepositoryElement> children;
	public GitStatus gitStatus;

	// Client Only Attribute
	public boolean lazy;
	@Nullable
	public RepositoryElement parent;
}
