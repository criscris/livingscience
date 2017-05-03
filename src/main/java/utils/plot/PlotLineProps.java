package utils.plot;

import utils.image.RgbColor;

public class PlotLineProps implements PlotProps
{
	public RgbColor color;
	public float alpha = 1f;
	public float strokeWidth = 2f;

	public RgbColor getColor() 
	{
		return color;
	}

	public PlotLineProps() 
	{
		this(new RgbColor(0f, 0f, 1f));
	}
	
	public PlotLineProps(RgbColor color) 
	{
		this.color = color;
	}
	
	
}
