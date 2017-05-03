package utils.lang;

import java.util.HashMap;
import java.util.Map;

public class UniqueXYCoords 
{
	Map<String, Integer> coordToIndex = new HashMap<>();
	ResizableFloatArray xyCounts = new ResizableFloatArray(1);
	
	public void add(float x, float y)
	{
		String id = x + "_" + y;
		
		Integer index = coordToIndex.get(id);
		if (index == null)
		{
			index = xyCounts.size() / 3;
			coordToIndex.put(id, index);
		}
		
		xyCounts.set(index * 3, x);
		xyCounts.set(index * 3 + 1, y);
		xyCounts.addToPreviousValue(index * 3 + 2, 1);
	}
	
	public void addAll(float[] x, float[] y)
	{
		for (int i=0; i<x.length; i++) add(x[i], y[i]);
	}
	
	public float[] getXArray()
	{
		return xyCounts.getArray(0, 2, xyCounts.size() / 3);
	}
	
	public float[] getYArray()
	{
		return xyCounts.getArray(1, 2, xyCounts.size() / 3);
	}
	
	public float[] getCounts()
	{
		return xyCounts.getArray(2, 2, xyCounts.size() / 3);
	}
}
