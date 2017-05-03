package utils.plot;

import java.util.List;

import utils.lang.ResizableFloatArray;
import utils.lang.ResizableFloatArray.StandardDeviationType;

public class FloatHistogram
{
	float[] counts;
	float[] unweightedCounts;
	public ResizableFloatArray[] values;
	boolean clipOutliers;
	
	float min = 0;
	float max = 0;
	float width;
	
	int sum1 = 0;
	float sum = 0;
	
	public FloatHistogram(float min, float max, int bins)
	{
		this(min, max, bins, false);
	}
	
	public FloatHistogram(float min, float max, int bins, boolean storeValues)
	{
		this(min, max, bins, storeValues, false);
	}
	
	public FloatHistogram(float min, float max, int bins, boolean storeValues, boolean clipOutliers)
	{
		this.min = min;
		this.max = max;
		counts = new float[bins];
		unweightedCounts = new float[bins];
		width = (max - min) / bins;
		if (storeValues)
		{
			values = new ResizableFloatArray[bins];
			for (int i=0; i<values.length; i++) values[i] = new ResizableFloatArray(2);
		}
		this.clipOutliers = clipOutliers;
	}
	
	public void addAll(int[] values)
	{
		for (int i=0; i<values.length; i++) add(values[i]);
	}
	
	public void addAll(float[] values)
	{
		for (int i=0; i<values.length; i++) add(values[i]);
	}
	
	public void addAll(List<Float> values)
	{
		for (int i=0; i<values.size(); i++) add(values.get(i));
	}
	
	public final void add(float value)
	{
		add(value, 1f);
	}
	
	public void addAll(float[] values, float[] weights)
	{
		for (int i=0; i<values.length; i++) add(values[i], weights[i]);
	}
	
	public void addAll(List<Float> values, List<Float> weights)
	{
		for (int i=0; i<values.size(); i++) add(values.get(i), weights.get(i));
	}
	
	public final void add(double value, float weight)
	{
		double indexF =  ((value - min) / width);
		if (clipOutliers && (indexF < 0 || indexF > counts.length)) return;
		
		int index = Math.max(0, Math.min(counts.length - 1,  (int) indexF));
		counts[index] += weight;
		unweightedCounts[index] += 1;
		sum += weight;
		sum1++;
		if (values != null) values[index].add(weight);
	}
	
	public float[] getBinValues()
	{
		float[] binValues = new float[counts.length];
		for (int i=0; i<counts.length; i++) binValues[i] = min + width * i + width / 2f;
		return binValues;
	}
	
	public float[] getFractions()
	{
		float[] fractions = new float[counts.length];
		for (int i=0; i<counts.length; i++) fractions[i] = counts[i] / sum;
		return fractions;
	}
	
	public float[] getMeans()
	{
		float[] means = new float[counts.length];
		for (int i=0; i<counts.length; i++) means[i] = counts[i] / unweightedCounts[i];
		return means;
	}
	
	public float[] getStandardErrors(StandardDeviationType sdType)
	{
		float[] sd = new float[values.length];
		for (int i=0; i<sd.length; i++) sd[i] = values[i].standardDeviation(sdType);
		return sd;
	}
	
	public float[] getConfidenceIntervals95()
	{
		float[] sd = new float[values.length];
		for (int i=0; i<sd.length; i++) sd[i] = values[i].confidenceIntervall95();
		return sd;
	}
	
	public float[] getFractions1()
	{
		float[] fractions = new float[counts.length];
		for (int i=0; i<counts.length; i++) fractions[i] = counts[i] / sum1;
		return fractions;
	}
	
	public float[] getCounts()
	{
		return counts;
	}
	
	public float totalWeight()
	{
		return sum;
	}
}
