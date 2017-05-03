package ch.ethz.livingscience.externalsearch.scholar;

import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Nodes;
import nu.xom.XPathContext;
import utils.net.Browser;

/**
 * q=author%3A"Dirk+Helbing"
 * &as_ylo=2001&as_yhi=2011
 */
public class GoogleScholarSearch
{
	private Browser browser = new Browser();
	
	public enum GSSubject
	{
		All,
		Biology_LifeSciences_EnvironmentalScience,
		Business_Administration_Finance_Economics,
		Chemistry_MaterialsScience,
		Engineering_ComputerScience_Mathematics,
		Medicine_Pharmacology_VeterinaryScience,
		Physics_Astronomy_PlanetaryScience,
		SocialSciences_Arts_Humanities
	}
	
	public String getSubjectString(GSSubject subject)
	{
		if (subject == GSSubject.Biology_LifeSciences_EnvironmentalScience) return "bio";
		else if (subject == GSSubject.Business_Administration_Finance_Economics) return "bus";
		else if (subject == GSSubject.Chemistry_MaterialsScience) return "chm";
		else if (subject == GSSubject.Engineering_ComputerScience_Mathematics) return "eng";
		else if (subject == GSSubject.Medicine_Pharmacology_VeterinaryScience) return "med";
		else if (subject == GSSubject.Physics_Astronomy_PlanetaryScience) return "phy";
		else if (subject == GSSubject.SocialSciences_Arts_Humanities) return "soc";
		return "";
	}
	
	public static final int maxResultsPerPage = 100;
	public static String getQuery(String searchString, int resultsPerPage, int startOffset) throws UnsupportedEncodingException
	{
		return "http://scholar.google.com/scholar?as_q=" + URLEncoder.encode(searchString, "UTF-8") + 
				"&start=" + startOffset +
				"&num=" + resultsPerPage + 
				"&as_epq=&as_oq=&as_eq=&as_occt=any&as_sauthors=&as_publication=" +
				"&as_sdt=1" + 
				"&as_sdtf=&as_sdts=5&btnG=Search+Scholar&hl=en" +
				"&num=" + resultsPerPage +
				"&as_vis=1";
	}
	
	private static String getQuotedAuthor(String author)
	{
		if (!author.startsWith("\"")) author = "\"" + author;
		if (!author.endsWith("\"")) author = author + "\"";
		return author;
	}
	
	public static String getAuthorFieldQuery(String author, int resultsPerPage, int startOffset) throws UnsupportedEncodingException
	{
		author = getQuotedAuthor(author);
		
		return "http://scholar.google.ch/scholar?as_q=" +
				"&start=" + startOffset +
				"&num=" + resultsPerPage +
				"&as_epq=&as_oq=&as_eq=&as_occt=any&as_sauthors=" + URLEncoder.encode(author, "UTF-8")  +
				"&as_publication=&as_ylo=&as_yhi=&as_sdt=1.&as_sdtf=&as_sdts=5&btnG=Search+Scholar&hl=en&as_vis=1";
	}
	
	public static String getAuthorFieldQueryIncludeCitations(String author, int resultsPerPage, int startOffset) throws UnsupportedEncodingException
	{
		author = getQuotedAuthor(author);
		
		return "http://scholar.google.ch/scholar?as_q=" +
				"&start=" + startOffset +
				"&num=" + resultsPerPage +
				"&as_epq=&as_oq=&as_eq=&as_occt=any&as_sauthors=" + URLEncoder.encode(author, "UTF-8")  +
				"&as_publication=&as_ylo=&as_yhi=&as_sdt=1.&as_sdtf=&as_sdts=5&btnG=Search+Scholar&hl=en&as_vis=0";
	}
	
	public static String getJournalFieldQuery(int lowYear, int highYear, String journal, int resultsPerPage, int startOffset) throws UnsupportedEncodingException
	{
		return "http://scholar.google.ch/scholar?as_q=" +
				"&start=" + startOffset +
				"&num=" + resultsPerPage +
				"&as_epq=&as_oq=&as_eq=&as_occt=any" +
				"&as_publication=" + URLEncoder.encode(journal, "UTF-8")  +
				"&as_ylo=" + lowYear + 
				"&as_yhi=" + highYear + 
				"&as_sdt=1.&as_sdtf=&as_sdts=5&btnG=Search+Scholar&hl=en&as_vis=1";
	}
	
	public static String getCitationsQuery(String citesID, int resultsPerPage, int startOffset)
	{
//		return "http://scholar.google.ch/scholar?cites=" + citesID + "&as_sdt=2005&sciodt=1,5&hl=en&num=" + resultsPerPage + "&start=" + startOffset;
		return "http://scholar.google.com/scholar?as_vis=1&hl=en&as_sdt=2005&sciodt=0&cites=" + citesID + "&num=" + resultsPerPage + "&start=" + startOffset + "&scipsc=";
	}
	
	public GoogleScholarData execQueryNonProxy(String urlString) throws Exception
	{
		GoogleScholarData data = new GoogleScholarData();
		data.docs = new ArrayList<GoogleScholarResult>();
		
		URL url = new URL(urlString);// + "&start=" + 0);
		System.out.println(urlString);
		Document doc = browser.getXML(url);
//		System.out.println(doc.toXML());
		data.totalResults = fillResults(data.docs, doc, 0);
		
		for (int i=0; i<data.docs.size(); i++)
		{
			GoogleScholarResult pub = data.docs.get(i);
			pub.setSearchProvider(data, i);
		}
		
		return data;
	}
	
//	public GoogleScholarData[] execAllProxy(final SearchParameters[] params) throws Exception
//	{
//		return execAllProxy(params, false);
//	}
//	
//	int addCounter;
//	public GoogleScholarData[] execAllProxy(final SearchParameters[] params, final boolean searchForCitationID) throws Exception
//	{
//		ProxyList list = ProxyList.load();
//		List<ProxyServerInfo> proxies = list.servers; 
//		
////		Collections.sort(proxies, new Comparator<ProxyServerInfo>()
////		{
////			public int compare(ProxyServerInfo o1, ProxyServerInfo o2)
////			{
////				return (int) (o1.responseTime - o2.responseTime);
////			}
////		});
////		List<ProxyServerInfo> fastProxies = proxies.subList(0, Math.min(proxies.size(), 1000));
////		for (ProxyServerInfo proxy : fastProxies)
////		{
////			System.out.println(proxy.responseTime + "ms: "+ proxy);
////		}
////		System.out.println(proxies.size() + " proxies available. " + fastProxies.size() + " used.");
//		
//		
////		List<ProxyServerInfo> fastProxies = TorStarter.getLocalTorProxies();
//		
//		for (ProxyServerInfo proxy : proxies)
//		{
//			proxy.responseTime = (long) (Math.random() * 10000);
//		}
//
//		
//		BrowserProxied3 browser = new BrowserProxied3(proxies);
//		
//		final GoogleScholarData[] results = new GoogleScholarData[params.length];
////		addCounter = 0;
//		List<ArrayList<String>> urls = new ArrayList<ArrayList<String>>();
//		for (int i=0; i<params.length; i++)
//		{
//			final ArrayList<String> queryUrls = new ArrayList<String>();
//			urls.add(queryUrls);
//			
//			if (params[i] == null) continue;
//			final int index = i;
//		
//			String url = null;
//			if (searchForCitationID)
//			{
//				url = GoogleScholarSearch.getCitationsQuery(params[i].query, GoogleScholarSearch.maxResultsPerPage, 0);
//			}
//			else if (params[i].authorFieldOnly)
//			{
//				url = GoogleScholarSearch.getAuthorFieldQuery(params[i].query, GoogleScholarSearch.maxResultsPerPage, 0);
//			}
//			else
//			{
//				url = GoogleScholarSearch.getQuery(params[i].query, GoogleScholarSearch.maxResultsPerPage, 0);
//			}
//			
////			addCounter++;
////			System.out.println("queued: " + addCounter);
//			browser.getXMLAsync(url, new URL(url), "utf-8", new IBrowserProxiedListener()
//			{
//				public void onResult(String id, Document doc) throws Exception
//				{
//					System.out.println("onResult");
//					GoogleScholarData data = new GoogleScholarData();
//					data.docs = new ArrayList<GoogleScholarResult>();
//					try 
//					{
//						data.totalResults = fillResults(data.docs, doc, 0);
//
//						// fill other urls
//						if (data.totalResults > GoogleScholarSearch.maxResultsPerPage)
//						{		
//							int pagesToFetch = data.totalResults / GoogleScholarSearch.maxResultsPerPage;
//							if (GoogleScholarSearch.maxResultsPerPage * pagesToFetch < data.totalResults) pagesToFetch++;
//							
//							for (int i=1; i<=Math.min(10, pagesToFetch); i++)
//							{
//								if (searchForCitationID)
//								{
//									queryUrls.add(GoogleScholarSearch.getCitationsQuery(params[i].query, GoogleScholarSearch.maxResultsPerPage, i*GoogleScholarSearch.maxResultsPerPage));
//								}
//								else if (params[index].authorFieldOnly)
//								{
//									queryUrls.add(GoogleScholarSearch.getAuthorFieldQuery(params[index].query, GoogleScholarSearch.maxResultsPerPage, i*GoogleScholarSearch.maxResultsPerPage));
//								}
//								else
//								{
//									queryUrls.add(GoogleScholarSearch.getQuery(params[index].query, GoogleScholarSearch.maxResultsPerPage, i*GoogleScholarSearch.maxResultsPerPage));
//								}
//							}
//						}
//
//						results[index] = data;
////						addCounter--;
////						System.out.println("queued: " + addCounter);
//					} 
//					catch (Exception e)
//					{
//						e.printStackTrace();
//					}
//					
//					if (data.totalResults == -1) // not a google scholar page
//					{
//						throw new Exception();
//					}
//				}
//				
//				public void onError(String id, Exception ex)
//				{
//					System.err.println(id);
//				}
//			});
//		}
//		
////		while (browser.isProcessingRequests())
////		{
////			Thread.sleep(1000);
////		}
//		
//		browser.execAll();
//		
//		// phase 2
//		System.out.println("phase 2");
//		final int[] successCounts = new int[params.length];
//		for (int i=0; i<params.length; i++)
//		{
//			if (results[i] == null) continue;
//			if (results[i].totalResults <= GoogleScholarSearch.maxResultsPerPage) continue;
//			final ArrayList<String> queryUrls =urls.get(i);
//			final int index = i;
//			
//			for (int j=0; j<queryUrls.size(); j++)
//			{
//				String url = queryUrls.get(j);
//				
////				addCounter++;
////				System.out.println("2queued: " + addCounter);
//				browser.getXMLAsync(url, new URL(url), "utf-8", new IBrowserProxiedListener()
//				{
//					public void onResult(String id, Document doc) throws Exception
//					{
//						List<GoogleScholarResult> docs = new ArrayList<GoogleScholarResult>();
//						fillResults(docs, doc, 0);
//						results[index].docs.addAll(docs);
//						successCounts[index]++;
////						addCounter--;
////						System.out.println("2queued: " + addCounter);
//					}
//					
//					public void onError(String id, Exception ex)
//					{
//						System.err.println(id);
//					}
//				});
//			}
//		}
//		
////		while (browser.isProcessingRequests())
////		{
////			Thread.sleep(1000);
////		}
////		browser.close();
//		
//		browser.execAll();
//		
//		Thread.sleep(2000);
//		
//		// could all additional urls be fetched?
//		int count = 0;
//		for (int i=0; i<params.length; i++)
//		{
//			if (results[i] == null) continue;
//			if (urls.get(i).size() != successCounts[i])
//			{
//				results[i] = null;
//				System.out.println("incomplete: " + params[i].query);
//			}
//			else
//			{
//				count++;
//			}
//		}
//		
//		System.out.println("done google scholar search. competed requests=" + count);
//		return results;
//	}
//	
//	
//	public List<GoogleScholarResult> execQueriesProxy(List<String> urls) throws Exception
//	{
//		ProxyList list = ProxyList.load();
//		List<ProxyServerInfo> proxies = list.servers; //TorStarter.getLocalTorProxies();
//		
//		Collections.sort(proxies, new Comparator<ProxyServerInfo>()
//		{
//			public int compare(ProxyServerInfo o1, ProxyServerInfo o2)
//			{
//				return (int) (o1.responseTime - o2.responseTime);
//			}
//		});
//		List<ProxyServerInfo> fastProxies = proxies.subList(0, Math.min(proxies.size(), 50));
//		for (ProxyServerInfo proxy : fastProxies)
//		{
//			System.out.println(proxy.responseTime + "ms: "+ proxy);
//		}
//		System.out.println(proxies.size() + " proxies available. " + fastProxies.size() + " used.");
//
//		BrowserProxied2 browser = new BrowserProxied2(fastProxies);
//		
//		final List<GoogleScholarResult> results = new Vector<GoogleScholarResult>();
//		
//		for (String url : urls)
//		{
//			browser.getXMLAsync(url, new URL(url), null, new IBrowserProxiedListener()
//			{
//				public void onResult(String id, Document doc) throws Exception
//				{
//					List<GoogleScholarResult> docs = new ArrayList<GoogleScholarResult>();
//					int totalResults = -1;
//					try 
//					{
//						totalResults = fillResults(docs, doc, 0);
//					} 
//					catch (Exception e)
//					{
//						
//					}
//					
//					if (totalResults == -1) // not a google scholar page
//					{
//						throw new Exception();
//					}
//					else
//					{
//						results.addAll(docs);
//						System.out.println("results: " + results.size());
//						
//						if (docs.size() == 0)
//						{
//							System.out.println(totalResults + ", no docs: " + id);
//						}
//					}
//				}
//				
//				public void onError(String id, Exception ex)
//				{
//					System.err.println(id);
//				}
//			});
//		}
//		
//		// wait until finished
//		while (browser.isProcessingRequests())
//		{
//			Thread.sleep(1000);
//		}
//		
//		Thread.sleep(10000);
//		browser.close();
//		
//		return results;
//	}
	
	public GoogleScholarData execQueryProxy(String urlString) throws Exception
	{
		// start silvertunneltortest/SilverTunnelStart.java first as a separate program
		int i=1;
		Proxy proxy = new Proxy(Proxy.Type.SOCKS, new InetSocketAddress("127.0.0.1", 8000+i));
		browser.setProxy(proxy);
		
		for (int j=0; j<10; j++)
		{
			try
			{
				return execQueryNonProxy(urlString);
			}
			catch (Exception ex)
			{
				System.out.println("execQueryProxy failed for " + (j+1)  + " iteration.");
			}
			
			Thread.sleep(1000 + j*1000);
		}
		
		return null;
	}
	
	
	public List<GoogleScholarResult> exec(String urlString, int minCitedCount, int start, String debugDescription) throws Exception
	{
		List<GoogleScholarResult> results = new ArrayList<GoogleScholarResult>();
		
		for (int i=start/100; i<10; i++)
		{
			URL url = new URL(urlString + "&start=" + i*100);
			
			Document doc = null;
			for (int t=0; t<30; t++)
			{
				try
				{
					doc = browser.getXMLProxied(url);
					Nodes nodes = doc.query("//html:font[@size='-2']", ctx);
					if (nodes.size() == 0)
					{
						System.out.println("no google page:" + debugDescription + " " + i*100);
						browser.markCurrentProxyAsFailed();
						continue;
					}
					if (!nodes.get(0).getValue().contains("Advanced Scholar Search"))
					{
						System.out.println(nodes.get(0).getValue() + "strange google page:" + debugDescription + " " + i*100);
						browser.markCurrentProxyAsFailed();
						continue;
					}
					
					break;
				}
				catch (Exception ex)
				{
//					ex.printStackTrace();
					System.out.println("failed (" + t +"): " + debugDescription + " " + i*100);
				}
			}
			
			if (doc == null)
			{
//				failedURLs.add(url);
				break;
			}
			
			int oldResultsCount = results.size();
			
			try
			{
				fillResults(results, doc, minCitedCount);
			}
			catch (Exception ex)
			{
//				failedURLs.add(url);
			}
			if (results.size() == oldResultsCount) break; // nothing was added, so skip subsequent pages
		}
		return results;
	}
	
	public List<GoogleScholarResult> exec(String searchString, int minCitedCount, GSSubject subject, int lowYear, int highYear) throws Exception
	{
		String urlString = "http://scholar.google.com/scholar?as_q=" + URLEncoder.encode(searchString, "UTF-8") + 
				"&num=100" +
				"&as_epq=&as_oq=&as_eq=&as_occt=any&as_sauthors=&as_publication=" +
				"&as_ylo=" + lowYear + 
				"&as_yhi=" + highYear + 
				"&as_sdt=1" +
				"&as_subj=" + getSubjectString(subject) + 
				"&as_sdtf=&as_sdts=5&btnG=Search+Scholar&hl=en" +
				"&num=100" + //  100 per page 
				"&as_vis=1";
//		System.out.println(urlString);
		
		String description = searchString + " (" + lowYear + "-" + highYear + ") " + subject;
		
		return exec(urlString, minCitedCount, 0, description);

	}
	
	private XPathContext ctx = new XPathContext("html", "http://www.w3.org/1999/xhtml");
	private static final String infoSep = " - ";
	private static final String cites = "cites=";
	private static final String cited = "Cited by ";
	private static final String beforeTotalResultString = "of about ";
	private static final String beforeTotalResultString2 = " of ";
	private Pattern pattern = Pattern.compile("\\d{4} - ");
	
	/**
	 * @return total results
	 */
	public int fillResults(List<GoogleScholarResult> results, Document doc, int minCitedCount) throws Exception
	{
//		System.out.println(doc.toXML());
		
		// test
		Nodes tds = doc.query("//*/html:td[@bgcolor='#dcf6db'] ", ctx);
		String text = "";
		for (int i=0; i<tds.size(); i++)
		{
			text +=  tds.get(i).getValue();
		}
		
		int totalResults = 0;
		int t1 = text.indexOf(beforeTotalResultString);
		int l = beforeTotalResultString.length();
		if (t1 == -1) 
		{
			t1 = text.indexOf(beforeTotalResultString2);
			l = beforeTotalResultString2.length();
		}
		if (t1 != -1) 
		{			
			int t2 = text.indexOf(".", t1 + l);
			totalResults = new Integer(text.substring(t1 + l, t2).replace(",", ""));
		}
		else
		{
			Nodes newResultsDiv = doc.query("//*/html:div[@id='gs_ab_md'] ", ctx);
			if (newResultsDiv.size() == 0) return 0;
			
			String resultsText = newResultsDiv.get(0).getValue();
			// get the first number
			
			int i=0;
			for (; i<resultsText.length(); i++)
			{
				if (Character.isDigit( resultsText.charAt(i))) break;
			}
			int start = i;
			
			for (; i<resultsText.length(); i++)
			{
				if (Character.isWhitespace(resultsText.charAt(i))) break;
			}
			int endEx = i;
			
			if (endEx == start) return 0;
			
			totalResults = new Integer(resultsText.substring(start, endEx).replace(",", ""));
		}
		
//		try
//		{

//		}
//		catch (Exception ex)
//		{
//			return 0;
//		}

		
		
//		String formText = doc.query("//*/html:form[@action='/scholar']", ctx).get(0).getValue();
//		System.out.println("Formtext: " + formText);
//		int t1 = formText.indexOf(beforeTotalResultString);
//		int t2 = formText.indexOf(".", t1 + beforeTotalResultString.length());
//		int totalResults = 0;
//		try
//		{
//			totalResults = new Integer(formText.substring(t1 + beforeTotalResultString.length(), t2).replace(",", ""));
//		}
//		catch (Exception ex)
//		{
//			return 0;
//		}
//		System.out.println("total Results: " + totalResults);
		
		Nodes nodes = doc.query("//*[@class='gs_r']");
		
		for (int i=0; i<nodes.size(); i++)
		{
			GoogleScholarResult gs = new GoogleScholarResult();
			Element elem = (Element) nodes.get(i);
			try
			{
				Element titleA = (Element) elem.query("./html:div/html:h3/html:a", ctx).get(0); // previously was:   ./html:h3/html:a
				gs.title = titleA.getValue();
				gs.link = urlSemicolonEncode(titleA.getAttribute("href").getValue());
				
				Nodes otherLinkNodes = elem.query("./html:div[@class='gs_ggs gs_fl']/html:a", ctx); // previously was: ./html:span/html:a
				if (otherLinkNodes.size() > 0)
				{
					gs.link1 = urlSemicolonEncode(((Element) otherLinkNodes.get(0)).getAttribute("href").getValue());
					if (gs.link1.equals(gs.link)) gs.link1 = null;
				}
				if (otherLinkNodes.size() > 1)
				{
					String link2 = urlSemicolonEncode(((Element) otherLinkNodes.get(1)).getAttribute("href").getValue());
					if (gs.link1 == null) gs.link1 = link2;
					else gs.link2 = link2;
				}
				
	//			Element all = (Element) elem.query("./html:font", ctx).get(0); previously needed
				
//				Nodes divs = elem.query("./html:div", ctx);
				
				Element infoElem = (Element) elem.query("./html:div/html:div[@class='gs_a']", ctx).get(0); //  (Element) divs.get(1); //
				String info = infoElem.getValue();
				int i1 = info.indexOf(infoSep);
//				int i2 = info.indexOf(infoSep, i1 + infoSep.length());
				gs.authors = info.substring(0, i1); // could throw an exception if no authors found (no " - ")
				info = info.substring(i1 + infoSep.length());
	
	            Matcher matcher = pattern.matcher(info);
                matcher.find();       
                int s = matcher.start();
                int e = matcher.end();
                String journalAndYear = info.substring(0, s + 4);
//				String journalAndYear = info.substring(i1 + infoSep.length(), i2);
				
				int i3 = journalAndYear.lastIndexOf(",");
				if (i3 == -1) // no journal provided
				{
					gs.year = new Integer(journalAndYear.trim());
				}
				else
				{
					gs.journal = journalAndYear.substring(0, i3).trim();
					gs.year = new Integer(journalAndYear.substring(i3 + 1).trim());
				}
				
//				gs.publisher = info.substring(i2 + infoSep.length());
				gs.publisher = info.substring(e).trim();
				
				Element infoElem2 = (Element) elem.query("./html:div/html:div[@class='gs_fl']", ctx).get(0); // divs.get(3);
				Element citedA = (Element) infoElem2.query("./html:a", ctx).get(0);
				String citedHref = citedA.getAttribute("href").getValue();
				int i4 = citedHref.indexOf(cites);
				gs.citesID = citedHref.substring(i4 + cites.length(), citedHref.indexOf("&", i4 + cites.length()));
				String citedValue = citedA.getValue();
				int i5 = citedValue.indexOf(cited);
				gs.citations = new Integer(citedValue.substring(i5 + cited.length()).trim());
				
			//	all.removeChild(infoElem);
			//	all.removeChild(infoElem2);
				
				Element descriptionElem = (Element) elem.query("./html:div/html:div[@class='gs_rs']", ctx).get(0); // (Element) divs.get(2);
				gs.description = descriptionElem.getValue().replace("\n", " ").trim();
				gs.description = removeSemicolon(gs.description);
				if (gs.description.length() == 0) gs.description = null;
				
				gs.title = removeSemicolon(gs.title);
				gs.authors = removeSemicolon(gs.authors); 
				if (gs.journal != null) gs.journal = removeSemicolon(gs.journal);
				gs.publisher = removeSemicolon(gs.publisher);
				
				if (gs.citations >= minCitedCount) results.add(gs);
//				System.out.println(gs);
			}
			catch (Exception ex)
			{
//				ex.printStackTrace();
//				System.out.println(i + " scholar node failed:" + gs.title);
//				System.out.println(elem.toXML());
//				throw new Exception(ex);
			}
		}

		return totalResults;
	}
	
	private String removeSemicolon(String text)
	{
		return text.replace(';', ',');
	}
	
	private String urlSemicolonEncode(String url)
	{
		url = url.replace("&amp;", "&");
		if (url.indexOf(";") != -1)
		{
//			System.out.println(url);
			return url.replace(";", "%3B");
		}
		return url;
	}
	
	public static void main(String[] args) throws Exception
	{
		GoogleScholarSearch search = new GoogleScholarSearch();
		
		String url = GoogleScholarSearch.getAuthorFieldQuery("balietti", 100, 0);
		url = GoogleScholarSearch.getCitationsQuery("16067864974135868355", 100, 0);
				
		GoogleScholarData data = search.execQueryNonProxy(url);
		
		url = GoogleScholarSearch.getCitationsQuery("16067864974135868355", 100, 100);
		data.merge(search.execQueryNonProxy(url));
		
		url = GoogleScholarSearch.getCitationsQuery("16067864974135868355", 100, 200);
		data.merge(search.execQueryNonProxy(url));
		
		for (GoogleScholarResult doc : data.docs)
		{
			System.out.println(doc);
		}
		System.out.println("results: " + data.docs.size() + " totalresults=" + data.totalResults);
		
//		data =search.execQueryNonProxy(GoogleScholarSearch.getQuery("cuhiuuih", 100, 100));
//		for (GoogleScholarResult doc : data.docs)
//		{
//			System.out.println(doc);
//		}
//		System.out.println("results: " + data.docs.size());
	}
}
