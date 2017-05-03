package utils.net;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Nodes;

public class GoogleSearch2
{
	public enum SearchCategory
	{
		All,
		News,
		Images,
		Video,
		NewsBlogs
	}
	
	public static SearchCategory get(String searchCategory)
	{
		if (searchCategory.equals("all")) return SearchCategory.All;
		if (searchCategory.equals("news")) return SearchCategory.News;
		if (searchCategory.equals("images")) return SearchCategory.Images;
		if (searchCategory.equals("video")) return SearchCategory.Video;
		if (searchCategory.equals("blogs")) return SearchCategory.NewsBlogs;
		
		return null;
	}
	
	public static String topLevelDomain = "com";
	
	public static String getUrl(String query) throws UnsupportedEncodingException
	{
		return "http://www.google." + topLevelDomain + "/search?client=ubuntu&channel=fs&q=" + URLEncoder.encode(query, "UTF-8");
	}
	
	public static String getUrl(String query, SearchCategory cat) throws UnsupportedEncodingException
	{
		if (cat == SearchCategory.All) return getUrl(query);
		String catString = getCatString(cat);
		
		return "http://www.google." + topLevelDomain + "/search?client=ubuntu&channel=fs&q=" + URLEncoder.encode(query, "UTF-8") + catString;
	}
	
	private static String getCatString(SearchCategory cat)
	{
		String catString = "&tbm=";
		if (cat == SearchCategory.News) catString += "nws";
		else if (cat == SearchCategory.NewsBlogs) catString += "nws&tbs=nrt:b";
		else if (cat == SearchCategory.Images) catString += "isch&biw=1680&bih=946";
		else if (cat == SearchCategory.Video) catString += "vid";
		
		return catString;
	}
	
	private static SimpleDateFormat df = new SimpleDateFormat("MM/dd/yyyy");
	public static String getUrl(String query, Date from, Date to) throws UnsupportedEncodingException
	{
		return getUrl(query, from, to, SearchCategory.All, null);
	}
	
	public static String getUrl(String query, Date from, Date to, SearchCategory cat, String sitesInThisLanguage) throws UnsupportedEncodingException
	{
		if (from == null || to == null) return getUrl(query, cat);
		return "http://www.google." + topLevelDomain + "/search?client=ubuntu&channel=fs&q=" + URLEncoder.encode(query, "UTF-8") +
				"&tbs=cdr:1%2Ccd_min%3A" + URLEncoder.encode(df.format(from), "UTF-8")  +
				"%2Ccd_max%3A" + URLEncoder.encode(df.format(to), "UTF-8") +  getCatString(cat) + (sitesInThisLanguage == null ? "" : "&lr=lang_" + sitesInThisLanguage);
	}
	
	private static final String aboutStr = "About ";
	public long getTotalResults(Document doc)
	{
		Nodes resultStatsNodes = doc.query("//*[@id='resultStats']");
		if (resultStatsNodes.size() == 0)
		{
			String htmlString = doc.toXML();
			if (htmlString.contains("No results found for")) return 0;	
			else if (htmlString.contains("did not match any documents.")) return 0;	
			else if (htmlString.contains("did not match any image results.")) return 0;
			
			return -1;
		}
		
		Element resultStatsDiv = (Element) resultStatsNodes.get(0);
		String resultStats = resultStatsDiv.getValue();
		int i1 = resultStats.indexOf(aboutStr);
		int i2 = resultStats.indexOf(" result");
		if (i1 != -1 && i2 > i1)
		{
			String resultString = resultStats.substring(i1 + aboutStr.length(), i2).trim().replace(",", "").replace(".", "");
			return new Long(resultString);
		}
		else if (i2 > 0)
		{
			String resultString = resultStats.substring(0, i2).trim().replace(",", "").replace(".", "");
			return new Long(resultString);
		}
		return -1;
	}
	
	public static void main(String[] args) throws Exception
	{
		Calendar cal = Calendar.getInstance();
		Date to = cal.getTime();
		cal.add(Calendar.YEAR, -1);
		Date from = cal.getTime();
		
		System.out.println(getUrl("test", from, to, SearchCategory.All, "de"));
	}
}
