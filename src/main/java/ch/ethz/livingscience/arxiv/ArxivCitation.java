package ch.ethz.livingscience.arxiv;

import java.util.ArrayList;
import java.util.List;

import utils.text.TextFileUtil;

/**
 * publication metadata OR parsed reference data
 *
 */
public class ArxivCitation 
{
	public String containingArxivID; // optional, this is the document where the reference is from
	
	public String arxivID; // optional, only few references have an explicit link to another arxiv publication
	public String doi; // optional, if different from arxivID
	
	public int year;
	
	public String journal; // for arxiv metadata, it contains already (issue?), volume and pages
	public String issue;
	public String volume;
	public String pages;
	
	public String title;
	public List<String> authors;
	
	public String summary; // optional
	
	
	public ArxivCitation()
	{
		containingArxivID = "";
		arxivID = "";
		doi = "";
		
		year = -1;
		
		journal = "";
		issue = "";
		volume = "";
		pages = "";
		
		title = "";
		authors = new ArrayList<>();
	}
	
	
	public ArxivCitation(String line)
	{
		List<String> parts = TextFileUtil.split(line, sep);

		
		containingArxivID = parts.get(0);
		arxivID = parts.get(1);
		doi = parts.get(2);
		
		year = new Integer(parts.get(3));
		
		journal = parts.get(4);
		issue = parts.get(5);
		volume = parts.get(6);
		pages = parts.get(7);
		
		title = parts.get(8);
		
		int noOfAuthors = new Integer(parts.get(9));
		
		authors = new ArrayList<>();
		for (int i=0; i<noOfAuthors; i++)
		{
			authors.add(parts.get(10 + i));
		}
		
		if (parts.size() > 10 + noOfAuthors)
		{
			summary = parts.get(10 + noOfAuthors);
		}
	}

	public static final String sep = ";";
	public String toString() 
	{
		return containingArxivID + sep + arxivID + sep + doi + sep + year + sep + journal + sep + issue + sep + volume + sep + pages + sep + title + sep + authors.size() + TextFileUtil.listToString(authors, sep);
	}
}
