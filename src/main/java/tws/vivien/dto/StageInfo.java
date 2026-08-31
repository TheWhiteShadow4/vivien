package tws.vivien.dto;

import tws.vivien.core.UserStage;

public class StageInfo
{
	public int added;
	public int removed;
	public int modified;

	public StageInfo(UserStage userstage)
	{
		added = userstage.added.size();
		removed = userstage.removed.size();
	}
}
