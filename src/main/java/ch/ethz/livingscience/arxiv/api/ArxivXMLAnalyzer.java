package ch.ethz.livingscience.arxiv.api;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.xml.sax.helpers.XMLReaderFactory;

import ch.ethz.livingscience.arxiv.ArxivCitation;
import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Elements;
import nu.xom.Node;
import nu.xom.Nodes;
import nu.xom.XPathContext;
import utils.text.CountableSet;

/**
 * 
 * the arxiv xml metadata contains only entries for the most recent version of a publication
 * (the update date is also used for the request date period).
 * 
 * 
 * 	872346 total entries. 79556 ms.
	871070 ids.
	102 dangerous dois.
	journal_ref: 391402
	doi: 398716
	comment: 767007
	summary: 872346
	id: 872346
	title: 872346
	primary_category: 872346
	updated: 872346
	published: 872346
	category: 1476448
	link: 2143408
	author: 2998158
	
	406102 affiliations. 66963 unique affiliations.
	
	containing "zurich":
	1529 affiliations. 195 unique affiliations.
 *
 */
public class ArxivXMLAnalyzer 
{
	boolean writeSummaries = true;
	
	public static void main(String[] args) throws Exception
	{
		//MANUAL: directory
		new ArxivXMLAnalyzer().execAll(
				new File("D:/LivingScience/Data/arxiv/xml201309_meta"),
				new File("D:/LivingScience/Data/arxiv"));
	}
	
	
	public static List<File> getFilesOfDir(File dir, final String ending)
	{
		File[] files_ = dir.listFiles(new FilenameFilter() 
		{
			public boolean accept(File dir, String name) 
			{
				return name.endsWith(ending);
			}
		});
		List<File> files = new ArrayList<>();
		for (File f : files_) files.add(f);
		Collections.sort(files);
		
		return files;
	}
	
	
	private XPathContext ctx = new XPathContext("feed", "http://www.w3.org/2005/Atom");
	public void execAll(File xmlDir, File outputDir) throws Exception
	{
		List<File> xmlFiles = getFilesOfDir(xmlDir, ".xml");
		System.out.println(xmlFiles.size() + " files.");
		
		int countEntries = 0;
		long time = System.currentTimeMillis();
		
		BufferedWriter writer = new BufferedWriter(new FileWriter(new File(outputDir, "arxivmeta" + (writeSummaries ? "_summaries" : "") + ".txt")));
		
		for (int i=0; i<xmlFiles.size(); i++)
		{
		    Builder bob = new Builder(XMLReaderFactory.createXMLReader());
		    
		    try
		    {
				Document doc = bob.build(xmlFiles.get(i));
				Nodes nodes = doc.query("//*/feed:entry", ctx);
				
				System.out.println((i+1) + " of " + xmlFiles.size() + ": " + nodes.size() + " entries.");
				
				countEntries += nodes.size();
				
				for (int j=0; j<nodes.size(); j++)
				{
					ArxivCitation pub = parseEntry((Element) nodes.get(j));
					if (pub != null)
					{
						writer.write(pub.toString());
						if (writeSummaries && pub.summary != null) writer.write(";" + pub.summary);
						writer.write("\n");
					}
				}
		    }
		    catch (Exception ex)
		    {
		    	ex.printStackTrace();
		    }

		}
		
		writer.close();
		
		System.out.println(countEntries + " total entries. " + (System.currentTimeMillis() - time) + " ms.");
		System.out.println(ids.size() + " ids.");
//		System.out.println(Arrays.toString(versions.getCounts()));
		System.out.println(dangerousDois + " dangerous dois.");
		
//		tags.debugSorted();
		
//		affiliations_zurich.debugSorted();
//		System.out.println(affiliations_zurich.totalCount() + " affiliations. " + affiliations_zurich.counts.size() + " unique affiliations.");
	}
	
	Set<String> ids = new HashSet<>();
	CountableSet<String> tags = new CountableSet<>();
//	CountableSet<String> affiliations_zurich = new CountableSet<>();
//	IntHistogram versions = new IntHistogram(100);
	
	SimpleDateFormat arxivDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'"); // 1996-09-20T11:09:26Z
	
	static final String idStart = "http://arxiv.org/abs/";
	int dangerousDois = 0;
	
	ArxivCitation parseEntry(Element entry) throws Exception
	{
		Elements children = entry.getChildElements();
		
		ArxivCitation pub = new ArxivCitation();
		boolean add = false;
		
		for (int e=0; e<children.size(); e++)
		{
			Element child = children.get(e);
			String tagName = child.getLocalName();
			tags.add(tagName);
			
			switch (tagName)
			{
			case "id":
				String id = getIDFromXMLElem(child);
				if (id == null) continue;
				add = ids.add(id);
				pub.arxivID = id.replace("/", ""); // pdf/tex names doesn't have a slash which was limited subject from number before 2007
			break;
			case "doi":
				String[] dois = child.getValue().trim().split(" ");
				pub.doi = dois[0];
				if (pub.doi.length() < 10 || pub.doi.contains(ArxivCitation.sep) || pub.doi.contains("\n"))
				{
					System.out.println("Dangerous doi: " + pub.doi);
					pub.doi = "";
					dangerousDois++;
				}
			break;
			case "updated":
				String date = child.getValue();
				Date d = arxivDateFormat.parse(date);
				
				Calendar cal = Calendar.getInstance();
				cal.setTime(d);
				pub.year = cal.get(Calendar.YEAR);
			break;
			case "journal_ref":
				pub.journal = child.getValue().replaceAll(";", ",").replaceAll("\n", "");
			break;
			case "title":
				pub.title = child.getValue().replaceAll(";", ",").replaceAll("\n", "");
			break;
			case "summary":
				pub.summary = child.getValue().replaceAll(";", ",").replaceAll("\n", " ").replace("  ", " ").trim();
			break;
			case "author":
				Elements authorChilds = child.getChildElements();
				pub.authors.add(authorChilds.get(0).getValue().replaceAll(";", ",").replaceAll("\n", ""));
//				if (authorChilds.size() > 1)
//				{
//					String affiliation = authorChilds.get(1).getValue().replaceAll(";", ",").replaceAll("\n", " ").replace("  ", " ").trim().toLowerCase();
//					if (affiliation.contains("zurich")) affiliations_zurich.add(affiliation);
//				}
			break;
			}
		}
		
		return add ? pub : null;
	}
	
	public static String getIDFromXMLElem(Node idElem)
	{
		String id = idElem.getValue();
		
		if (!id.startsWith(idStart)) return null;
		id = id.substring(idStart.length());
		id = removeVersionFromID(id);

		return id.replace("/", "");
	}
	
	public static String removeVersionFromID(String id)
	{
		int v = id.lastIndexOf("v");
		if (v >= id.length() - 3)
		{
			try
			{
//				int version = 
						new Integer(id.substring(v + 1));	
				id = id.substring(0, v);
			}
			catch (Exception ex)
			{
				System.out.println("could not remove arxiv version: " + id);
			}	
		}
		return id;
		
	}
	
	public static String addSlashToIDifOldID(String id)
	{
		if (id.contains("/")) return id;
		if (id.contains(".") && id.length() == 9) return id;
		
		int i = 0;
		for (; i<id.length(); i++)
		{
			if (Character.isDigit(id.charAt(i))) break;
		}
		
		return id.substring(0, i) + "/" + id.substring(i); 
	}
}
