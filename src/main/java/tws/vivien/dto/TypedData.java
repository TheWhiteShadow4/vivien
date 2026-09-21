package tws.vivien.dto;

public class TypedData
{
	public String name;
	public String displayLabel;
	public String type;
	public Object value;

	public TypedData() {}

	public TypedData(String name, String displayLabel, int value)
	{
		this.name = name;
		this.displayLabel = displayLabel;
		this.type = "int";
		this.value = value;
	}

	public int intValue()
	{
		return (Integer) value;
	}
}
