package ch.ethz.livingscience.data;

import java.util.List;

public class Publication 
{
	public String id;
	
	public String url;
	public String title;
	public List<String> authors;
	public int year;
	public String journal;
	public String summary;
	
	// optional
	public String json;
	public List<String> affiliations;
}
