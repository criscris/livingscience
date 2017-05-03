package utils.image;


public class TexturedQuad 
{
	public Vertex2f v1; // LB
	public Vertex2f v2; // RB
	public Vertex2f v3; // RU
	public Vertex2f v4; // LU
	
	public BgrByteImage texture;

	public TexturedQuad() 
	{
		v1 = new Vertex2f();
		v2 = new Vertex2f();
		v3 = new Vertex2f();
		v4 = new Vertex2f();
	}
}
