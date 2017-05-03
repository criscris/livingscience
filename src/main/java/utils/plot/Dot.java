package utils.plot;


public class Dot 
{
	int xd;
	int yd;
	float alpha;
	
	public Dot(int xd, int yd)
	{
		this(xd, yd, 1f);
	}
	
	public Dot(int xd, int yd, float alpha)
	{
		this.xd = xd;
		this.yd = yd;
		this.alpha = alpha;
	}
	
	public static final Dot[] dots3x3 = generateSquare(1);
	
	public static final Dot[] generateSquare(int radius)
	{
		int length = radius*2 + 1;
		Dot[] dots = new Dot[length * length];
		int index = 0;
		for (int yi=-radius; yi<=radius; yi++)
		{
			for (int xi=-radius; xi<=radius; xi++)
			{
				dots[index] = new Dot(xi, yi);
				index++;
			}
		}
		
		return dots;
	}
	
	public static final Dot[] genrateVerticalLine(int radius)
	{
		int length = radius*2 + 1;
		Dot[] dots = new Dot[length];
		
		int index = 0;
		for (int yi=-radius; yi<=radius; yi++)
		{
			dots[index] = new Dot(0, yi);
			index++;
		}
		
		return dots;
	}
	
	public static final Dot[] generateRect(int radiusX, int radiusY)
	{
		int length = (radiusX*2 + 1)*(radiusY*2 + 1);
		
		Dot[] dots = new Dot[length];
		int index = 0;
		for (int yi=-radiusY; yi<=radiusY; yi++)
		{
			for (int xi=-radiusX; xi<=radiusX; xi++)
			{
				dots[index] = new Dot(xi, yi);
				index++;
			}
		}
		
		return dots;
	}
	
	
}
