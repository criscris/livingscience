package ch.ethz.livingscience.externalsearch.plos;

import ch.ethz.livingscience.externalsearch.ExternalSearchProvider;
import ch.ethz.livingscience.externalsearch.ICustomSearchResult;

public class PlosSearch implements ExternalSearchProvider 
{
	public ICustomSearchResult search(String query, boolean isAuthorRequest) 
	{
		return null;
	}
	
	public String getLabel()
	{
		return "PLoS";
	}
}
