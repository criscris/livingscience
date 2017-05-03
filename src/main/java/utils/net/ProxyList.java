package utils.net;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import utils.net.GoogleSearch.TimeLimit;

public class ProxyList
{
	private static final File allProxiesFile = new File("data/proxies/proxies_working_20131126.txt");
	private static final File workingProxiesFile = new File("data/proxies/proxies_working_20131126.txt"); // proxies_working.txt
	
	public List<ProxyServerInfo> servers = new ArrayList<ProxyServerInfo>();
	private Map<String, ProxyServerInfo> serverMap = new HashMap<String, ProxyServerInfo>();
	private Set<String> ips = new HashSet<String>();
	
	public static void main(String[] args) throws Exception
	{
//		long time = System.currentTimeMillis();
//		ProxyList.grabNewProxyList();
//		ProxyList.createWorkingProxiesList();
//		System.out.println("Finished in " + (System.currentTimeMillis() - time) + " ms.");
		
		debugCompareProxyLists();
	}
	
	private static void debugCompareProxyLists() throws Exception
	{
		ProxyList p1 = load(new File("proxies_working_20121011.txt"));
		ProxyList p2 = load(workingProxiesFile);
		List<ProxyServerInfo> proxies = new ArrayList<ProxyServerInfo>(p1.servers);
		proxies.addAll(p2.servers);
		
		Map<String, Integer> proxyCountMap = new HashMap<String, Integer>();
		for (ProxyServerInfo p : proxies)
		{
			String s = p.address.getAddress().getHostAddress() + ":" + p.address.getPort();
			Integer count = proxyCountMap.get(s);
			if (count == null) count = 0;
			count++;
			proxyCountMap.put(s, count);
		}
	
		int count2 = 0;
		for (Entry<String, Integer> p : proxyCountMap.entrySet())
		{
			if (p.getValue() == 2) count2++;
		}
		System.out.println(count2 + " proxies in both lists.");
	}
	
	public static void grabNewProxyList() throws Exception
	{
		Set<String> urls = new HashSet<String>(); // super-slow with java.net.URL !!!
		
		GoogleSearch googleSearch = new GoogleSearch();
		List<GoogleSearchResult> googleSearchResults = new ArrayList<GoogleSearchResult>();
		
		googleSearchResults.addAll(googleSearch.exec("proxy list 3128", TimeLimit.Month, 500));
		googleSearchResults.addAll(googleSearch.exec("proxy list 3128", TimeLimit.Week, 500));
		googleSearchResults.addAll(googleSearch.exec("proxy list 3128", TimeLimit.Day, 500));
		googleSearchResults.addAll(googleSearch.exec("proxy list 3128", TimeLimit.None, 500));
		googleSearchResults.addAll(googleSearch.exec("\"proxy list\" ip port", TimeLimit.Week, 500));	
		
		googleSearchResults.addAll(googleSearch.exec("site:blackhatworld.com/blackhat-seo/proxy-lists/", TimeLimit.Month, 500));
		googleSearchResults.addAll(googleSearch.exec("site:freeproxylist.free-webmaster-resources.org", TimeLimit.Month, 500));
		googleSearchResults.addAll(googleSearch.exec("site:pr0xies.org", TimeLimit.Month, 500));
		googleSearchResults.addAll(googleSearch.exec("site:2proxy.org", TimeLimit.Month, 30));
		
		for (GoogleSearchResult g : googleSearchResults)
		{
			urls.add(g.url);
//			System.out.println(g.url);
		}
		
		urls.add("http://www.blackhatworld.com/blackhat-seo/proxy-lists/297574-100-scrapebox-proxies.html");
		urls.add("http://www.blackhatworld.com/blackhat-seo/proxy-lists/297574-100-scrapebox-proxies-2.html");
		urls.add("http://www.blackhatworld.com/blackhat-seo/proxy-lists/297574-100-scrapebox-proxies-3.html");
		urls.add("http://www.blackhatworld.com/blackhat-seo/proxy-lists/297574-100-scrapebox-proxies-4.html");
		urls.add("http://www.blackhatworld.com/blackhat-seo/proxy-lists/297574-100-scrapebox-proxies-5.html");
		urls.add("http://www.blackhatworld.com/blackhat-seo/proxy-lists/297574-100-scrapebox-proxies-6.html");
		urls.add("http://www.blackhatworld.com/blackhat-seo/proxy-lists/297574-100-scrapebox-proxies-7.html");
		
		urls.add("http://www.pr0xies.org/search/label/Anonymous%20Proxy%20Servers%20Speed%20Sorted");
		urls.add("http://www.pr0xies.org/");
		urls.add("http://www.dreamproxy.net/");
		urls.add("http://elite-proxies.blogspot.com/");
		urls.add("http://elite-proxies.blogspot.com/search/label/Anonymous%20US%20Proxies");
		urls.add("http://elite-proxies.blogspot.com/search/label/L1%2FL2%2FL3%20HTTP%20Proxies");
		urls.add("http://listproxy.blogspot.com/");
		
		System.out.println(urls.size() + " unique urls.");
		
		ProxyList proxyList = new ProxyList();
		proxyList.collect(urls, allProxiesFile);
	}
	
	public static void createWorkingProxiesList() throws Exception
	{
		ProxyList proxyList = new ProxyList();
		BufferedReader reader = new BufferedReader(new FileReader(allProxiesFile));
		String line = null;
		while ((line = reader.readLine()) != null)
		{
			line = line.trim();
			if (line.length() < 1) continue;
			proxyList.servers.add(ProxyServerInfo.createFromString(line));
		}
		reader.close();
		
		proxyList.testProxies();
	}
	
	public static ProxyList load() throws Exception
	{
		return load(workingProxiesFile);
	}
	
	public static ProxyList load(File file) throws Exception
	{
		return load(new FileInputStream(file));
	}
	
	public static ProxyList load(InputStream is) throws Exception
	{
		ProxyList proxyList = new ProxyList();
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		String line = null;
		while ((line = reader.readLine()) != null)
		{
			line = line.trim();
			if (line.length() < 1) continue;
			proxyList.servers.add(ProxyServerInfo.createFromString(line));
		}
		reader.close();
		
		return proxyList;
	}
	
	private volatile int threadcount = 0;
	
	public void testProxies() throws Exception
	{
		System.out.println("testing " + servers.size() + " proxy servers.");
		final ExecutorService exec = Executors.newFixedThreadPool(10);
		
		for (int i=0; i<servers.size(); i++)
		{
			final ProxyServerInfo proxy = servers.get(i);
			
			while (threadcount > 70)
			{
				Thread.sleep(50);
			}
			
			
			exec.execute(new Runnable()
			{
//				private int trials = 0;
				
				public void run()
				{
					threadcount++;
//					trials++;
					
					try
					{
						proxy.test();
						addProgress(true);
						threadcount--;
						return;
					}
					catch (Exception ex)
					{
//						if (trials < 2) exec.execute(this);
					}
					
//					if (trials >= 2) 
						addProgress(false);
					
					threadcount--;
				}
			});
		}
		
		while (count + countFailed < servers.size() * 0.94f)
		{
			Thread.sleep(30000);
		}
		
//		System.out.println("before shutdown");
//		exec.awaitTermination(1, TimeUnit.HOURS);
//		System.out.println("awaitTermination.");
//		exec.shutdown();
		System.out.println("after shutdown");
//		exec.awaitTermination(10, TimeUnit.SECONDS);
		
		
		
		
//		final int chunks = 100;
//		final int elementsPerChunk = servers.size()/chunks + 1; 
//		Thread threads[] = new Thread[chunks];
//		for (int i=0; i<chunks; i++)
//		{
//			final int j=i*elementsPerChunk;
//			threads[i] = new Thread(new Runnable()
//			{
//				public void run()
//				{
//					doPartWork(j, Math.min(servers.size(), j + elementsPerChunk));
//				}
//			});
//			threads[i].start();
//		}
		
//		for (int i=0; i<chunks; i++)
//		{
//			threads[i].join();
//		}
		
//		while (true)
//		{
//			Thread.sleep(1000);
//			if (workDone >= servers.size() - 300) break;
//		}
//		System.out.println("after termination");
		
		class WebsiteInfo
		{
			int count;
			int sumResponseTime;
		}
		
		Map<String, WebsiteInfo> usedWebsitesMap = new HashMap<String, WebsiteInfo>();
		
		List<ProxyServerInfo> workingList = new ArrayList<ProxyServerInfo>();
		for (ProxyServerInfo proxy : servers)
		{
			if (proxy.works)
			{
				workingList.add(proxy);
				
				WebsiteInfo info = usedWebsitesMap.get(proxy.websiteURL);
				if (info == null)
				{
					info = new WebsiteInfo();
					usedWebsitesMap.put(proxy.websiteURL, info);
				}
				info.count++;
				info.sumResponseTime += proxy.responseTime;
			}
		}
		Collections.sort(workingList, new Comparator<ProxyServerInfo>()
		{
			public int compare(ProxyServerInfo o1, ProxyServerInfo o2)
			{
				return (int) (o1.responseTime - o2.responseTime);
			}
		});
		
		PrintWriter writer = new PrintWriter(new FileOutputStream(workingProxiesFile));
		for (ProxyServerInfo proxy : workingList)
		{
			writer.println(proxy);
		}
		writer.close();
		
		List<Entry<String, WebsiteInfo>> usedWebsitesList = new ArrayList<Entry<String, WebsiteInfo>>(usedWebsitesMap.entrySet());
		Collections.sort(usedWebsitesList, new Comparator<Entry<String, WebsiteInfo>>() 
		{
			public int compare(Entry<String, WebsiteInfo> o1, Entry<String, WebsiteInfo> o2)
			{
				return o2.getValue().count - o1.getValue().count;
			}
		});
		
		System.out.println("--------------------");
		for (Entry<String, WebsiteInfo> entry : usedWebsitesList)
		{
			WebsiteInfo info = entry.getValue();
			if (info.count < 2) continue;
			System.out.println(info.count + " (" + info.sumResponseTime / info.count + " ms.) " + entry.getKey());
		}
		
		System.exit(0);
		
	}
	
//	private void doPartWork(int fromIndex, int exToIndex)
//	{
//		for (int i=fromIndex; i<exToIndex; i++)
//		{
//			final ProxyServerInfo proxy = servers.get(i);
//
//				Thread thread = new Thread(new Runnable()
//				{
//					public void run()
//					{
//						try
//						{
//							proxy.test();
//							addProgress(true);
//							return;
//						}
//						catch (Exception ex)
//						{
//						}
//						addProgress(false);	
//					}
//					
//				});
//				thread.start();
//				try 
//				{
//					thread.join(300);
//					if (thread.isAlive())
//					{
//						thread.interrupt();
//						addProgress(false);
//					}
//				}
//				catch (InterruptedException e) 
//				{
//					e.printStackTrace();
//				}
//		}
//	}
	
//	private int workDone = 0;
	private int count = 0;
	private int countFailed = 0;
	private synchronized void addProgress(boolean success)
	{
//		workDone++;
		
		if (success) count++;
		else countFailed++;
		
		System.out.println("ok: " + count + "  f: " + countFailed + " t" + threadcount);
	}
	
	private boolean addProxy(String address, String websiteURL, PrintWriter writer)
	{
    	ProxyServerInfo proxy = serverMap.get(address);
    	if (proxy == null)
    	{
    		proxy = new ProxyServerInfo(address);
    		proxy.websiteURL = websiteURL;
    		serverMap.put(address, proxy);
    		servers.add(proxy);
    		ips.add(proxy.address.getAddress().getHostAddress());
    		writer.println(proxy);
    		return true;
    	}
    	return false;
	}
	
	public void collect(Collection<String> pagesWithProxies, File allProxiesFile) throws Exception
	{
		System.out.println("start collecting addresses...");
		JavaBrowser browser = new JavaBrowser();
		Pattern addressPattern = Pattern.compile("(\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}):(\\d{1,5})");
		Pattern ipPattern = Pattern.compile("(\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3})");
		
		PrintWriter writer = new PrintWriter(new FileOutputStream(allProxiesFile));
		
		for (String url : pagesWithProxies)
		{
			try
			{
				String html = browser.getString(new URL(url));
				
	            Matcher matcher = addressPattern.matcher(html);

	            int count = 0;
                while (matcher.find()) 
                {
                	String address = matcher.group();
                	if (addProxy(address, url, writer)) count++;
                }
                
                // now check for lonely IPs without port
	            matcher = ipPattern.matcher(html);
	            int count2 = 0;
                while (matcher.find()) 
                {
                	String address = matcher.group();
                	if (ips.add(address))
                	{
                    	if (addProxy(address + ":3128", url, writer)) count2++;
                	}
                }
                      
                System.out.println(count + " new and " + count2 + " lonely IPs for " + url);
				
			}
			catch (Exception ex)
			{
				System.out.println("error: " + url);
			}
		}

		writer.close();
	}
}
