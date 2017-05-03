package ch.ethz.livingscience.pages;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletResponse;

import nu.xom.Attribute;
import nu.xom.Comment;
import nu.xom.Document;
import nu.xom.Element;

import org.json.JSONException;
import org.json.JSONObject;

import ch.ethz.livingscience.data.ProfilesDB;
import ch.ethz.livingscience.data.Publication;
import ch.ethz.livingscience.data.TextOutputPrep;
import ch.ethz.livingscience.externalsearch.ExternalSearch;
import ch.ethz.livingscience.externalsearch.ExternalSearchProvider;
import ch.ethz.livingscience.externalsearch.ICustomSearchDocument;
import ch.ethz.livingscience.externalsearch.SearchResult;

public class ExternalSearchPage extends ProfilePubListPage
{
	ProfilesDB db;
	String profileID;
	
	Request request;
	boolean isStatusRequest;
	
	public ExternalSearchPage(Document doc, ProfilesDB db, String profileID, String query, List<ExternalSearchProvider> providers, boolean isAuthorSearch, boolean isStatusRequest) throws IOException
	{
		super(doc, db, profileID);
		
		this.db = db;
		this.profileID = profileID;
		
		System.out.println("ExternalSearchPage " + providers.size() + " providers. status=" + isStatusRequest);
		
		request = RequestCache.getInstance().getRequest(query, providers, isAuthorSearch);
		this.isStatusRequest = isStatusRequest;
	}
	
	public void exec() throws IOException
	{
		if (isStatusRequest) return;
		
		loadPubs();
		
		Element searchOptions = new Element("span", ns);
		searchOptions.addAttribute(new Attribute("id", "externalSearchSetup"));
		searchOptions.appendChild(new Comment(" _ "));
		content.appendChild(searchOptions);
		
		SearchResult result = request.search.getResult();
		if (result == null)
		{
			Element wait = new Element("span", ns);
			wait.addAttribute(new Attribute("id", "externalSearchWait"));
			wait.appendChild(new Comment(" _ "));
			content.appendChild(wait);
			return;
		}
		
		Element stats = new Element("div", ns);
		stats.addAttribute(new Attribute("class", "contentEntry"));
		Element st = new Element("div", ns);
		st.addAttribute(new Attribute("class", "profileStatEntry"));
		st.appendChild(result.merged.getPubCount() + " results.");
		stats.appendChild(st);
		content.appendChild(stats);
		
		for (int p=0; p<result.merged.getPubCount(); p++)
		{
			int noOfDocs = result.merged.getPubIndicesCount(p);
			ICustomSearchDocument doc = result.allDocs.get(result.merged.getPubIndex(p, 0));
			
			String doi = doc.getDoi(); // pick up the first
			String journal = doc.getJournal(); // pick up the longest
			int year = doc.getYear(); // pick up the minimum
			String title = doc.getTitle(); // pick up the longest
			List<String> authors = doc.getAuthors(); // pick up the biggest list && full names
			String summary = doc.getSummary(); // pick up the longest
			
			List<String> searchProviders = new ArrayList<>();
			searchProviders.add(doc.getSearchProvider().getName());
			
			for (int i=1; i<noOfDocs; i++)
			{
				ICustomSearchDocument doc2 = result.allDocs.get(result.merged.getPubIndex(p, i));
				if (doi == null) doi = doc2.getDoi();
				if (doc2.getJournal() != null) if (journal == null || journal.length() < doc2.getJournal().length()) journal = doc2.getJournal();
				if (year < 100 || (doc2.getYear() > 100 && doc2.getYear() < year)) year = doc2.getYear();
				if (title == null || (doc2.getTitle() != null && doc2.getTitle().length() > title.length())) title = doc2.getTitle();
				List<String> oAuthors = doc2.getAuthors();
				if (oAuthors != null && oAuthors.size() > 0)
				{
					if (authors == null || authors.size() == 0) authors = oAuthors;
					else if (oAuthors.size() > authors.size())
					{
						String[] nameParts = oAuthors.get(0).split(" ");
						int countFullNameParts = 0;
						for (String namePart : nameParts)
						{
							if (namePart.length() > 1) countFullNameParts++;
						}
						if (countFullNameParts >= 2) authors = oAuthors; // author name seems not abbreviated
					}
				}
				String summaryO = doc2.getSummary();
				if (summary == null || (summaryO != null && summaryO.length() > summary.length())) summary = summaryO;
				
				searchProviders.add(doc2.getSearchProvider().getName());
			}
			String url = doi != null ? "http://dx.doi.org/" + doi : doc.getUrl();
			
			Publication pub = new Publication();
			pub.id = UUID.randomUUID().toString();
			pub.title = title;
			pub.url = url;
			pub.authors = authors;
			pub.year = year;
			pub.journal = journal;
			pub.summary = summary;
			
			addPublication(pub, searchProviders);
		}
	}
	
	void addPublication(Publication pub, List<String> searchProviders)
	{
		Element pubDivLeft = new Element("div", ns);
		pubDivLeft.addAttribute(new Attribute("class", "pubLeft"));
		
		Element e = new Element("a", ns);
		e.addAttribute(new Attribute("class", "pubLink"));
		e.addAttribute(new Attribute("target", "_blank"));
		e.addAttribute(new Attribute("href", pub.url));
		e.addAttribute(new Attribute("id", "title_" + pub.id));
		String t = "" + pub.title;
		e.appendChild(TextOutputPrep.prepare(t));
		pubDivLeft.appendChild(e);
		
		e = new Element("div", ns);
		e.addAttribute(new Attribute("class", "pubMeta"));
		e.addAttribute(new Attribute("id", "meta_" + pub.id));
		String meta = "";
		if (pub.authors.size() > 0)
		{
			int displayedAuthors = Math.min(pub.authors.size(), 10);
			if (pub.authors.size() == 11) displayedAuthors = 9; 
			
			meta += pub.authors.get(0);
			for (int i=1; i<displayedAuthors; i++) meta += ", " + pub.authors.get(i);
			
			int leftAuthors = pub.authors.size() - displayedAuthors;
			if (leftAuthors > 0)
			{
				meta += " ... and " + leftAuthors + " more authors";
			}
		}
		
		meta += " (" + pub.year + ")";
		if (pub.journal != null && pub.journal.length() > 0) meta += " - " + pub.journal;
		e.appendChild(TextOutputPrep.prepare(meta));
		pubDivLeft.appendChild(e);
		

		e = new Element("div", ns);
		e.addAttribute(new Attribute("class", "pubSummary pubSummary_short"));
		e.addAttribute(new Attribute("id", "summary_" + pub.id));
		String s = pub.summary;
		if (s != null)
		{
			e.appendChild(TextOutputPrep.prepare(s));
		}
		pubDivLeft.appendChild(e);
		
		e = new Element("div", ns);
		e.addAttribute(new Attribute("class", "pubSearchProviders"));
		String ps = "";
		for (int i=0; i<searchProviders.size(); i++)
		{
			if (i > 0) ps += " | ";
			ps += searchProviders.get(i);
		}
		e.appendChild(ps);
		pubDivLeft.appendChild(e);
		
		
		Element pubDiv = new Element("div", ns);
		pubDiv.addAttribute(new Attribute("class", "pubContainer"));
		
		Element table = new Element("table", ns);
		table.addAttribute(new Attribute("cellspacing", "0"));
		table.addAttribute(new Attribute("cellpadding", "0"));
		pubDiv.appendChild(table);
		
		
		Element row = new Element("tr", ns);
		row.addAttribute(new Attribute("valign", "top"));
		table.appendChild(row);
		Element col = new Element("td", ns);
		row.appendChild(col);
		col.appendChild(pubDivLeft);
		
		col = new Element("td", ns);
		row.appendChild(col);
		
		Element pubDivRight = new Element("div", ns);
		pubDivRight.addAttribute(new Attribute("class", "pubRight"));
		col.appendChild(pubDivRight);
		
		// Add
		Element action = new Element("div", ns);
		action.addAttribute(new Attribute("class", "pubAction")); // pubButtonEdit
		action.addAttribute(new Attribute("id", "add_" + pub.id));
		action.appendChild("Add");
		pubDivRight.appendChild(action);
		
		content.appendChild(pubDiv);
	}

	public void writeResponse(HttpServletResponse resp) throws IOException 
	{
		if (isStatusRequest)
		{
			resp.setContentType(jsonContent);
			resp.setStatus(HttpServletResponse.SC_OK);
			try 
			{
				JSONObject o = new JSONObject();
				o.put("status", request.search.isDone() ? "done" : "busy");
				resp.getWriter().write(o.toString(2));
			} 
			catch (JSONException e) 
			{
				throw new IOException(e.getCause());
			}
		}
		else
		{
			super.writeResponse(resp);
		}
	}
}

class RequestCache
{
	static RequestCache singleton;
	static synchronized RequestCache getInstance()
	{
		if (singleton == null) singleton = new RequestCache();
		return singleton;
	}
	
	Map<String, Request> cache = new HashMap<>();

	private RequestCache()
	{
		
	}
	
	Request getRequest(String query, List<ExternalSearchProvider> providers, boolean isAuthorSearch)
	{
		String id = query + "_" + isAuthorSearch;
		List<String> labels = new ArrayList<>();
		for (ExternalSearchProvider provider : providers) labels.add(provider.getLabel());
		Collections.sort(labels);
		for (String label : labels) id += "_" + label;
		
		Request request = cache.get(id);
		if (request == null)
		{
			request = new Request(query, providers, isAuthorSearch);
			cache.put(id, request);
		}	
		
		return request;
	}
	
	
	Map<String, Request> requestMap = new HashMap<String, Request>();
}

class Request
{
	String query; 
	List<ExternalSearchProvider> providers;
	boolean isAuthorSearch;
	
	ExternalSearch search;
	
	public Request(String query, List<ExternalSearchProvider> providers, boolean isAuthorSearch)
	{
		this.query = query;
		this.providers = providers;
		this.isAuthorSearch = isAuthorSearch;
		
		search = new ExternalSearch(providers, query, isAuthorSearch);
		search.execAsync();
	}
}
