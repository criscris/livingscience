package utils.plot;

import utils.image.RgbColor;

public class PlotCustomProps  implements PlotProps
{
	public RgbColor color = new RgbColor(0f, 0f, 0f);
	public DataPointPlotter dataPointPlotter;
	
	public PlotCustomProps(DataPointPlotter dataPointPlotter)
	{
		this.dataPointPlotter = dataPointPlotter;
	}
	
	public RgbColor getColor() 
	{
		return color;
	}
}
