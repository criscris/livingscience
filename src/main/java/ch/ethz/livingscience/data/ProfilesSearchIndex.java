package ch.ethz.livingscience.data;

import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;

import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

import ch.ethz.livingscience.arxiv.ArxivAuthorNames;
import utils.text.CountableSet;
import utils.text.RandomAccessTextFile;
import utils.text.TextFileUtil;

public class ProfilesSearchIndex
{
	Map<String, List<String>> nameToProfileIDMap = new HashMap<>();
	List<String> profileNameList = new ArrayList<>();
	int noOfAutomaticProfiles = 0;
	int noOfManualProfiles = 0;
	HttpServletRequest req;
	//String query = req.getParameter("q");
	
	RandomAccessTextFile ngramsSearchIndex;
	
	public ProfilesSearchIndex(ProfilesDB db, File searchIndexDir) throws Exception
	{
		long time = System.currentTimeMillis();
		
		CountableSet<String> namesOccurences = new CountableSet<>();
		noOfAutomaticProfiles = addAllProfiles(db.collProfilesAuto, namesOccurences, db);
		noOfManualProfiles = addAllProfiles(db.collProfilesManual, namesOccurences, db);
		
		System.out.println("iterating over all docs took " + (System.currentTimeMillis() - time) + " ms. " + noOfAutomaticProfiles + " auto and " + noOfManualProfiles + " manual profiles indexed.");
	
		time = System.currentTimeMillis();
		//TODO: delete this
//		ngramsSearchIndex = new RandomAccessTextFile(
//				new File(searchIndexDir, "ngrams_profiles.txt"),
//				new File(searchIndexDir, "ngrams_profiles_index.txt"));
//		System.out.println("Loaded n-gram search index in " + (System.currentTimeMillis() - time) + " ms.");
		
	}
	
	int addAllProfiles(DBCollection coll, CountableSet<String> namesOccurences, ProfilesDB db)
	{
		int count = 0;
		DBCursor cursor = coll.find();
		try 
		{
		   while(cursor.hasNext()) 
		   {
		       DBObject dbo = cursor.next();
		       try
		       {   
		    	   String id = dbo.get("_id").toString();
		    	   String name = dbo.get("name").toString();
		    	   
		    	   
		    	   namesOccurences.add(name);
		    	   int nameCount = namesOccurences.counts.get(name);
		    	   String urlName = URLEncoder.encode(name.replace(" ", "_"), "utf-8");
		    	   if (nameCount > 1) urlName += "_" + nameCount;
		    	   db.labelIDtoInternalID.put(urlName, id);
		    	   
		    	   id = urlName;
		    	   //add(name, id);
		    	   /*
		    	   String initialAndFamilyName = ArxivAuthorNames.getFirstInitialAndFamilyName(name);
		    	   add(initialAndFamilyName, id);
		    	   
		    	   String familyName = ArxivAuthorNames.getFamilyName(name);
		    	   add(familyName, id);
		    	   
		    	   String firstName = ArxivAuthorNames.getFirstName(name);
		    	   add(firstName, id);
		    	   */
		    	   List<String> names = ArxivAuthorNames.getAllAssociatedNames(name);
		    	   add(names, id);		    	   
		    	   
		    	   count++;
		       }
		       catch (Exception ex)
		       {
		    	   ex.printStackTrace();
		       }
		   }
		} 
		finally 
		{
		   cursor.close();
		}
		return count;
	}
	
	void add(List<String> names, String profileID)
	{
		if (names == null) return;
		for (String name : names){			
			name = name.toLowerCase();			
			List<String> profileIDs = nameToProfileIDMap.get(name);
			if (profileIDs == null)
			{
				profileIDs = new ArrayList<>();
				nameToProfileIDMap.put(name,  profileIDs);
				profileNameList.add(name);
			}
			profileIDs.add(profileID);			
		}
	}
	
	/**
	 * 
	 * @return(profileID,score)*
	 */
	public Map<String, Float> queryForProfiles(String query)
	{
		String name = query.toLowerCase().trim();
		List<String> profileIDs = new ArrayList<>();
		Map<Integer, String> nameToProfileDistanceMap = new HashMap<>();

		for (String profile : profileNameList)
		{
			if(Math.abs(profile.length()-name.length())< 2) {
			nameToProfileDistanceMap.put(ArxivAuthorNames.distance(profile, name), profile);
			}
//			if (ArxivAuthorNames.distance(profile, name) == 0)
//			{
//				profileIDs.addAll(nameToProfileIDMap.get(profile));
//				//nameToProfileIDMap.get(profile);
//				//break;
//			}			
		}
//		if (profileIDs.isEmpty())
//		{
		for (Entry<Integer, String> entry : nameToProfileDistanceMap.entrySet())		
		{
		    int key = entry.getKey();
		    String value = entry.getValue();
		    if (key == 0)
		    {
		    	profileIDs.addAll(nameToProfileIDMap.get(value));
		    	//profileIDs = nameToProfileIDMap.get(value);				    	
		    }
		    else if (key < 4 && key > 0 && profileIDs.isEmpty())
		    {
		    	profileIDs.addAll(nameToProfileIDMap.get(value));
		    	//profileIDs = nameToProfileIDMap.get(value);				    	
		    }
		}

		if (!profileIDs.isEmpty())
		{
			Map<String, Float> scores = new HashMap<>();
			for (String profileID : profileIDs)
			{
				scores.put(profileID, 0f);				
			}			
			return scores;
		}
		return queryNGramsSearchIndex(query);
	}
	
	Map<String, Float> queryNGramsSearchIndex(String query)
	{
		Map<String, Float> scores = new HashMap<>();
		String line = null;
		try 
		{
			if(ngramsSearchIndex != null) {
				line = ngramsSearchIndex.getLine(query);
			}
			else {
				return scores;
			}
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}

		if (line == null) return scores;
		
		List<String> parts = TextFileUtil.split(line, ";");
		List<String> profileIDs = parts.subList(1, parts.size());
		
		CountableSet<String> profileCounts = new CountableSet<>();
		for (String profileID : profileIDs) profileCounts.add(profileID);
		
		List<Entry<String, Integer>> entries = profileCounts.sortedEntriesDecending();
		
		for (int i=0; i<Math.min(10,  entries.size()); i++)
		{
			String profileID = entries.get(i).getKey();
			scores.put(profileID, (float) entries.get(i).getValue());
		}
		
		return scores;
	}
	
	public int getNoOfAutomaticProfiles()
	{
		return noOfAutomaticProfiles;
	}
	
	public int getNoOfManualProfiles()
	{
		return noOfManualProfiles;
	}
}
