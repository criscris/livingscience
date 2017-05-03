package ch.ethz.livingscience.externalsearch;

import java.util.List;

public interface ICustomSearchDocument
{
	String getTitle();
	List<String> getAuthors();
	int getYear();
	String getDoi();
	String getUrl();
	String getJournal();
	
	ICustomSearchResult getSearchProvider();
//	int getOrigIndex();
	
	String getSummary(); // optional
	
//	boolean isUsed();
//	void setUsed(boolean isUsed);
}
