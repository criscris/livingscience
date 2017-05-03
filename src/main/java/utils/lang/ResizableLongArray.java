package utils.lang;

import java.util.Arrays;

public class ResizableLongArray 
{
	long[] data;
	int size;

	public ResizableLongArray(int initialCapacity)
	{
		data = new long[initialCapacity];
		size = 0;
	}
	
	public final void add(long value)
	{
		checkResize(size + 1);
		data[size] = value;
		size++;
	}
	
	public final void set(int index, long value)
	{
		checkResize(index + 1);
		data[index] = value;
		size = Math.max(size, index + 1);
	}
	
	public final long get(int index)
	{
		return data[index];
	}
	
	private final void checkResize(int neededSize)
	{
		if (neededSize > data.length)
		{
			int newCapacity = Math.max(neededSize, data.length + (data.length >> 1)); // grow by 1.5
			data = Arrays.copyOf(data, newCapacity);
		}
	}
	
	public long[] trim()
	{
		if (data.length > size)
		{
			data = Arrays.copyOf(data, size);
		}
		return data;
	}
	
	public final int size()
	{
		return size;
	}
}
