package tws.vivien.dto;

import java.util.Set;

public class GitBranchStatus
{
	public String branch;
	public RemoteGitStatus remote;
	public boolean modified;
	public Set<String> untracked;
	public Set<String> added;
	public Set<String> changed;
	public Set<String> missing;
	public Set<String> conflicts;
}
