package ch.ethz.livingscience.pages;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import nu.xom.Document;

import org.json.JSONArray;
import org.json.JSONObject;

import ch.ethz.livingscience.data.ProfilesDB;
import ch.ethz.livingscience.data.Publication;
import ch.ethz.livingscience.data.wikitopics.SprikiData;
import ch.ethz.livingscience.data.wikitopics.SprikiKeyword;
import ch.ethz.livingscience.data.wikitopics.SprikiRelations;
import ch.ethz.livingscience.data.wikitopics.SprikiResult;
import ch.ethz.livingscience.data.wikitopics.SprikiResults;

public class TopicsPage extends ProfilePubListPage
{
	SprikiRelations relations;
	
	public TopicsPage(Document doc, ProfilesDB db, String profileID, SprikiRelations relations) throws IOException
	{
		super(doc, db, profileID);
		this.relations = relations;
	}
	
	public void exec() throws IOException
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
		
		
		addJSONData(result, "graphData");
	}
}