package tws.vivien.dto;

import jakarta.annotation.Nullable;

import java.util.Set;

public class GitBranchStatus
{
	public String branch;
	@Nullable
	public RemoteGitStatus remote;
	public boolean uncommited;
	public Set<String> untracked;
	public Set<String> added;
	public Set<String> modified;
	public Set<String> changed;
	public Set<String> removed;
	public Set<String> missing;
	public Set<String> conflicts;
}
