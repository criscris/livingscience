package ch.ethz.livingscience.externalsearch.arxiv;

import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Nodes;
import nu.xom.XPathContext;
import utils.net.Browser;

/**
 * see http://arxiv.org/help/api/user-manual
 * 
 * http://export.arxiv.org/api/query?search_query=
 * http://export.arxiv.org/api/query?search_query=au:"dirk helbing"
 */
public class ArxivAPIReader
{
	private static XPathContext actx = new XPathContext("atom", "http://www.w3.org/2005/Atom");
	private static XPathContext octx = new XPathContext("opensearch", "http://a9.com/-/spec/opensearch/1.1/");
	private static XPathContext ctx = new XPathContext("arxiv", "http://arxiv.org/schemas/atom");
	
	public ArxivData query(String query, int maxResults, boolean isAuthor) throws Exception
	{
		Browser browser = new Browser();
		browser.setXmlReader(null); // expecting well-formed xml
		
		String arxivQuery = null;
		if (!isAuthor)
		{
			arxivQuery =  URLEncoder.encode(query, "UTF-8");
		}
		else
		{
			if (!query.startsWith("\"")) query = "\"" + query;
			if (!query.endsWith("\"")) query = query + "\"";
			arxivQuery = "au:" +  URLEncoder.encode(query, "UTF-8");
		}
		
		String urlString = "http://export.arxiv.org/api/query?search_query=" + arxivQuery + "&max_results=" + maxResults + "&sortBy=lastUpdatedDate&sortOrder=descending";
		System.out.println("arxiv: " + urlString);
		Document xml = browser.getXML(new URL(urlString));
		
		ArxivData data = new ArxivData();
		data.totalResults = new Integer(xml.query("//*/opensearch:totalResults", octx).get(0).getValue());
		
		Nodes entries = xml.query("atom:feed/atom:entry", actx);
		data.docs = new ArrayList<ArxivPublication>();
		for (int i=0; i<entries.size(); i++)
		{
			Element entry = (Element) entries.get(i);
			
			try
			{
				ArxivPublication pub = parse(entry);
				if (pub != null)
				{
					pub.setSearchProvider(data, data.docs.size());
					data.docs.add(pub);
				}
				
			}
			catch (Exception ex)
			{
				ex.printStackTrace();
			}
		}
		
		return data;
	}
	
//	private SimpleDateFormat apiDateFormat = new SimpleDateFormat("yyyyMMdd");
//	private URL getQuery(String query, Date from, Date toExl) throws Exception
//	{
//		Calendar cal = Calendar.getInstance();
//		cal.setTime(from);
//		String fromString = apiDateFormat.format(cal.getTime());
//		cal.setTime(toExl);
//		String toString = apiDateFormat.format(cal.getTime());
//		
//		
//		URL url = new URL("http://export.arxiv.org/api/query?search_query=" + query + "%20AND%20lastUpdatedDate:[" + fromString + "0000+TO+" + toString + "0000]&max_results=5000");
//		System.out.println(url);
//		return url;
//	}
	
	private static final String idStartString = "http://arxiv.org/abs/";
	private SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
	public ArxivPublication parse(Element entry) throws Exception
	{
		ArxivPublication pub = new ArxivPublication();
		
		String id = entry.query("./atom:id", actx).get(0).getValue();
		int i1 = id.indexOf(idStartString);
		id = id.substring(i1 + idStartString.length());
		// cut off "v1" or "v2" etc.
		int i2 = id.lastIndexOf("/");
		int i3 = id.lastIndexOf(".");
		int i4 = Math.max(i2, i3);
		int i5 = id.lastIndexOf("v", i4 + 1);
		if (i5 != -1) id = id.substring(0, i5);
		pub.arxivID = id;
		
		Nodes doi = entry.query("./arxiv:doi", ctx);
		if (doi.size() > 0) pub.doi = doi.get(0).getValue();
		
		Nodes journal = entry.query("./arxiv:journal_ref", ctx);
		if (journal.size() > 0) pub.journalRef = journal.get(0).getValue().replace("\n", "").trim();
		
		pub.publishedDate = df.parse(entry.query("./atom:published", actx).get(0).getValue());
		pub.updatedDate = df.parse(entry.query("./atom:updated", actx).get(0).getValue());
		pub.title = entry.query("./atom:title", actx).get(0).getValue().replace("\n", "").trim();
		pub.summary = entry.query("./atom:summary", actx).get(0).getValue().replace("\n", " ").replace("\r", " ").replace("\r\n", " ").trim();

		pub.authorNames = new ArrayList<String>();
		Nodes authors = entry.query("./atom:author/atom:name", actx);
		for (int i=0; i<authors.size(); i++)
		{
			pub.authorNames.add(authors.get(i).getValue());
		}
		
		pub.categories = new ArrayList<String>();
		Nodes cats = entry.query("./atom:category", actx);
		for (int i=0; i<cats.size(); i++)
		{
			pub.categories.add(((Element) cats.get(i)).getAttributeValue("term").replace("\n", " "));
		}
		
		return pub;
	}
	
	public static void main(String[] args) throws Exception
	{
		ArxivData data = new ArxivAPIReader().query("Dirk Helbing", 20, true);
		System.out.println("results: " + data.docs.size() + " of " + data.totalResults);
		
		for (ArxivPublication doc : data.docs)
		{
			System.out.println(doc);
		}
	}
}
