package ch.ethz.livingscience.data.wikitopics;

import java.io.Serializable;
import java.util.List;

public class SprikiData implements Serializable
{
	private static final long serialVersionUID = -5463534659390593378L;
	
	public String serverErrorMessage; // is null if everything was ok.
	
	public SprikiResults results1;
	public SprikiResults results2;
	
	public List<SprikiKeyword> keywords;
	
	public SprikiData()
	{
		
	}
}
