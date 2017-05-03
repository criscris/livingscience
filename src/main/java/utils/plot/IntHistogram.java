package utils.plot;

public class IntHistogram
{
	public int[] counts;
	int sum = 0;
	
	public IntHistogram(int maxValue)
	{
		counts = new int[maxValue + 1];
	}
	
	public final int getMaxValue()
	{
		return counts.length - 1;
	}
	
	public void addAll(int[] values)
	{
		for (int i=0; i<values.length; i++) add(values[i]);
	}
	
	public final void add(int value)
	{
		counts[value]++;
		sum++;
	}
	
	public final void add_checked(int value)
	{
		counts[Math.max(0, Math.min(counts.length - 1, value))]++;
		sum++;
	}
	
	public int totalCount()
	{
		return sum;
	}
	
	public float[] getBinValues()
	{
		float[] binValues = new float[counts.length];
		for (int i=0; i<counts.length; i++) binValues[i] = i;
		return binValues;
	}
	
	public float[] getFractions()
	{
		float[] fractions = new float[counts.length];
		for (int i=0; i<counts.length; i++) fractions[i] = (float) counts[i] / sum;
		return fractions;
	}
	
	public float[] getCounts()
	{
		float[] fractions = new float[counts.length];
		for (int i=0; i<counts.length; i++) fractions[i] = counts[i];
		return fractions;
	}
	
	public float mean()
	{
		int totalSum = 0;
		for (int i=0; i<counts.length; i++) totalSum += i * counts[i];
		
		return (float) totalSum / sum;
	}
}
