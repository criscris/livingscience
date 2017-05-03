package utils.image;


public class ColorMapper
{
    public static final ColorMap[] defaultColors = new ColorMap[] {
        new ColorMap(0.02f, new RgbColor(0.0f, 0.0f, 0.5f)),
        new ColorMap(0.05f, new RgbColor(0.0f, 0.0f, 1.0f)),
        new ColorMap(0.1f, new RgbColor(0.0f, 1.0f, 1.0f)),
        new ColorMap(0.2f, new RgbColor(1.0f, 1.0f, 0.0f)),
        new ColorMap(0.5f, new RgbColor(1.0f, 0.0f, 0.0f)),
        new ColorMap(0.8f, new RgbColor(0.5f, 0.0f, 0.0f)) };
    
    public ColorMap[] colors;
    public ColorMapper(ColorMap[] colors)
    {
    	this.colors = colors;
    }
	
    RgbColor temp = new RgbColor();
	public final RgbColor map(float value)
	{
		if (value <= colors[0].minValue) temp.set(colors[0].rgb);
		else if (value >= colors[colors.length - 1].minValue) temp.set(colors[colors.length - 1].rgb);
		else
		{
			int i=1;
			for (; i<colors.length - 1; i++)
			{
				if (value < colors[i].minValue) break;
			}
			temp.blend(colors[i].rgb, colors[i-1].rgb, (value - colors[i-1].minValue) / (colors[i].minValue - colors[i-1].minValue));
		}

		return temp;
	}
}
