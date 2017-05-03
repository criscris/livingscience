package ch.ethz.livingscience.externalsearch.mendeley;

import ch.ethz.livingscience.externalsearch.ExternalSearchProvider;
import ch.ethz.livingscience.externalsearch.ICustomSearchResult;

public class MendeleySearch implements ExternalSearchProvider 
{
	public ICustomSearchResult search(String query, boolean isAuthorRequest) 
	{
		return null;
	}
	
	public String getLabel()
	{
		return "Mendeley";
	}
}
