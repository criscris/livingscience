package ch.ethz.livingscience;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import ch.ethz.livingscience.data.ProfilesDB;
import ch.ethz.livingscience.data.ProfilesSearchIndex;
import ch.ethz.livingscience.data.affiliation.UniList;
import ch.ethz.livingscience.data.wikitopics.SprikiRelations;
import ch.ethz.livingscience.externalsearch.ExternalSearchProvider;
import ch.ethz.livingscience.externalsearch.arxiv.ArxivSearch;
import ch.ethz.livingscience.externalsearch.mendeley.MendeleySearch;
import ch.ethz.livingscience.externalsearch.plos.PlosSearch;
import ch.ethz.livingscience.externalsearch.springer.SpringerSearch;
import ch.ethz.livingscience.ngrams.NGramStore2_inMemory;
import ch.ethz.livingscience.pages.AffiliationsPage;
import ch.ethz.livingscience.pages.BibtexPage;
import ch.ethz.livingscience.pages.ExternalSearchPage;
import ch.ethz.livingscience.pages.GlobalTrendsPage;
import ch.ethz.livingscience.pages.JsonPage;
import ch.ethz.livingscience.pages.NGramsPage;
import ch.ethz.livingscience.pages.ProfilePubListPage;
import ch.ethz.livingscience.pages.SearchProfilesPage;
import ch.ethz.livingscience.pages.TopicsPage;
import ch.ethz.livingscience.pages.WelcomePage;
import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.ParsingException;
import utils.Log;
import utils.io.BinaryFileUtil;
import utils.text.TextFileUtil;

public class LivingScienceServlet extends HttpServlet
{	
	private static final long serialVersionUID = 1L;
	
	File staticContentDir;
	CachedFile responseTemplate;
	CachedFile homeResTemp;
	Map<String, CachedFile> cachedFiles = new HashMap<>();
	
	ProfilesDB db;
	ProfilesSearchIndex searchIndex;
	UniList uniList;
	SprikiRelations relations;
	NGramStore2_inMemory ngramsStore;
	
	Map<String, ExternalSearchProvider> externalSearchProviders = new HashMap<>();
	
	public LivingScienceServlet(File staticContentDir, ProfilesDB db, ProfilesSearchIndex searchIndex, NGramStore2_inMemory ngramsStore) throws Exception
	{
		this.db = db;
		this.searchIndex = searchIndex;
		this.staticContentDir = staticContentDir;
		this.ngramsStore = ngramsStore;
		
		responseTemplate = new CachedFile(new File(staticContentDir, "ls.xml"));
		homeResTemp = new CachedFile(new File(staticContentDir, "home.xml"));
		
		uniList = new UniList();
		relations = new SprikiRelations(new File(staticContentDir, "data/wikiScienceTopics2012.txt"));
		
//		externalSearchProviders.put("g", new ScholarSearch());
//		externalSearchProviders.put("a", new AmazonSearch());
		externalSearchProviders.put("x", new ArxivSearch());
		externalSearchProviders.put("m", new MendeleySearch());
		externalSearchProviders.put("p", new PlosSearch());
		externalSearchProviders.put("s", new SpringerSearch());
	}
	
	void requestStart(HttpServletRequest req)
	{
		HttpSession session = req.getSession(true);
		Log.logRequest(req, session);
	}
	
	protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException 
	{
		requestStart(req);
		
		String reqURI = req.getRequestURI();
		int i0 = reqURI.indexOf("?");
		if (i0 != -1) reqURI = reqURI.substring(0, i0);
		RequestSolver rs = new RequestSolver(reqURI);
		
		if (rs.profileID != null && "pubs".equals(rs.action) && rs.rest != null)
		{
			boolean success = db.removePubFromProfile(rs.profileID, rs.rest);
			Log.log("DELETE " + rs.profileID + "/" + rs.rest + " " + success);
		}
	}

	
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException 
	{
		requestStart(req);
		
		String reqURI = req.getRequestURI();
		int i0 = reqURI.indexOf("?");
		if (i0 != -1) reqURI = reqURI.substring(0, i0);
		
		if (reqURI.startsWith("/pubs/") && reqURI.length() > 6)
		{
			String r = reqURI.substring(6);
			int i1 = r.indexOf("/");
			if (i1 != -1) r = r.substring(0, i1);
			String id = r;
			
			String json = new String(BinaryFileUtil.copy(req.getInputStream()));
			
			boolean success = db.updatePub(id, json);
			if (success) Log.log("UPDATE pubs/" + id + " is " + json);
		}
	}
	
	protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException 
	{
		requestStart(req);
	}
	
	class RequestSolver
	{
		String profileID = null;
		String action = "";
		String rest = null;
		
		public RequestSolver(String reqURI)
		{
			int i1 = reqURI.indexOf(profilesURI);
			if (i1 >= 0)
			{
				int start = i1 + profilesURI.length();
				int i2 = reqURI.indexOf("/", start);
				profileID = reqURI.substring(i1 + profilesURI.length(), i2 == -1 ? reqURI.length() : i2);
				if (i2 != -1 && i2 < reqURI.length() - 1) 
				{
					int i3 = reqURI.indexOf("/", i2 + 1);
					if (i3 != -1 && i3 < reqURI.length() - 2)
					{
						rest = reqURI.substring(i3 + 1);
					}
					
					if (i3 == -1) i3 = reqURI.indexOf("?", i2 + 1);
					if (i3 == -1) i3 = reqURI.length();
					
					action = reqURI.substring(i2 + 1, i3);
				}
			}
		}
	}

	private static final String profilesURI = "/profiles/";
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException 
	{
		String reqURI = req.getRequestURI();
		int i0 = reqURI.indexOf("?");
		if (i0 != -1) reqURI = reqURI.substring(0, i0);
	
		if ((reqURI.startsWith("/js/") && reqURI.endsWith(".js")) ||
			(reqURI.startsWith("/css/") && (reqURI.endsWith(".css") || reqURI.endsWith(".png") || reqURI.endsWith(".svg") || reqURI.endsWith(".xhtml"))))
		{
			if (reqURI.contains("..")) return;
			File file = new File(staticContentDir, reqURI);
			
			CachedFile cachedFile = cachedFiles.get(file.getAbsolutePath());
			
			if (cachedFile == null)
			{
				if (!file.exists()) return;
				
				cachedFile = new CachedFile(file);
			}
			
			byte[] data = cachedFile.getData();
			resp.setContentLength(data.length);
			
			resp.setContentType(cachedFile.getContentType());
			
			BinaryFileUtil.copy(new ByteArrayInputStream(data), resp.getOutputStream());
			return;
		}
		requestStart(req);
		
		Document doc = responseTemplate.getXMLCopy();
		Document homeDoc = homeResTemp.getXMLCopy();

		// request
		String query = req.getParameter("q");
		RequestSolver rs = new RequestSolver(reqURI);

		
		Page page = null;
		if (rs.profileID != null)
		{
			switch (rs.action)
			{
			case "addpub": 
			break;
			case "import": 
			break;
			case "externalsearch":
				List<ExternalSearchProvider> searchProviders = new ArrayList<>();
				String p = req.getParameter("providers");
				if (p != null)
				{
					List<String> ps = TextFileUtil.split(p, ",");
					for (String s : ps)
					{
						ExternalSearchProvider provider = externalSearchProviders.get(s.toLowerCase());
						if (provider != null) searchProviders.add(provider);
					}
				}
				String author = req.getParameter("author");
				boolean isAuthorSearch = author != null && author.equalsIgnoreCase("true");
				String status = req.getParameter("status");
				boolean isStatusRequest = status != null && status.equalsIgnoreCase("true");
				
				page = new ExternalSearchPage(doc, db, rs.profileID, query, searchProviders, isAuthorSearch, isStatusRequest);
			break;
			case "affiliations": 
				page = new AffiliationsPage(doc, db, rs.profileID, uniList);
			break;
			case "topics": 
				page = new TopicsPage(doc, db, rs.profileID, relations);
			break;
			case "ngrams":
				page = new NGramsPage(doc, db, rs.profileID, ngramsStore);
			break;
			case "json": 
				page = new JsonPage(doc, db, rs.profileID);
			break;
			case "bib": 
				page = new BibtexPage(db, rs.profileID);
			break;
			//new for global trends
			case "global":
				page = new GlobalTrendsPage(doc, db, rs.profileID, ngramsStore);
			break;	
			default: 
				page = new ProfilePubListPage(doc, db, rs.profileID);
			break;
			}
		}
		else if (query != null)
		{
			page = new SearchProfilesPage(doc, db, searchIndex, query);
		}
		else
		{
			page = new WelcomePage(homeDoc, searchIndex, db, relations);
		}
		
		if (page != null)
		{
			page.exec();
			
			
			page.writeResponse(resp);
		}
	}
}

class CachedFile
{
	File file;
	long lastModified;
	byte[] data;
	Document docTemplate;
	
	public CachedFile(File file) throws IOException
	{
		this.file = file;
	}
	
	public byte[] getData() throws IOException
	{
		if (lastModified != file.lastModified() || data == null)
		{
			data = BinaryFileUtil.getFileData(file);
			lastModified = file.lastModified();
		}
		
		return data;
	}
	
	public String getContentType()
	{
		int i1 = file.getName().lastIndexOf(".");
		if (i1 == -1) return "";
		String extension = file.getName().substring(i1 + 1).toLowerCase();
		
		switch (extension)
		{
		case "js": return "application/javascript;charset=utf-8";
		case "css": return "text/css;charset=utf-8";
		case "png": return "image/png";
		case "svg": return "image/svg+xml";
		case "xhtml": return "text/html;charset=utf-8";
		}
		return "";
	}
	
	public Document getXMLCopy() throws IOException
	{
		if (docTemplate == null || lastModified != file.lastModified())
		{
			try
			{
			    XMLReader tagsoup = XMLReaderFactory.createXMLReader();
			    Builder bob = new Builder(tagsoup);
				docTemplate = bob.build(new FileInputStream(file));
				lastModified = file.lastModified();
			}
			catch (SAXException | ParsingException e)
			{
				throw new IOException(e.fillInStackTrace());
			}
		}
		
		return new Document(docTemplate);
	}	
}