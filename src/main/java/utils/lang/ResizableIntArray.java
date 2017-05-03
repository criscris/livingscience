package utils.lang;

import java.util.ArrayList;
import java.util.Arrays;

public class ResizableIntArray {

	public static void main(String[] args) throws Exception
	{
		int size = 20000000;

		long t1 = System.currentTimeMillis();
		int[] data = new int[size];
		for (int i=0; i<size; i++)
		{
			data[i] = i;
		}
		
		long t2 = System.currentTimeMillis();
		
		for (int i=0; i<size; i++)
		{
			if (i != data[i]) throw new Exception("wrong array data.");
		}		
		
		long t3 = System.currentTimeMillis();
		
		ResizableIntArray a = new ResizableIntArray(size);
		for (int i=0; i<size; i++)
		{
			a.add(i);
		}
		
		long t4 = System.currentTimeMillis();
		
		for (int i=0; i<size; i++)
		{
			if (i != a.get(i)) throw new Exception("wrong array data.");
		}		
		
		long t5 = System.currentTimeMillis();
		
		
		ArrayList<Integer> b = new ArrayList<>(size);
		for (int i=0; i<size; i++)
		{
			b.add(i);
		}
		
		long t6 = System.currentTimeMillis();
		
		for (int i=0; i<size; i++)
		{
			if (i != b.get(i)) throw new Exception("wrong array data.");
		}	
		
		long t7 = System.currentTimeMillis();
		
		
		
		System.out.println((t2 - t1) + " " + (t3 - t2) + " " + (t4 - t3) + " " + (t5 - t4) + " " + (t6 - t5) + " " + (t7 - t6));
	}
	
	int[] data;
	int size;

	public ResizableIntArray(int initialCapacity)
	{
		data = new int[initialCapacity];
		size = 0;
	}
	
	public ResizableIntArray(int[] data)
	{
		this.data = data;
		size = data.length;
	}
	
	public final void add(int value)
	{
		checkResize(size + 1);
		data[size] = value;
		size++;
	}
	
	public final void set(int index, int value)
	{
		checkResize(index + 1);
		data[index] = value;
		size = Math.max(size, index + 1);
	}
	
	public final int get(int index)
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
	
	public int[] trim()
	{
		if (data.length > size)
		{
			data = Arrays.copyOf(data, size);
		}
		return data;
	}
	
	public int min()
	{
		if (size == 0) return 0;
		int min = data[0];
		for (int i=1; i<size; i++) min = Math.min(min, data[i]);
		return min;
	}
	
	public int max()
	{
		if (size == 0) return 0;
		int max = data[0];
		for (int i=1; i<size; i++) max = Math.max(max, data[i]);
		return max;
	}
	
	/**
	 * sum() < Integer.MAX
	 * @return
	 */
	public int sum()
	{
		if (size == 0) return 0;
		
		int sum = 0;
		for (int i=0; i<size; i++) sum += data[i];
		return sum;
	}

	public int mean()
	{
		if (size == 0) return 0;
		return sum() / size;
	}
	
	public final int size()
	{
		return size;
	}
}
