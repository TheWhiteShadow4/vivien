package tws.vivien.dto;

import jakarta.annotation.Nullable;

import java.util.List;

public class TypedData
{
	public String name;
	public String label;
	public String type;
	public Object value;
	@Nullable
	public List<String> options;
	@Nullable
	public String filter;

	public String key() { return name; }

	public TypedData() {}

	public TypedData(String type, String name, String label, Object value)
	{
		this.type = type;
		this.name = name;
		this.label = label;
		this.value = value;
	}

	public static TypedData asInt(String name, String label, int value)
	{
		return new TypedData("int", name, label, value);
	}

	public static TypedData asFloat(String name, String label, float value)
	{
		return new TypedData("float", name, label, value);
	}

	public static TypedData asBool(String name, String label, int value)
	{
		return new TypedData("bool", name, label, value > 0);
	}

	public static TypedData asEnum(String name, String label, int value)
	{
		return new TypedData("enum", name, label, value);
	}

	public TypedData withOptions(List<String> options)
	{
		this.options = options;
		return this;
	}

	public TypedData withFilter(String filter)
	{
		this.filter = filter;
		return this;
	}

	public int intValue()
	{
		var cls = value.getClass();
		if (cls == Integer.class) return (Integer) value;
		if (cls == Boolean.class) return ((Boolean) value) ? 1 : 0;
		if (cls == String.class) return Integer.parseInt((String) value);
		throw new UnsupportedOperationException();
	}

	public float floatValue()
	{
		var cls = value.getClass();
		if (cls == Float.class) return (Float) value;
		if (cls == Integer.class) return (Integer) value;
		throw new UnsupportedOperationException();
	}

	@Override
	public String toString()
	{
		return "TypedData{" +
				"name='" + name + '\'' +
				", type='" + type + '\'' +
				", value=" + value + '}';
	}
}
