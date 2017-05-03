package utils.lang;

import java.util.Arrays;
import java.util.List;

public class ArraysUtil 
{
	public static final float[] add(float[] array1, float[] array2)
	{
		float[] sum = new float[Math.min(array1.length, array2.length)];
		for (int i=0; i<sum.length; i++) sum[i] = array1[i] + array2[i];
		return sum;
	}
	
	public static final float[] create(int minValue, int maxValue)
	{
		int noOfElements = maxValue - minValue + 1;
		float[] data = new float[noOfElements];
		for (int i=0; i<noOfElements; i++) data[i] = minValue + i;
		return data;
	}
	
	public static final float[] create(float minValue, float step, float maxValue)
	{
		int noOfElements = (int) ((maxValue - minValue)/step) + 1;
		float[] data = new float[noOfElements];
		for (int i=0; i<noOfElements; i++) data[i] = minValue + step*i;
		return data;
	}
	
	public static final float max(float[] array)
	{
		float max = -Float.MAX_VALUE;
		for (int i=0; i<array.length; i++)
		{
			max = Math.max(max, array[i]);
		}
		return max;
	}
	
	public static final float max(float[] array, int rankIndex)
	{
		float[] s = Arrays.copyOf(array, array.length);
		Arrays.sort(s);
		return s[Math.max(s.length - 1 - rankIndex, 0)];
	}
	
	public static final float min(float[] array)
	{
		float min = Float.MAX_VALUE;
		for (int i=0; i<array.length; i++)
		{
			min = Math.min(min, array[i]);
		}
		return min;
	}
	
	public static final float dot(float[] array1, float[] array2)
	{
		float sum = 0f;
		for (int i=0; i<array1.length; i++) sum += array1[i] * array2[i];
		return sum;
	}
	
	public static final float[] sub(float[] array1, float[] array2)
	{
		float[] sum = new float[Math.min(array1.length, array2.length)];
		for (int i=0; i<sum.length; i++) sum[i] = array1[i] - array2[i];
		return sum;
	}
	
	public static final double[] toDouble(float[] array)
	{
		double[] darray = new double[array.length];
		for (int i=0; i<darray.length; i++) darray[i] = array[i];
		return darray;
	}
	
	public static final float[] toFloat(double[] array)
	{
		float[] darray = new float[array.length];
		for (int i=0; i<darray.length; i++) darray[i] = (float) array[i];
		return darray;
	}
	
	public static final float[] toFloat(int[] array)
	{
		float[] darray = new float[array.length];
		for (int i=0; i<darray.length; i++) darray[i] = (float) array[i];
		return darray;
	}
	
	public static final float[] toFloatArray(List<Float> floatList)
	{
		float[] array = new float[floatList.size()];
		for (int i=0; i<array.length; i++) array[i] = floatList.get(i);
		return array;
	}
	
	public static final long[] toLongArray(List<Long> longList)
	{
		long[] array = new long[longList.size()];
		for (int i=0; i<array.length; i++) array[i] = longList.get(i);
		return array;
	}
	
	public static final int[] toIntArray(List<Integer> floatList)
	{
		int[] array = new int[floatList.size()];
		for (int i=0; i<array.length; i++) array[i] = floatList.get(i);
		return array;
	}
	
	public static final String[] toStringArray(List<String> list)
	{
		String[] array = new String[list.size()];
		for (int i=0; i<array.length; i++) array[i] = list.get(i);
		return array;
	}
	
	public static final void reverse(float[] array)
	{
		for (int i=0; i<array.length/2; i++)
		{
			int ri = array.length - 1 - i;
			
			float x = array[i];
			array[i] = array[ri]; 
			array[ri] = x;
		}
	}
	
	public static float sum(float[] values)
	{
		float sum = 0f;
		for (int i=0; i<values.length; i++) sum += values[i];
		return sum;
	}
	
	public static double sum(double[] values)
	{
		double sum = 0f;
		for (int i=0; i<values.length; i++) sum += values[i];
		return sum;
	}
}
