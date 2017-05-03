package utils.net;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.XPathContext;
import utils.io.BinaryFileUtil;

import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

public class Browser
{
	public XPathContext ctx = new XPathContext("html", "http://www.w3.org/1999/xhtml");
	private Proxy proxy;
	
	public void setProxy(Proxy proxy)
	{
		this.proxy = proxy;
	}
	
	public String getString(URL url) throws Exception
	{
		HttpURLConnection urlConn = (HttpURLConnection) (proxy == null ? url.openConnection() : url.openConnection(proxy));
		urlConn.setConnectTimeout(40000);
		urlConn.setReadTimeout(40000);
		urlConn.setRequestMethod("GET");
		urlConn.addRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:20.0) Gecko/20100101 Firefox/20.0");
		
//		urlConn.addRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
//		urlConn.addRequestProperty("Accept-Language", "en-us,en;q=0.5");
//		urlConn.addRequestProperty("Accept-Encoding", "gzip, deflate");
//		urlConn.addRequestProperty("Accept-Charset", "utf-8");
		
//		for (Entry<String, List<String>> entry : urlConn.getHeaderFields().entrySet())
//		{
//			StringBuilder sbh = new StringBuilder();
//			
//			List<String> values = entry.getValue();
//			sbh.append(values.get(0));
//			for (int i=1; i<values.size(); i++)
//			{
//				sbh.append(";");
//				sbh.append(values.get(i));
//			}
//			
//			System.out.println("response: " + entry.getKey() + ": " +  sbh.toString());
//			
//		}
		
		InputStream is = urlConn.getInputStream();
		
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		StringBuilder sb = new StringBuilder();
		String line = null;
		long time = System.currentTimeMillis();
		while ((line = reader.readLine()) != null)
		{
			sb.append(line);
			sb.append("\n");
			if (System.currentTimeMillis() > time + 12000) break;
		}

		reader.close();
		
		return sb.toString();
	}
	
	public String getString(URL url, Map<String, String> responseHeader) throws Exception
	{
		HttpURLConnection urlConn = (HttpURLConnection) (proxy == null ? url.openConnection() : url.openConnection(proxy));
		urlConn.setConnectTimeout(20000);
		urlConn.setReadTimeout(20000);
		urlConn.setRequestMethod("GET");
		urlConn.addRequestProperty("User-Agent", "Mozilla/5.0 (X11; Linux x86_64; rv:6.0) Gecko/20100101 Firefox/6.0");
		
		InputStream is = urlConn.getInputStream();
		
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		StringBuilder sb = new StringBuilder();
		String line = null;
		long time = System.currentTimeMillis();
		while ((line = reader.readLine()) != null)
		{
			sb.append(line);
			sb.append("\n");
			if (System.currentTimeMillis() > time + 5000) break;
		}

		reader.close();
		
		for (Entry<String, List<String>> entry : urlConn.getHeaderFields().entrySet())
		{
			StringBuilder sbh = new StringBuilder();
			
			List<String> values = entry.getValue();
			sbh.append(values.get(0));
			for (int i=1; i<values.size(); i++)
			{
				sbh.append(";");
				sbh.append(values.get(i));
			}
			
			responseHeader.put(entry.getKey(), sbh.toString());
			
		}
		
		return sb.toString();
		

	}
	
	public String getString(URL url, Map<String, String> requestHeader, Map<String, String> responseHeader) throws Exception
	{
		HttpURLConnection urlConn = (HttpURLConnection) (proxy == null ? url.openConnection() : url.openConnection(proxy));
		urlConn.setConnectTimeout(20000);
		urlConn.setReadTimeout(20000);
		urlConn.setRequestMethod("GET");
		
		for (Entry<String, String> request : requestHeader.entrySet())
		{
			urlConn.addRequestProperty(request.getKey(), request.getValue());
		}
		
		
		InputStream is = urlConn.getInputStream();
		
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		StringBuilder sb = new StringBuilder();
		String line = null;
		long time = System.currentTimeMillis();
		while ((line = reader.readLine()) != null)
		{
			sb.append(line);
			sb.append("\n");
			if (System.currentTimeMillis() > time + 5000) break;
		}

		reader.close();
		
		for (Entry<String, List<String>> entry : urlConn.getHeaderFields().entrySet())
		{
			StringBuilder sbh = new StringBuilder();
			
			List<String> values = entry.getValue();
			sbh.append(values.get(0));
			for (int i=1; i<values.size(); i++)
			{
				sbh.append(";");
				sbh.append(values.get(i));
			}
			
			responseHeader.put(entry.getKey(), sbh.toString());
			
		}
		
		return sb.toString();
		

	}
	
	/**
	 * throws an Exception in case of a connection problem (e.g. timeout) or when response is not ok or when html could not be parsed
	 * returns successfully parsed html or null when requested resource is not html or http status is: 404 not found.
	 * 
	**/
	public Document getHTML(URL url) throws Exception
	{
		HttpURLConnection urlConn = (HttpURLConnection) (proxy == null ? url.openConnection() : url.openConnection(proxy));
		urlConn.setRequestMethod("GET");
		urlConn.addRequestProperty("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10.8; rv:20.0) Gecko/20100101 Firefox/20.0");
		
		urlConn.setConnectTimeout(20000);
		urlConn.setReadTimeout(20000);
		
		if (urlConn.getResponseCode() == 404) return null;
		if (urlConn.getResponseCode() != 200) throw new Exception("HTTP Status: " + urlConn.getResponseCode() + " " + urlConn.getResponseMessage());

		boolean isHTML = false;
		for (Entry<String, List<String>> entry : urlConn.getHeaderFields().entrySet())
		{
			if ("Content-Type".equalsIgnoreCase(entry.getKey()))
			{
				for (String value : entry.getValue())
				{
					if (value.contains("html"))
					{
						isHTML = true;
					}
				}
			}
		}
		
		if (isHTML)
		{
		    XMLReader tagsoup = xmlReader == null ? XMLReaderFactory.createXMLReader() : XMLReaderFactory.createXMLReader(xmlReader);
		    Builder bob = new Builder(tagsoup);
			InputStream is = urlConn.getInputStream();
			Document doc = bob.build(is);
			is.close();
			return doc;

		}
		
		return null;
	}
	
	private ProxyList proxyList;
	private int counter = 0;
	private int currentProxy;
	private boolean reuseProxy = false;
	public int changeProxyAfter = 30; // calls
	public Document getXMLProxied(URL url) throws Exception
	{
		if (proxyList == null)
		{
			proxyList = ProxyList.load();
			resetProxyList();
			
			setProxy(proxyList.servers.get(0).getProxy());
			currentProxy = -1;
		}
		
		if (!reuseProxy)
		{
			int newProxy = currentProxy;
			for (int i=0; i<proxyList.servers.size(); i++)
			{
				newProxy++;
				if (newProxy >= proxyList.servers.size()) newProxy = 0;
				
				if (proxyList.servers.get(newProxy).works) break;
			}
			if (newProxy == currentProxy)
			{
				resetProxyList();
			}
			else
			{
				currentProxy = newProxy;
			}
		}

		ProxyServerInfo proxy = proxyList.servers.get(currentProxy);
		setProxy(proxy.getProxy());
		reuseProxy = false;
		proxy.works = false;
		long time = System.currentTimeMillis();
		Document doc = getXML(url, null);
		time = (System.currentTimeMillis() - time);
		System.out.println(proxy.toString_address() + " in " + time + " ms.");
		proxy.works = true;
		
		if (time < 8000 && counter < changeProxyAfter)
		{
//			currentProxy = currentProxy == 0 ? proxyList.servers.size() - 1 : currentProxy - 1; // next time reuse this proxy
			reuseProxy = true;
			counter++;
		}
		else
		{
			counter = 0;
		}
		
		return doc;
	}
	
	public void markCurrentProxyAsFailed()
	{
		ProxyServerInfo proxy = proxyList.servers.get(currentProxy);
		proxy.works = false;
		counter = 0;
		reuseProxy = false;
	}
	
	public void resetProxyList()
	{
		System.out.println("proxy list resetted.");
		for (ProxyServerInfo prox : proxyList.servers) prox.works = true;
		currentProxy = 0;		
	}
	
	public static final String UTF8 = "UTF-8";
	public static final String ISO88591 = "iso-8859-1";
	
	public Document getXML(URL url) throws Exception
	{
		return getXML(url, null);
	}
	
	private String xmlReader = "org.ccil.cowan.tagsoup.Parser";
	public void setXmlReader(String name)
	{
		xmlReader = name;
	}
	
	public Document getXML(URL url, String encoding) throws Exception
	{
		HttpURLConnection urlConn = (HttpURLConnection) (proxy == null ? url.openConnection() : url.openConnection(proxy));
		urlConn.setRequestMethod("GET");
		urlConn.addRequestProperty("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10.8; rv:20.0) Gecko/20100101 Firefox/20.0");
		
		urlConn.setConnectTimeout(20000);
		urlConn.setReadTimeout(20000);
////		
//		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
//		DocumentBuilder db = dbf.newDocumentBuilder();
////	
	    XMLReader tagsoup = xmlReader == null ? XMLReaderFactory.createXMLReader() : XMLReaderFactory.createXMLReader(xmlReader);
	    Builder bob = new Builder(tagsoup);
		
		
		if (encoding == null)
		{
			InputStream is = urlConn.getInputStream();
			Document doc = bob.build(is);
			is.close();
			return doc;
		}

		InputStream is0 = urlConn.getInputStream();
		BufferedReader is = new BufferedReader(new InputStreamReader(is0, encoding));
		
		Document doc = bob.build(is);
		is.close();
		return doc;
	}
	
	public Document getXML(File file, String encoding) throws Exception
	{
		FileInputStream fis = new FileInputStream(file);
		
	    XMLReader tagsoup = xmlReader == null ? XMLReaderFactory.createXMLReader() : XMLReaderFactory.createXMLReader(xmlReader);
	    Builder bob = new Builder(tagsoup);
		
		
		if (encoding == null)
		{
			Document doc = bob.build(fis);
			fis.close();
			return doc;
		}

		BufferedReader is = new BufferedReader(new InputStreamReader(fis, encoding));
		
		Document doc = bob.build(is);
		is.close();
		return doc;
	}
	
	public Document postXML(URL url, boolean isXmlHttpRequest, String referer, String cookie, String content) throws Exception
	{
		HttpURLConnection urlConn = (HttpURLConnection) (proxy == null ? url.openConnection() : url.openConnection(proxy));
		urlConn.setRequestMethod("POST");
		
		urlConn.addRequestProperty("User-Agent", "Mozilla/5.0 (X11; Linux x86_64; rv:6.0) Gecko/20100101 Firefox/6.0");
		urlConn.addRequestProperty("Accept", "text/javascript, text/html, application/xml, text/xml, */*");
		
		if (isXmlHttpRequest)
		{
			urlConn.addRequestProperty("X-Requested-With", "XMLHttpRequest");
			urlConn.addRequestProperty("X-Prototype-Version", "1.5.1");
		}

		if (referer != null) urlConn.addRequestProperty("Referer", referer);
		if (cookie != null) urlConn.addRequestProperty("Cookie", cookie);
		
		
		urlConn.setDoOutput(true);
		
		if (content != null)
		{
			BufferedWriter os = new BufferedWriter(new OutputStreamWriter(urlConn.getOutputStream()));
			os.append(content);
			os.close();
		}
		
		InputStream is = urlConn.getInputStream();
	    XMLReader tagsoup = XMLReaderFactory.createXMLReader("org.ccil.cowan.tagsoup.Parser");
	    Builder bob = new Builder(tagsoup);
	    Document doc = bob.build(is);
	    is.close();
	    return doc;
	}
	
	public void postBinary(URL url, InputStream source, Map<String, String> requestHeader) throws Exception
	{
		HttpURLConnection urlConn = (HttpURLConnection) (proxy != null ? url.openConnection(proxy) : url.openConnection());
		urlConn.setRequestMethod("POST");
		urlConn.setDoOutput(true);
		urlConn.setChunkedStreamingMode(5000000); // http://stackoverflow.com/questions/19647319/upload-files-by-post-to-server-outofmemory
		if (requestHeader != null)
		{
			for (Entry<String, String> request : requestHeader.entrySet())
			{
				urlConn.addRequestProperty(request.getKey(), request.getValue());
			}
		}
		
	    BinaryFileUtil.copy(source, urlConn.getOutputStream());	
	    
	    // must be called, otherwise no connection established.
		InputStream is = urlConn.getInputStream();
	    is.close();
	}
	
	public byte[] getBinary(URL url) throws Exception
	{
		HttpURLConnection urlConn = (HttpURLConnection) (proxy != null ? url.openConnection(proxy) : url.openConnection());
		urlConn.setRequestMethod("GET");
		urlConn.addRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:20.0) Gecko/20100101 Firefox/20.0");

	    if (urlConn.getResponseCode() == 503)
	    {
	    	System.out.println(proxy.toString());
	    }
		InputStream is = urlConn.getInputStream();
		
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		BinaryFileUtil.copy(is, bos);

		return bos.toByteArray();
	}
}
