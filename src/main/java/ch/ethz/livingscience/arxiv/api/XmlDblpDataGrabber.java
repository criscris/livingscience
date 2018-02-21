package ch.ethz.livingscience.arxiv.api;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class XmlDblpDataGrabber
{
	public long waitingTimeInMs = 10000;
	
	public Element getXML(URL url) throws Exception
	{
		Element root = null;

		HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();
		urlConn.setRequestMethod("GET");
		urlConn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:20.0) Gecko/20100101 Firefox/20.0");
		urlConn.setRequestProperty("HTTP_REFERER", "http://api.springer.com"); 
		urlConn.setDoOutput(true);
		
		
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		
		InputStream is = urlConn.getInputStream();
		Document doc = db.parse(is);
		root = doc.getDocumentElement();
		is.close();
		
		return root;
	}
	
	private long lastUrlConnection;
	public void readURL(URL url, File dest) throws Exception
	{
		long dt = System.currentTimeMillis() - lastUrlConnection;
		if (dt < waitingTimeInMs && dt > 0)
		{
			Thread.sleep(waitingTimeInMs - dt);
			System.out.println("wait: " + (3000 - dt) + " ms.");
		}
		
		HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();
		urlConn.setRequestMethod("GET");
		urlConn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:20.0) Gecko/20100101 Firefox/20.0");
		urlConn.addRequestProperty("HTTP_REFERER", "http://dblp.uni-trier.de/"); 
		urlConn.setDoOutput(true);
		
		InputStream is = urlConn.getInputStream();

		FileOutputStream os = new FileOutputStream(dest);
		byte[] buffer = new byte[4096];  
		int bytesRead;  
		while ((bytesRead = is.read(buffer)) != -1) 
		{  
			os.write(buffer, 0, bytesRead);  
		}   
		os.close();
		lastUrlConnection = System.currentTimeMillis();
	}
	
	public void readURLPost(URL url, File dest, String postParameters) throws Exception
	{
		long dt = System.currentTimeMillis() - lastUrlConnection;
		if (dt < waitingTimeInMs && dt > 0)
		{
			Thread.sleep(waitingTimeInMs - dt);
			System.out.println("wait: " + (3000 - dt) + " ms.");
		}
		
		HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();
		urlConn.setRequestMethod("POST");
		urlConn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:20.0) Gecko/20100101 Firefox/20.0");
		urlConn.setRequestProperty("HTTP_REFERER", "http://dblp.uni-trier.de/"); 
		urlConn.setDoOutput(true);
		
        OutputStreamWriter writer = new OutputStreamWriter(urlConn.getOutputStream());
        writer.write(postParameters);
        writer.flush();
		
		InputStream is = urlConn.getInputStream();

		FileOutputStream os = new FileOutputStream(dest);
		byte[] buffer = new byte[4096];  
		int bytesRead;  
		while ((bytesRead = is.read(buffer)) != -1) 
		{  
			os.write(buffer, 0, bytesRead);  
		}   
		os.close();
		writer.close();
		lastUrlConnection = System.currentTimeMillis();
	}
}
