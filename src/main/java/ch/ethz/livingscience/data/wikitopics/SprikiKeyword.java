package ch.ethz.livingscience.data.wikitopics;

import java.io.Serializable;

public class SprikiKeyword implements Serializable
{
	private static final long serialVersionUID = 257228890697286865L;

	public String label;
	public int occurrences;
	
	// position on relations field
	public float x;
	public float y;
	public transient int pcaIndex;
	
	public SprikiKeyword()
	{
		
	}
}
