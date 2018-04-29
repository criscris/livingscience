package ch.ethz.livingscience.pages;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Nodes;
import ch.ethz.livingscience.Page;
import ch.ethz.livingscience.data.Profile;
import ch.ethz.livingscience.data.ProfilesDB;
import ch.ethz.livingscience.data.ProfilesSearchIndex;
import ch.ethz.livingscience.data.Publication;
import ch.ethz.livingscience.data.wikitopics.SprikiData;
import ch.ethz.livingscience.data.wikitopics.SprikiKeyword;
import ch.ethz.livingscience.data.wikitopics.SprikiRelations;
import ch.ethz.livingscience.data.wikitopics.SprikiResult;
import ch.ethz.livingscience.data.wikitopics.SprikiResults;

public class WelcomePage extends Page
{
	ProfilesDB db;
	String profileID;
	SprikiRelations relations;
	ProfilesSearchIndex searchIndex;
	
	public WelcomePage(Document doc, ProfilesSearchIndex searchIndex, ProfilesDB db, SprikiRelations relations) throws IOException
	{
		super(doc);
		this.db = db;
		//Dirk Helbing's id
		DBObject author = null;
		BasicDBObject searchObject = new BasicDBObject();
		searchObject.put("name", "Dirk Helbing");
		BasicDBObject fieldObject = new BasicDBObject();
		fieldObject.put("_id", 1);
		DBCursor resultSubset = db.collProfilesAuto.find(searchObject, fieldObject);
		this.profileID = resultSubset.next().get("_id").toString();
		this.relations = relations;
		this.searchIndex = searchIndex;
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
	
	public void createAnimation() throws IOException
	{
		loadPubs();
		
		SprikiData data = new SprikiData();
		data.results1 = new SprikiResults();
		data.results1.query = "";
		data.results1.totalResults = 0;
		data.results1.publications = new ArrayList<SprikiResult>();
	    

		for (int i=0; i<Math.min(100, pubs.size()); i++)
		{
			Publication doc = pubs.get(i);
			SprikiResult sr = new SprikiResult();
			sr.authors = doc.authors;
			sr.journal = doc.journal;
			sr.link = doc.url;
			sr.summary = doc.summary;
			sr.title = doc.title;
			sr.year = doc.year;
			data.results1.publications.add(sr);
		}
		
		data.results2 = new SprikiResults();
		data.results2.query = "";
		data.results2.publications = new ArrayList<SprikiResult>();
		
		JSONObject result = new JSONObject();
		
		try 
		{
		
			relations.exec(data);
		
			JSONArray nodes = new JSONArray();
			
			for (int i=0; i<data.keywords.size(); i++)
			{
				SprikiKeyword keyword = data.keywords.get(i);
				JSONObject o = new JSONObject();
				o.put("x", keyword.x);
				o.put("y", keyword.y);
				o.put("name", keyword.label);
				o.put("group", 1);
				keyword.pcaIndex = i;
				nodes.put(o);
			}
			
			
			JSONArray links = new JSONArray();
			
			int index = data.keywords.size();
			for (int i=0; i<data.results1.publications.size(); i++)
			{
				SprikiResult doc = data.results1.publications.get(i);
				if (doc.keywords.size() == 0) continue;
				
				JSONObject o = new JSONObject();
				o.put("name", doc.title + " (" + doc.year + ")");
				o.put("x", doc.x);
				o.put("y", doc.y);
				o.put("group", 2);
				nodes.put(o);
				
				Set<Integer> words = new HashSet<>();
				for (SprikiKeyword keyword : doc.keywords)
				{
					words.add(keyword.pcaIndex);
				}
				for (Integer word : words)
				{
					JSONObject l = new JSONObject();
					l.put("source", index);
					l.put("target", word);
					links.put(l);
				}
				
				index++;
			}
			
			result.put("nodes", nodes);
			result.put("links", links);
		} 
		catch (Exception e) 
		{
			throw new IOException(e.getCause());
		}
		
		addJSONData(result, "welGraph");
	}
	
	public void exec() throws IOException
	{	
		Nodes nodes = doc.query("//*/html:span[@id='noOfProfilesInDatabase'] ", ctx);
		if (nodes.size() != 0)
		{
			Element noOfProfiles = (Element) nodes.get(0);
			noOfProfiles.removeChildren();
			noOfProfiles.appendChild(" " + (searchIndex.getNoOfAutomaticProfiles() + searchIndex.getNoOfManualProfiles()) + " ");	
		}
		try {
			createAnimation();
		}
		catch (Exception e) 
		{
			throw new IOException(e.getCause());
		}		
	}
}
