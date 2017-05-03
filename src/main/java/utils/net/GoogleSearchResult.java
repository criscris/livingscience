package utils.net;


public class GoogleSearchResult
{
	public String title;
	public String description;
	public String url;
	
	public String toString()
	{
		return title + "\n" + url + "\n" + description;
	}
}
