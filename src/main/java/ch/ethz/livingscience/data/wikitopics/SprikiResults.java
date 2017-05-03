package ch.ethz.livingscience.data.wikitopics;

import java.io.Serializable;
import java.util.List;

public class SprikiResults implements Serializable
{
	private static final long serialVersionUID = -4429667957431418145L;
	
	public String query;
	public List<SprikiResult> publications;
	public int totalResults;
}
