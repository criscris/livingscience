package ch.ethz.livingscience.externalsearch.arxiv;

import java.io.Serializable;
import java.util.List;

import ch.ethz.livingscience.externalsearch.ICustomSearchDocument;
import ch.ethz.livingscience.externalsearch.ICustomSearchResult;



public class ArxivData implements ICustomSearchResult, Serializable
{
	public static final String name = "ArXiv";
	
	private static final long serialVersionUID = 4487000492105589382L;

	public int totalResults;
	public List<ArxivPublication> docs;
	
	public ArxivData()
	{
		
	}
	
	public List<? extends ICustomSearchDocument> getResults()
	{
		return docs;
	}

	public int getTotalResults()
	{
		return totalResults;
	}
	
	public String getName()
	{
		return name;
	}
	
	public Object getSource()
	{
		return this;
	}
}
