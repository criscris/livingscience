package ch.ethz.livingscience.externalsearch.arxiv;

import ch.ethz.livingscience.externalsearch.ExternalSearchProvider;
import ch.ethz.livingscience.externalsearch.ICustomSearchResult;

public class ArxivSearch implements ExternalSearchProvider
{
	public ICustomSearchResult search(String query, boolean isAuthorRequest) 
	{
		try 
		{
			return new ArxivAPIReader().query(query, 20, isAuthorRequest);
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
		return null;
	}
	
	public String getLabel()
	{
		return ArxivData.name;
	}
}
