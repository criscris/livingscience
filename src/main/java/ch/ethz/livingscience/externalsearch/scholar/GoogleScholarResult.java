package ch.ethz.livingscience.externalsearch.scholar;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import ch.ethz.livingscience.externalsearch.ICustomSearchDocument;
import ch.ethz.livingscience.externalsearch.ICustomSearchResult;


public class GoogleScholarResult implements Serializable, ICustomSearchDocument
{
	private static final long serialVersionUID = -8140551840370370020L;
	
	public int citations;
	public int year;
	public String title;
	public String authors;
	public String journal; // may be null
	public String publisher;
	public String citesID;
	public String link;
	public String link1; // may be null
	public String link2; // may be null
	public String description; // may be null;
	
	public GoogleScholarResult()
	{
		
	}
	
	public int getCitations()
	{
		return citations;
	}

	public int getYear()
	{
		return year;
	}

	public String getTitle()
	{
		return title;
	}

	public String getJournal()
	{
		return journal;
	}

	public String getPublisher()
	{
		return publisher;
	}

	public String getCitesID()
	{
		return citesID;
	}

	public String getUrl()
	{
		return link;
	}

	public String getLink1()
	{
		return link1;
	}

	public String getLink2()
	{
		return link2;
	}

	public String getSummary()
	{
		return description;
	}
	
	public String getAuthorsString()
	{
		return authors;
	}

	public List<String> getAuthors()
	{
		List<String> authorNames = new ArrayList<String>();
		if (authors == null) return authorNames;
		
		String nAuthors = authors;
		if (nAuthors.endsWith("...")) nAuthors = nAuthors.substring(0, nAuthors.length() - 3);
		
		int i = nAuthors.indexOf(", ");

		while (i > 0)
		{
			authorNames.add(nAuthors.substring(0, i));
			nAuthors = nAuthors.substring(i + 2);
			i = nAuthors.indexOf(", ");
		}
		authorNames.add(nAuthors);
		
		return authorNames;
	}

	private static final String sep = ";";
	public static GoogleScholarResult createFromString(String text)
	{
		GoogleScholarResult r = new GoogleScholarResult();
		
		// StringTokenizer is not javascriptable
//		StringTokenizer st = new StringTokenizer(text, ";");
//		r.citations = new Integer(st.nextToken());
//		r.year = new Integer(st.nextToken());
//		r.title = st.nextToken();
//		r.authors = st.nextToken();
//		r.journal = st.nextToken();
//		if ("null".equals(r.journal)) r.journal = null;
//		r.publisher = st.nextToken();
//		r.citesID = st.nextToken();
//		r.link = st.nextToken();
//		r.link1 = st.nextToken();
//		if ("null".equals(r.link1)) r.link1 = null;
//		r.link2 = st.nextToken();
//		if ("null".equals(r.link2)) r.link2 = null;
//		r.description = st.nextToken();
//		if ("null".equals(r.description)) r.description = null;
		
		int[] seps = new int[10];
		seps[0] = text.indexOf(sep);
		for (int i=1; i<seps.length; i++)
		{
			seps[i] = text.indexOf(sep, seps[i-1] + 1);
		}
		r.citations = new Integer(text.substring(0, seps[0]));
		r.year = new Integer(text.substring(seps[0] + 1, seps[1]));
		r.title = text.substring(seps[1] + 1, seps[2]);
		r.authors = text.substring(seps[2] + 1, seps[3]);
		r.journal =text.substring(seps[3] + 1, seps[4]);
		if ("null".equals(r.journal)) r.journal = null;
		r.publisher =text.substring(seps[4] + 1, seps[5]);
		r.citesID = text.substring(seps[5] + 1, seps[6]);
		
		r.description = text.substring(seps[6] + 1, seps[7]);
		if ("null".equals(r.description)) r.description = null;
		
		r.link = text.substring(seps[7] + 1, seps[8]);
		r.link1 = text.substring(seps[8] + 1, seps[9]);
		if ("null".equals(r.link1)) r.link1 = null;
		r.link2 =text.substring(seps[9] + 1);
		if ("null".equals(r.link2)) r.link2 = null;

		
		return r;
	}
	
	@Override
	public String toString()
	{
		title = title.replaceAll(";", ",");
		authors = authors.replaceAll(";", ",");
		if (journal != null) journal = journal.replaceAll(";", ",");
		if (publisher != null) publisher = publisher.replaceAll(";", ",");
		if (description != null) description = description.replaceAll(";", ",");
		
		return citations + ";" + year + ";" + title + ";" + authors + ";" + journal + ";" + publisher + ";" + citesID + ";" + description + ";" + link + ";" + link1 + ";" + link2;
	}

	@Override
	public String getDoi()
	{
		return null;
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
	
	public String getPdfLink()
	{
		if (checkPdfLink(link2)) return link2;
		else if (checkPdfLink(link1)) return link1;
		else if (checkPdfLink(link)) return link;
		return null;
	}
	
	private boolean checkPdfLink(String link)
	{
		return link != null && link.endsWith(".pdf") && !link.contains("springerlink.com");
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
