package utils.plot;

import utils.image.ColorMapper;
import utils.image.RgbColor;
import utils.plot.shape.CircleShape;
import utils.plot.shape.DotShape;

public class PlotDotProps implements PlotProps
{
	public RgbColor color = new RgbColor(0f, 0f, 1f);
	public float alpha = 1f;
	
	public Dot[] dots = Dot.dots3x3; // only for raster-based plotting
	
	public DotShape shape = new CircleShape(2f); // for vector-based plotting
	
	public PlotDotProps() 
	{
		this(new RgbColor(0f, 0f, 1f));
	}
	
	public PlotDotProps(RgbColor color) 
	{
		this.color = color;
	}
	
	public RgbColor getColor() 
	{
		return color;
	}
	
	public ColorMapper colorMapper;
	public void setPropValue(float value)
	{
		if (colorMapper != null) color = colorMapper.map(value);
	}
}
