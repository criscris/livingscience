package ch.ethz.livingscience.arxiv.api;

import java.io.File;
import java.io.PrintStream;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import utils.io.BinaryFileUtil;
import utils.text.XmlUtil;





/**
 * takes as input arguments the date (year month day) from which the metadata should 
 * be downloaded
 */


public class ArxivAPIMetaDataLoader
{
  public static final File arxivMetaDir = new File("data/arxiv/xml_meta");
  private File saveDir;

  public static void main(String[] args) throws Exception
  {
    ArxivAPIMetaDataLoader loader = new ArxivAPIMetaDataLoader(arxivMetaDir);
    
    Calendar cal = Calendar.getInstance();
    Date today = cal.getTime();
    
    cal.set(new Integer(args[0]), new Integer(args[1]), new Integer(args[2]), 0, 0, 0);
    
    while (cal.getTime().before(today))
    {
      Date from = cal.getTime();
      cal.add(5, 1);
      Date to = cal.getTime();
      loader.downloadViaAPI(from, to);
    }
  }
  
  public static Date getDate(int year, int month)
  {
    Calendar cal = Calendar.getInstance();
    cal.set(year, month, 1, 0, 0, 0);
    return cal.getTime();
  }
  

  public ArxivAPIMetaDataLoader(File saveDir)
  {
    this.saveDir = saveDir;
  }
  
  private static final DecimalFormat decimalFormat = new DecimalFormat("000000");  
  private static final String urlStringBegin = "http://export.arxiv.org/api/query?search_query=cat:";
  private static final String urlStringMaxResults = "&max_results=";
  private static final String urlStringOffset = "&start=";
  private XmlDataGrabber xmlDataGrabber = new XmlDataGrabber();
  
  public void download(String cat, int maxDocs) throws Exception {
    boolean iterate = true;
    int offset = 0;
    while (iterate)
    {
      URL url = new URL("http://export.arxiv.org/api/query?search_query=cat:" + cat + "&max_results=" + maxDocs + "&start=" + offset);
      File file = new File(saveDir, cat + "_" + decimalFormat.format(offset) + ".xml");
      System.out.println("read " + url);
      
      if (!file.exists()) {
        xmlDataGrabber.readURL(url, file);
      }
      
      offset += maxDocs;
      

      if (file.length() < 2000L)
      {
        file.delete();
        iterate = false;
      }
    }
  }
  
  private SimpleDateFormat apiDateFormat = new SimpleDateFormat("yyyyMMdd");
  
  int noOfDownloads = 0;
  int noOfRenewals = 0;
  
  public void downloadViaAPI(Date from, Date toExl) throws Exception {
    noOfDownloads += 1;
    if (noOfDownloads < 1) { return;
    }
    Calendar cal = Calendar.getInstance();
    cal.setTime(from);
    String fromString = apiDateFormat.format(cal.getTime());
    cal.setTime(toExl);
    String toString = apiDateFormat.format(cal.getTime());
    
    URL url = new URL("http://export.arxiv.org/api/query?search_query=lastUpdatedDate:[" + fromString + "0000+TO+" + toString + "0000]&max_results=5000");
    System.out.println(noOfDownloads + " " + url);
    File file = new File(saveDir, fromString + "_" + toString + ".xml");
    
    File tmpFile = new File(saveDir, "__tempDownload.txt");
    
    xmlDataGrabber.readURL(url, tmpFile);
    
    if ((!file.exists()) || ((float)tmpFile.length() > 1.2F * (float)file.length()))
    {
      System.out.println(file.getName() + " is renewed. " + tmpFile.length() + " bytes, was " + file.length() + " bytes. noOfRenewals=" + noOfRenewals);
      BinaryFileUtil.copy(tmpFile, file);
      noOfRenewals += 1;
    }
    tmpFile.delete();
  }
  
  private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
  
  public void download(Date from, Date to) throws Exception {
    Calendar cal = Calendar.getInstance();
    cal.setTime(from);
    
    XmlDataGrabber xmlDataGrabber = new XmlDataGrabber();
    
    while (!cal.getTime().after(to))
    {
      String fromString = dateFormat.format(cal.getTime());
      cal.add(5, 1);
      String toString = dateFormat.format(cal.getTime());
      
      File file = new File(saveDir, "_update_" + fromString + ".xml");
      if (file.exists())
      {
        System.out.println("already exists: " + file.getAbsolutePath());
      }
      else
      {
        URL url = new URL("http://export.arxiv.org/oai2?verb=ListRecords&from=" + fromString + "&until=" + toString + "&metadataPrefix=arXivRaw");
        System.out.println(url);
        Element xml = xmlDataGrabber.getXML(url);
        
        List<Element> records = XmlUtil.getChildren(XmlUtil.getChild(xml, "ListRecords"), "record");
        System.out.println(fromString + " to " + toString + ", new records: " + records.size());
        
        if (records.size() == 0) { return;
        }
        List<String> ids = new ArrayList(records.size());
        for (Element record : records)
        {
          String id = XmlUtil.getChild(XmlUtil.getChild(XmlUtil.getChild(record, "metadata"), "arXivRaw"), "id").getTextContent();
          ids.add(id);
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append("id_list=");
        for (int i = 0; i < ids.size(); i++)
        {
          sb.append((String)ids.get(i));
          if (i < ids.size() - 1) sb.append(",");
        }
        sb.append("&max_results=5000&start=0");
        
        xmlDataGrabber.readURLPost(new URL("http://export.arxiv.org/api/query"), file, sb.toString());
      }
    }
  }
  
  public void debugXMLInfo() throws Exception {
    int totalEntries = 0;
    for (File file : saveDir.listFiles())
    {
      if (file.isFile()) {
        String fileName = file.getName();
        if (fileName.endsWith(".xml"))
        {
          DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
          DocumentBuilder builder = factory.newDocumentBuilder();
          Document document = builder.parse(file);
          
          Node root = document.getFirstChild();
          
          int entries = 0;
          NodeList children = root.getChildNodes();
          
          for (int j = 0; j < children.getLength(); j++)
          {
            Node child = children.item(j);
            String childName = child.getNodeName();
            String value = child.getTextContent();
            if (childName.equals("opensearch:totalResults"))
            {
              entries = new Integer(value).intValue();
              break;
            }
          }
          
          totalEntries += entries;
          
          System.out.println(fileName.substring(0, fileName.indexOf("_")) + ": " + entries);
        }
      }
    }
    System.out.println("totalEntries == " + totalEntries);
  }
}
