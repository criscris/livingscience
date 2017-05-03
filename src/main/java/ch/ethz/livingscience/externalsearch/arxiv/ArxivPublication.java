package ch.ethz.livingscience.externalsearch.arxiv;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import ch.ethz.livingscience.externalsearch.ICustomSearchDocument;
import ch.ethz.livingscience.externalsearch.ICustomSearchResult;



public class ArxivPublication implements Serializable, ICustomSearchDocument
{
	private static final long serialVersionUID = 8433166455469796508L;
	
	public String arxivID; // after 2007-04-01: yymm.####, before: astro-ph/9204001
	
	public List<String> authorNames;
	public List<String> categories;
	public Date publishedDate;
	public Date updatedDate;
	public String title;
	public String summary;
	
	public String doi;
	public String journalRef;
	
	public ArxivPublication()
	{
		
	}
	
	public String getUrl()
	{
		return "http://arxiv.org/abs/" + arxivID;
	}
	
	public String getTitle()
	{
		return title;
	}

	public List<String> getAuthors()
	{
		return authorNames;
	}

	private static SimpleDateFormat formatYear = new SimpleDateFormat("yyyy");
	public int getYear()
	{
		return new Integer(formatYear.format(publishedDate));
	}

	public String getDoi()
	{
		return doi;
	}

	public String getJournal()
	{
		return journalRef;
	}
	
//	public JavaScriptObject toJSO()
//	{
//		JavaScriptObject jso = JavaScriptObject.createObject();
//		
//		
//		return jso;
//	}

	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append(arxivID);
		sb.append(";");
		sb.append(title);
		sb.append(";");
		for (int i=0; i<authorNames.size(); i++)
		{
			sb.append(authorNames.get(i));
			if (i < authorNames.size() - 1) sb.append(", ");
		}
		sb.append(";");
		for (int i=0; i<categories.size(); i++)
		{
			sb.append(categories.get(i));
			if (i < categories.size() - 1) sb.append("|");
		}
		sb.append(";");
		sb.append(publishedDate);
		sb.append(";");
		sb.append(updatedDate);
		sb.append(";");
		sb.append(doi);
		sb.append(";");
		sb.append(journalRef);
		sb.append(";");
		sb.append(summary);
		
		return sb.toString();
	}
	
	ICustomSearchResult data;
	public ICustomSearchResult getSearchProvider()
	{
		return data;
	}
	
	public void setSearchProvider(ICustomSearchResult data, int origIndex)
	{
		this.data = data;
		this.origIndex = origIndex;
	}

	private int origIndex;
	public int getOrigIndex()
	{
		return origIndex;
	}

	public String getSummary()
	{
		return summary;
	}
	
	boolean isUsed = true;
	public boolean isUsed()
	{
		return isUsed;
	}

	public void setUsed(boolean isUsed)
	{
		this.isUsed = isUsed;
	}
}