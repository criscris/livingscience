package utils.plot.shape;

public class RectShape implements DotShape
{
	public float radiusX;
	public float radiusY;
	
	public RectShape(float radiusX, float radiusY) 
	{
		this.radiusX = radiusX;
		this.radiusY = radiusY;
	}
	
	public RectShape(float quadRadius)
	{
		this.radiusX = quadRadius;
		this.radiusY = quadRadius;
	}
}
