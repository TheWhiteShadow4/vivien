package tws.vivien.dto;

public class TypedData
{
	public String name;
	public String label;
	public String type;
	public Object value;

	public TypedData() {}

	public TypedData(String name, String label, int value)
	{
		this.name = name;
		this.label = label;
		this.type = "int";
		this.value = value;
	}

	public int intValue()
	{
		return (Integer) value;
	}
}
