package utils.plot;

import java.io.ByteArrayInputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import utils.math.Mat;
import utils.text.TextFileUtil;

public class Plot
{
	public final float[] xData;
	public final float[] yData;
	public final float[] propData;
	
	public final Range xRange;
	public final Range yRange;
	public final Range propRange;
	
	public PlotProps props;
	
	public String label; // optional
	
	public static final String[] getArrayS(List<String> data)
	{
		if (data == null) return null;
		
		String[] array = new String[data.size()];
		for (int i=0; i<data.size(); i++) array[i] = data.get(i);
		return array;
	}
	
	public static final float[] getArray(List<Float> data)
	{
		if (data == null) return null;
		
		float[] array = new float[data.size()];
		for (int i=0; i<data.size(); i++) array[i] = data.get(i);
		return array;
	}
	
	public static final float[] getArrayI(List<Integer> data)
	{
		if (data == null) return null;
		
		float[] array = new float[data.size()];
		for (int i=0; i<data.size(); i++) array[i] = data.get(i);
		return array;
	}
	
	public Plot(List<Float> xData, List<Float> yData)
	{
		this(xData, yData, null);
	}
	
	public static Plot fromEntries(List<Entry<Integer, Integer>> data)
	{
		float[] x = new float[data.size()];
		float[] y = new float[data.size()];
		
		for (int i=0; i<data.size(); i++)
		{
			Entry<Integer, Integer> entry = data.get(i);
			x[i] = entry.getKey();
			y[i] = entry.getValue();
		}
		
		return new Plot(x, y);
	}
	
	public static Plot fromMat(Mat field2D)
	{
		List<Float> xData = new ArrayList<>();
		List<Float> yData = new ArrayList<>();
		List<Float> propData = new ArrayList<>();
		
		for (int y=0; y<field2D.getRows(); y++)
		{
			for (int x=0; x<field2D.getCols(); x++)
			{
				float value = field2D.get(x, y);
				if (value != 0)
				{
					xData.add((float) x);
					yData.add((float) y);
					propData.add(value);
				}
			}
		}
		
		return new Plot(xData, yData, propData);
	}
	
	public Plot(List<Float> xData, List<Float> yData, List<Float> propData)
	{
		this(getArray(xData), getArray(yData), getArray(propData));
	}
	
	public Plot(float[] xData, float[] yData)
	{
		this(xData, yData, null);
	}
	
	public Plot(float[] xData, float[] yData, float[] propData)
	{
		this.xData = xData;
		this.yData = yData;
		this.propData = propData;
		xRange = new Range(xData);
		yRange = new Range(yData);
		propRange = propData == null ? null : new Range(propData);
	}

	public String toString() 
	{
		StringBuilder sb = new StringBuilder();
		for (int i=0; i<xData.length; i++)
		{
			sb.append(xData[i] + "," + yData[i] + (propData != null ? "," + propData[i] : "") + "\n");
		}
		return sb.toString();
	}
	
	public Plot(String text) throws Exception
	{
		List<String> lines = TextFileUtil.loadList(new ByteArrayInputStream(text.getBytes()));
		xData = new float[lines.size()];
		yData = new float[lines.size()];
		propData = TextFileUtil.split(lines.get(0), ",").size() > 2 ? new float[lines.size()] : null;
		
		for (int i=0; i<lines.size(); i++)
		{
			List<String> parts = TextFileUtil.split(lines.get(i), ",");
			
			xData[i] = new Float(parts.get(0));
			yData[i] = new Float(parts.get(1));
			
			if (parts.size() > 2)
			{
				propData[i] =  new Float(parts.get(2));
			}
		}
		
		xRange = new Range(xData);
		yRange = new Range(yData);
		propRange = propData == null ? null : new Range(propData);
	}
}
