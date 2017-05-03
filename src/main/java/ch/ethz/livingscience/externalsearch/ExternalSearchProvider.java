package ch.ethz.livingscience.externalsearch;

public interface ExternalSearchProvider 
{
	ICustomSearchResult search(String query, boolean isAuthorRequest);
	String getLabel();
}
