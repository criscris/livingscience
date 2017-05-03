package utils.plot;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import utils.lang.ArraysUtil;

public class Axis
{
	public enum AxisType
	{
		Linear,
		Log
	}
	public AxisType type;
	
	public String label;
	public Range range;
	public float[] ticks;
	public DecimalFormat df = new DecimalFormat("#.####");
	public String[] tickLabels; // optional
	
	
	public String getLabel(int index)
	{
		return tickLabels != null ? tickLabels[index] : df.format(ticks[index]);
	}
	
	public Axis(String label, Range range, float[] ticks)
	{
		this(label, range, ticks, null);
	}
	
	public Axis(String label, Range range, float[] ticks, String[] tickLabels)
	{
		this.label = label;
		this.range = range;
		this.ticks = ticks;
		this.tickLabels = tickLabels;
		type = AxisType.Linear;
	}
	
	public Axis(String label, Range range, List<Float> ticks, List<String> tickLabels)
	{
		this.label = label;
		this.range = range;
		this.ticks = Plot.getArray(ticks);
		this.tickLabels = Plot.getArrayS(tickLabels);
		type = AxisType.Linear;
	}
	
	static final String[] superscripts = new String[] { 
		"\u2070",  // 0
		"\u00B9",  // 1
		"\u00B2",  // 2  ...
		"\u00B3",
		"\u2074",
		"\u2075",
		"\u2076",
		"\u2077",
		"\u2078",
		"\u2079"}; // 9
	static final String superscriptMinus = "\u207B";
	
	/**
	 * 
	 * from minScale*10^min10 to maxScale*10^max10
	 * 
	 * minScale/maxScale is from 1..9
	 */
	public Axis(String label, int minScale, int min10, int maxScale, int max10)
	{
		this.label = label;
		type = AxisType.Log;
		range = new Range((float) (minScale * Math.pow(10, min10)), (float) (maxScale * Math.pow(10,  max10)));
		
		List<Float> ticksList = new ArrayList<>();
		List<String> tickLabelsList = new ArrayList<>();
		
		int currentScale = minScale;
		int current10 = min10;
		
		while (currentScale <= maxScale || current10 < max10)
		{

			float t = (float) (currentScale * Math.pow(10, current10));
			ticksList.add(t);
			
			String l = null;
			if (currentScale == 1) // main tick
			{
				int ea = Math.abs(current10);
				l = "10" + (current10 < 0 ? superscriptMinus : "") + 
						(ea >= 100 ? superscripts[(ea/100) % 10] : "") +
						(ea >= 10 ? superscripts[(ea/10) % 10] : "") + 
						superscripts[ea % 10];
				
				
//				System.out.println(t + ": " + l);
			}
			tickLabelsList.add(l);
			
			
			
			currentScale++;
			if (currentScale == 10)
			{
				currentScale = 1;
				current10++;
			}
		}
		
		ticks = ArraysUtil.toFloatArray(ticksList);
		tickLabels = ArraysUtil.toStringArray(tickLabelsList);
	}
	
	public float getNorm(float realValue)
	{
		if (type == AxisType.Log) 
		{
			float inLogSpace = (float) Math.log10(realValue);
			
			float rangeMin = (float) Math.log10(range.min);
			float rangeMax = (float) Math.log10(range.max);
			
			return (inLogSpace - rangeMin) / (rangeMax - rangeMin);
		}
		return (realValue - range.min) / (range.max - range.min);
	}
}
