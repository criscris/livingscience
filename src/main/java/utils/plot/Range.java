package utils.plot;

public class Range
{
	public float min;
	public float max;
	
	public Range(float[] data)
	{
		min = Float.MAX_VALUE;
		max = -Float.MAX_VALUE;
		for (int i=0; i<data.length; i++)
		{
			min = Math.min(data[i], min);
			max = Math.max(data[i], max);
		}
	}
	
	public Range(float min, float max)
	{
		this.min = min;
		this.max = max;
	}
	
	public boolean isWithin(float value)
	{
		return value >= min && value <= max;
	}
	
	/**
	 * 
	 * @param norm 0..1
	 * @return min..max
	 */
	public float getFromNorm(float norm)
	{
		return min + (max - min) * norm;
	}
	
	public float[] generateTicks(int noOfsTicks)
	{
		float[] ticks = new float[noOfsTicks];
		ticks[0] = min;
		ticks[ticks.length - 1] = max;
		
		float d = (max - min) / (noOfsTicks - 1);
		for (int i=1; i<ticks.length - 1; i++)
		{
			ticks[i] = min +  d * i;
		}
		
		return ticks;
	}
	
	public static float getSmallest1Digitbound(float value, boolean above)
	{
		if (value == 0f) return 0f;
		
		boolean inverse = false;
		if (value < 0)
		{
			value *= -1;
			inverse = true;
			above = !above;
		}
		
		float u = 1f;  // value == 1f -> 1f;

		if (value < 1f)
		{
			int i=1;
			float x = value;
			while ((x *= 10) < 1f) i++;

			double s = above ? Math.ceil(x) : Math.floor(x);
			u = (float) (s / Math.pow(10, i));
		}
		else if (value > 1f & value <= 10f)
		{
			double s = above ? Math.ceil(value) : Math.floor(value);
			u = (float) s;
		}
		else if (value > 10f)
		{
			int i=1;
			float x = value;
			while ((x /= 10f) >= 10f) i++;

			double s = above ? Math.ceil(x) : Math.floor(x);
			u = (float) (s * Math.pow(10, i));
		}
		
		return inverse ? -u : u;
	}
	
	public static void main(String[] args) throws Exception
	{
		System.out.println("0.3343232f -> " + getSmallest1Digitbound(0.3343232f, false));
		System.out.println("-5.323 -> " + getSmallest1Digitbound(-5.323f, true));
	}
}
