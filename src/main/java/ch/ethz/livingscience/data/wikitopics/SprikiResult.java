package ch.ethz.livingscience.data.wikitopics;

import java.io.Serializable;
import java.util.List;

public class SprikiResult implements Serializable
{
	private static final long serialVersionUID = 2086693289663929164L;
	
	public String title;
	public List<String> authors;
	public int year;

	public String journal;
	public String link;
	public String summary;
	
	// position on relations field
	public float x;
	public float y;
	public transient int pcaIndex;
	
	public transient boolean isHighlighted;
	public String cssColor;
	
	public List<SprikiKeyword>  keywords;
	
	public SprikiResult()
	{
		
	}

	public SprikiResult(SprikiResult s)
	{
		this.title = s.title;
		this.authors = s.authors;
		this.year = s.year;
		this.journal = s.journal;
		this.link = s.link;
		this.summary = s.summary;
	}

	public String toString()
	{
		StringBuilder sb = new StringBuilder(title + ";" + year + ";" + journal + ";" + link);
		
		for (String author : authors)
		{
			sb.append(";");
			sb.append(author);
		}
		
		return sb.toString();
	}
}
