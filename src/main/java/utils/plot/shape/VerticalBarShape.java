package utils.plot.shape;

public class VerticalBarShape implements DotShape
{
	public float width;
	public float offsetX;
	public float zero = 0;
	
	public VerticalBarShape(float width, float offsetX) 
	{
		this(width, offsetX, 0);
	}
	
	public VerticalBarShape(float width, float offsetX, float zero) 
	{
		this.width = width;
		this.offsetX = offsetX;
		this.zero = zero;
	}
}