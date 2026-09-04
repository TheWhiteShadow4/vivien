package tws.vivien.dto;

public class ImportSetting
{
	public String displayLabel;
	public String type;
	public Object value;

	public ImportSetting(String displayLabel, int value)
	{
		this.displayLabel = displayLabel;
		this.type = "int";
		this.value = value;
	}

	public int intValue()
	{
		return (Integer) value;
	}
}
