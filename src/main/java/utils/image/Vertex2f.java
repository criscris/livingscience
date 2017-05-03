package utils.image;

import utils.math.Vector2f;

public class Vertex2f 
{
	public Vector2f pos;
	public Vector2f tex;
	
	public Vertex2f()
	{
		
	}
	
	public Vertex2f(Vector2f pos, Vector2f tex) 
	{
		this.pos = pos;
		this.tex = tex;
	}
}
