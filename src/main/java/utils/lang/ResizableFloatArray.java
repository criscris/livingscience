package utils.lang;

import java.util.Arrays;

public class ResizableFloatArray 
{	
	float[] data;
	int size;

	public ResizableFloatArray(int initialCapacity)
	{
		data = new float[initialCapacity];
		size = 0;
	}
	
	public ResizableFloatArray(float[] data)
	{
		this.data = data;
		size = data.length;
	}
	
	public ResizableFloatArray(float[] data, int offset, int length)
	{
		this.data = new float[length];
		size = this.data.length;
		
		for (int i=0; i<this.data.length; i++)
		{
			this.data[i] = data[offset + i]; 
		}
	}
	
	public ResizableFloatArray subArray(int offset, int length)
	{
		return new ResizableFloatArray(data, offset, length);
	}
	
	public final void add(float value)
	{
		checkResize(size + 1);
		data[size] = value;
		size++;
	}
	
	public final void set(int index, float value)
	{
		checkResize(index + 1);
		data[index] = value;
		size = Math.max(size, index + 1);
	}
	
	public final void addToPreviousValue(int index, float value)
	{
		checkResize(index + 1);
		data[index] += value;
		size = Math.max(size, index + 1);
	}
	
	public final float get(int index)
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
	
	public final int size()
	{
		return size;
	}
	
	public final float mean()
	{
		if (size == 0) return Float.NaN;
		return sum() / size;
	}
	
	public final float sum()
	{
		float sum = 0f;
		for (int i=0; i<size; i++) sum += data[i];
		return sum;
	}
	
	public final void multAll(float value)
	{
		for (int i=0; i<size; i++) data[i] *= value;
	}
	
	public enum StandardDeviationType
	{
		Population,
		RandomSample
	}
	
	public final float standardDeviation(StandardDeviationType type)
	{
		float mean = mean();
		if (Float.isNaN(mean)) return Float.NaN;
		
		float sd = 0f;
		for (int i=0; i<size; i++) 
		{
			float d = data[i] - mean;
			sd += d*d;
		}
		sd /= size - (type == StandardDeviationType.RandomSample ? 1 : 0);
		return (float) Math.sqrt(sd);
	}
	
	public final float confidenceIntervall95()
	{
		float mean = mean();
		if (Float.isNaN(mean)) return Float.NaN;
		
		float s2 = 0f;
		for (int i=0; i<size; i++) 
		{
			float d = data[i] - mean;
			s2 += d*d;
		}
		s2 /= size - 1;
	
		float t = size - 1 == 0 ? 0 : (float) StudentT.tTable(size - 1, 0.05f);
		
		return (float) (t * Math.sqrt(s2) / Math.sqrt(size));
	}
	
	public final void sortAscending()
	{
		Arrays.sort(data, 0, size - 1);
	}
	
	public float[] getArray()
	{
		return Arrays.copyOf(data, size);
	}
	
	public float[] getArray(int offset, int stride, int length)
	{
		float[] a = new float[length];
		int oi = offset;
		for (int i=0; i<length; i++)
		{
			a[i] = data[oi];
			oi += stride + 1;
		}
		return a;
	}
 }
