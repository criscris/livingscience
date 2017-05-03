package utils.net;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Calendar;
import java.util.Date;
import java.util.StringTokenizer;

public class ProxyServerInfo
{
	public InetSocketAddress address;
	public Date lastTested;
	public boolean works = false;
	public long responseTime;
	public String websiteURL;
	
	public Proxy.Type type = Proxy.Type.HTTP;
	
	public ProxyServerInfo(String address)
	{
		int i1 = address.indexOf(":");
		String ip = address.substring(0, i1);
		int port = new Integer(address.substring(i1 + 1));
		
		this.address = new InetSocketAddress(ip, port);
		lastTested = new Date();
	}
	
	public ProxyServerInfo(String ip, int port, Proxy.Type type)
	{
		this.address = new InetSocketAddress(ip, port);
		this.type = type;
	}
	
	public static ProxyServerInfo createFromString(String line)
	{
		StringTokenizer st = new StringTokenizer(line, ";");

		ProxyServerInfo p = new ProxyServerInfo(st.nextToken());
		p.lastTested = new Date(new Long(st.nextToken()));
		p.responseTime = new Long(st.nextToken());
		p.websiteURL = st.nextToken();
		return p;
	}
	
	public void test() throws Exception
	{
		test2(30000);
	}
	
	public void test(int timeout) throws Exception
	{
		works = false;
		lastTested = Calendar.getInstance().getTime();
		
		Proxy proxy = getProxy();
		
		// http://www.google.com/  http://209.85.147.104/ 
		URL url = new URL("http://www.google.com/search?client=ubuntu&channel=fs&q=" + URLEncoder.encode("test", "UTF-8") + "&ie=utf-8&oe=utf-8");
		
		long time = System.currentTimeMillis();
		
		HttpURLConnection urlConn = (HttpURLConnection) url.openConnection(proxy);
		urlConn.setConnectTimeout(timeout);
		urlConn.setReadTimeout(timeout);
		urlConn.setRequestMethod("GET");
		urlConn.addRequestProperty("User-Agent", "Mozilla/5.0 (X11; Linux x86_64; rv:6.0) Gecko/20100101 Firefox/6.0");
		
		InputStream is = urlConn.getInputStream();
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		String line = null;
		int found = 0;
		while ((line = reader.readLine()) != null)
		{
			if (System.currentTimeMillis() - time > timeout * 2)
			{
//				System.out.println("too long read");n
				throw new Exception("timeout");
			}
			
			if (line.contains("google")) found++;
		}
		reader.close();
		
		System.out.println("proxy test: " + found);
		if (found < 5) throw new Exception("wrong response");
		
		responseTime = System.currentTimeMillis() - time;
		works = true;
	}
	
	public void test2(int timeout) throws Exception
	{
		works = false;
		lastTested = Calendar.getInstance().getTime();
		
		Proxy proxy = getProxy();
		
		// http://www.google.com/  http://209.85.147.104/ 
		URL url = new URL("http://www.spiegel.de/");
		
		long time = System.currentTimeMillis();
		
		HttpURLConnection urlConn = (HttpURLConnection) url.openConnection(proxy);
		urlConn.setConnectTimeout(timeout);
		urlConn.setReadTimeout(timeout);
		urlConn.setRequestMethod("GET");
		urlConn.addRequestProperty("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10.8; rv:20.0) Gecko/20100101 Firefox/20.0");
		
		InputStream is = urlConn.getInputStream();
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		String line = null;
		int found = 0;
		while ((line = reader.readLine()) != null)
		{
			if (System.currentTimeMillis() - time > timeout * 2)
			{
//				System.out.println("too long read");n
				throw new Exception("timeout");
			}
			
			if (line.contains("SPIEGEL ONLINE")) found++;
		}
		reader.close();
		
//		System.out.println("proxy test: " + found);
		if (found < 1) throw new Exception("wrong response");
		
		responseTime = System.currentTimeMillis() - time;
		works = true;
	}
	
	public void test_googlescholar(int timeout) throws Exception
	{
		works = false;
		lastTested = Calendar.getInstance().getTime();
		
		Proxy proxy = getProxy();
		
		URL url = new URL("http://scholar.google.ch/");
		
		long time = System.currentTimeMillis();
		
		HttpURLConnection urlConn = (HttpURLConnection) url.openConnection(proxy);
		urlConn.setConnectTimeout(timeout);
		urlConn.setReadTimeout(timeout);
		urlConn.setRequestMethod("GET");
		urlConn.addRequestProperty("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10.8; rv:20.0) Gecko/20100101 Firefox/20.0");
		
		InputStream is = urlConn.getInputStream();
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		String line = null;
		int found = 0;
		while ((line = reader.readLine()) != null)
		{
			if (System.currentTimeMillis() - time > timeout * 2)
			{
//				System.out.println("too long read");n
				throw new Exception("timeout");
			}
			
			if (line.contains("scholar_logo_lg_2011.gif")) found++;
		}
		reader.close();
		
//		System.out.println("proxy test: " + found);
		if (found < 1) throw new Exception("wrong response");
		
		responseTime = System.currentTimeMillis() - time;
		works = true;
	}
	
	public Proxy getProxy()
	{
		return new Proxy(type, address);
	}
	
	public String toString_address()
	{
		return address.getAddress().getHostAddress() + ":" + address.getPort();
	}

	public String toString()
	{
		return address.getAddress().getHostAddress() + ":" + address.getPort() + ";" + (lastTested == null ? 0 : lastTested.getTime()) + ";" + responseTime + ";" + websiteURL;
	}
}
