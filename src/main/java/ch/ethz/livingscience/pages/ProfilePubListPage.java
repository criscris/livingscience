package ch.ethz.livingscience.pages;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import nu.xom.Attribute;
import nu.xom.Document;
import nu.xom.Element;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import ch.ethz.livingscience.Page;
import ch.ethz.livingscience.data.Profile;
import ch.ethz.livingscience.data.ProfilesDB;
import ch.ethz.livingscience.data.Publication;
import ch.ethz.livingscience.data.TextOutputPrep;

public class ProfilePubListPage extends Page
{
	ProfilesDB db;
	String profileID;
	
	public ProfilePubListPage(Document doc, ProfilesDB db, String profileID) throws IOException
	{
		super(doc);
		this.db = db;
		this.profileID = profileID;
	}
	
	Profile profile;
	List<Publication> pubs;
	JSONObject json;
	
	void loadPubs() throws IOException
	{
		profile = db.getProfile(profileID);
		if (profile == null)
		{
			Element div = new Element("div", ns);
			div.appendChild("Profile not found.");
			content.appendChild(div);
			return;
		}
		
		
		pubs = new ArrayList<>();
		for (String pubID : profile.pubIDs)
		{
			Publication pub = db.getPub(pubID);
			if (pub != null) pubs.add(pub);
		}
		
		Collections.sort(pubs, new Comparator<Publication>() 
		{
			public int compare(Publication o1, Publication o2) 
			{
				return o2.year - o1.year;
			}
		});
		
		writeJSON();
		addJSONData(json, "data");
	}
	
	private void writeJSON() throws IOException
	{
		json = new JSONObject();
		
		JSONParser parser = new JSONParser();
		try 
		{
			org.json.simple.JSONObject p = (org.json.simple.JSONObject) parser.parse(profile.json);
			p.remove("pubs");
			json.put("profile", p);
		} 
		catch (ParseException | JSONException e) 
		{
			throw new IOException(e.getCause());
		}
		
		if (pubs.size() > 0)
		{
			JSONObject jpubs = new JSONObject();
			
			try 
			{
				for (int i=0; i<pubs.size(); i++)
				{
					Publication pub = pubs.get(i);
					org.json.simple.JSONObject p = (org.json.simple.JSONObject) parser.parse(pub.json);
					p.remove("_id");
					jpubs.put(pub.id, p);
				}
				
				json.put("publications", jpubs);
			} 
			catch (JSONException | ParseException e) 
			{
				throw new IOException(e.getCause());
			}
		}
	}
	
	boolean renderPubs = false;
	public void exec() throws IOException
	{
		loadPubs();
		
		Element heading = new Element("div", ns);
		heading.addAttribute(new Attribute("class", "mainHeading"));
		heading.appendChild(profile.name);
		content.appendChild(heading);
		
		Element stats = new Element("div", ns);
		stats.addAttribute(new Attribute("class", "contentEntry"));
		
		if (profile.affiliation != null && profile.affiliation.length() > 0)
		{
			Element st = new Element("div", ns);
			st.addAttribute(new Attribute("class", "profileStatEntry"));
			st.appendChild(profile.affiliation + ".");
			stats.appendChild(st);
		}
		
		Element st = new Element("div", ns);
		st.addAttribute(new Attribute("class", "profileStatEntry"));
		st.appendChild(pubs.size() + " publication" + (pubs.size() == 1 ? "" : "s") + ".");
		stats.appendChild(st);
		
		content.appendChild(stats);
		
		Element pubList = new Element("span", ns);
		pubList.addAttribute(new Attribute("id", "pubList"));
		content.appendChild(pubList);
		
		if (renderPubs)
		for (Publication pub : pubs)
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
			
			// Edit
			Element action = new Element("div", ns);
			action.addAttribute(new Attribute("class", "pubAction pubButtonEdit"));
			action.addAttribute(new Attribute("id", "edit_" + pub.id));
			action.appendChild("Edit");
			pubDivRight.appendChild(action);
			
			// Remove
			action = new Element("div", ns);
			action.addAttribute(new Attribute("class", "pubAction pubButtonRemove"));
			action.addAttribute(new Attribute("id", "remove_" + pub.id));
			action.appendChild("Remove");
			pubDivRight.appendChild(action);
			
			// Merge
			action = new Element("div", ns);
			action.addAttribute(new Attribute("class", "pubAction pubButtonMerge"));
			action.addAttribute(new Attribute("id", "merge_" + pub.id));
			action.appendChild("Merge");
			pubDivRight.appendChild(action);
			
			content.appendChild(pubDiv);
		}
		
		Element searchOptions = new Element("span", ns);
		searchOptions.addAttribute(new Attribute("id", "searchOptions"));
		sidebar.appendChild(searchOptions);
	}
}
