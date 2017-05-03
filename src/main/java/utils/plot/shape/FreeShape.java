package utils.plot.shape;

public class FreeShape implements DotShape
{
	public enum FreeShapeType
	{
		Cross,
		Triangle,
		Circle,
		Quad,
		HLine
	}
	public float radius;
	public String path;
	public float strokeWidth = 0.25f;
	
	public FreeShape(FreeShapeType type, float radius)
	{
		switch (type)
		{
		case Cross: 
			path = "M-1 1 L1 -1 M-1 -1 L1 1"; 
		break;
		case Triangle: 
			float a = 2f; // length of side
			float ahalf = a / 2f;
			float h = (float) Math.sqrt(3) / 2f * a;
			float h1 = h / 3f; // center divides height 2:1
			float h2 = h1 * 2;
			path = "M0 " + (-h2) + " L" + ahalf + " " + h1 + " L" + (-ahalf) + " " + h1 + " Z"; 
		break;
		case Circle:
			path = "M 0 0 m -1, 0 a 1,1 0 1,0 2,0 a 1,1 0 1,0 -2,0";
		break;
		case Quad:
			path = "M-1 -1 L1 -1 L1 1 L-1 1 Z"; 
		case HLine:
			path = "M-1 0 L1 0"; 
		break;
		}
		
		this.radius = radius;
	}
}
