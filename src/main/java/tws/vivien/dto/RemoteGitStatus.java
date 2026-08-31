package tws.vivien.dto;

public class RemoteGitStatus
{
	public int behindCount;
	public int aheadCount;


	public RemoteGitStatus(int behindCount, int aheadCount)
	{
		this.behindCount = behindCount;
		this.aheadCount = aheadCount;
	}
}
