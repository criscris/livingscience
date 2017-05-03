package utils.image;

import utils.math.Vector2f;
import utils.math.Vector3f;

public class Vertex3f 
{
	public Vector3f pos;
	public Vector2f tex;
	
	public Vertex3f()
	{
		
	}
	
	public Vertex3f(Vector3f pos, Vector2f tex) 
	{
		this.pos = pos;
		this.tex = tex;
	}
}
