package utils.net;

import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Node;
import nu.xom.Nodes;

public class GoogleSearch
{
	private JavaBrowser browser = new JavaBrowser();
	
	public enum TimeLimit
	{
		None,
		Year,
		Month,
		Week,
		Day,
		Hour,
	}
	
	public List<GoogleSearchResult> exec(String searchString, int maxResults) throws Exception
	{
		return exec(searchString, TimeLimit.None, maxResults);
	}
	
	public List<GoogleSearchResult> exec(String searchString, TimeLimit timelimit, int maxResults) throws Exception
	{
		List<GoogleSearchResult> results = new ArrayList<GoogleSearchResult>();
		
		String time = "";
		if (timelimit == TimeLimit.Year) time = "&tbs=qdr:y";
		else if (timelimit == TimeLimit.Year) time = "&tbs=qdr:y";
		else if (timelimit == TimeLimit.Month) time = "&tbs=qdr:m";
		else if (timelimit == TimeLimit.Week) time = "&tbs=qdr:w";
		else if (timelimit == TimeLimit.Day) time = "&tbs=qdr:d";
		else if (timelimit == TimeLimit.Hour) time = "&tbs=qdr:h";
		
		URL url = new URL("http://www.google.com/search?client=ubuntu&channel=fs&q=" + URLEncoder.encode(searchString, "UTF-8") + "&ie=utf-8&oe=utf-8" + time);
		while (results.size() < maxResults && url != null)
		{
			url = fillResultsAndGetNextPage(results, url);
//			System.out.println(url);
		}
		
//		for (GoogleSearchResult result : results)
//		{
//			System.out.println(result + "\n");
//		}
		System.out.println(results.size() + " google search results for \"" + searchString + "\" and timelimit " + timelimit + "." );

		return results;
	}
	
	
	private URL fillResultsAndGetNextPage(List<GoogleSearchResult> results, URL url) throws Exception
	{
		Document doc = browser.getXML(url);
		Nodes nodes = doc.query("//*[@class='vsc']");
		
		for (int i=0; i<nodes.size(); i++)
		{
			Node node = nodes.get(i);
//			System.out.println(node.toXML());
			
			Nodes aNodes = node.query(".//*[@shape='rect']");
			Nodes textNodes = node.query(".//*[@class='st']");
			
			GoogleSearchResult result = new GoogleSearchResult();
			Element a = (Element) aNodes.get(0);
			result.title = a.getValue();
			result.url = new URL(a.getAttribute("href").getValue()).toString();
			result.description = textNodes.get(0).getValue();
			results.add(result);
		}
		
		// is there a next button?
		Nodes nextNodes = doc.query("//*[@id='pnnext']");
		if (nextNodes.size() != 0)
		{
			Element next = (Element) nextNodes.get(0);
//			System.out.println(next.toXML());
			return new URL("http://www.google.com" + next.getAttribute("href").getValue());
		}
		
		return null;
	}
}
