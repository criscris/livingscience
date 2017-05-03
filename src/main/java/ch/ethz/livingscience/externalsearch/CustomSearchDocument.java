package ch.ethz.livingscience.externalsearch;

import java.util.ArrayList;
import java.util.List;

public class CustomSearchDocument implements ICustomSearchDocument
{
	public String title;
	public List<String> authors = new ArrayList<>();
	public int year;
	public String doi;
	public String url;
	public String journal;
	public ICustomSearchResult result;
	public String summary;

	public String getTitle() 
	{
		return title;
	}

	public List<String> getAuthors() 
	{
		return authors;
	}

	public int getYear() 
	{
		return year;
	}

	public String getDoi() 
	{
		return doi;
	}

	public String getUrl() 
	{
		return url;
	}

	public String getJournal() 
	{
		return journal;
	}

	public ICustomSearchResult getSearchProvider() {

		return result;
	}

	public String getSummary() 
	{
		return summary;
	}
}
