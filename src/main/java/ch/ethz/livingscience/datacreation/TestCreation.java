package ch.ethz.livingscience.datacreation;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bson.types.ObjectId;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

import ch.ethz.livingscience.arxiv.ArxivCitation;
import ch.ethz.livingscience.arxiv.api.ArxivXMLAnalyzer;
import ch.ethz.livingscience.data.Profile;
import ch.ethz.livingscience.data.ProfilesDB;
import ch.ethz.livingscience.data.Publication;
import ch.ethz.livingscience.data.affiliation.UniList;
import utils.text.CountableSet;
import utils.text.LineListener;
import utils.text.TextFileUtil;

public class TestCreation implements LineListener
{
	public static void main(String[] args) throws Exception
	{
		TestCreation c = new TestCreation();
		c.exec();
	}
	
	ProfilesDB db;
	Map<String, String> nameToID;
	
	public TestCreation() throws Exception
	{
//		db = new ProfilesDB(27013);
//		nameToID = createNameToID();
	}
	
	//map with existing authors
	Map<String, String> createNameToID() throws Exception
	{
		Map<String, String> nameToID = new HashMap<>();
		DBCursor cursor = db.collProfilesAuto.find();
		int count = 0;
		while(cursor.hasNext()) 
		{
			DBObject dbo = cursor.next();
			count++;
			nameToID.put(dbo.get("name").toString(), dbo.get("_id").toString());
		}
		return nameToID;
	}
	
	public void exec() throws Exception
	{
		readFirst(Paths.get("C:/Users/almud/livingscience/data/arxivngrams/ngrams_profiles_index.txt"),10);
	}
	
	int autCount=0;
	
	public void newLine(int index, String line) 
	{
		ArxivCitation citation = new ArxivCitation(line);
		//for each author check if already in db, if so add pub, if not add author to db
		for(String author : citation.authors)
		{
			String authorID = nameToID.get(author);
			//author is already in db, add publication
			if (authorID != null)
			{
				System.out.println("yes: " + author);
				if(autCount==0)
				{
					nameToID.put("Ivana Jovovic", "test");
				}
//				DBObject currentAuthor = db.collProfilesAuto.findOne(new BasicDBObject("_id", new ObjectId(authorID)));
//				List<String> autPubs = getList(currentAuthor.get("pubs"));
//				autPubs.add(id);
//				currentAuthor.put("pubIDs", autPubs);
			}
			//add author to database and hashmap
			else
			{
				autCount++;
				System.out.println("author: " + author);
//				List<String> pubsID = new ArrayList<>();
//				pubsID.add(id);
//				BasicDBObject autDoc = new BasicDBObject("name", author).append("pubs", pubsID);
//			    db.collProfilesAuto.insert(autDoc);
//			    authorID = autDoc.get("_id").toString();
//			    nameToID.put(author, authorID);
//			    autCount++;
//			    if(autCount==1) System.out.println("new author: " + author);
			}
			if (autCount == 20) System.exit(0);
		}
	}
	public static List<String> getList(Object result)
	{
		List<String> list = new ArrayList<>();
		if (result == null) return list;
		
		BasicDBList e = (BasicDBList) result;
		for (Object o : e)
		{
			list.add(o.toString());
		}
		
		return list;
	}	
	public void readFirst(final Path path, final int numLines) throws IOException {
	    try (final BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
//	        final List<String> lines = new ArrayList<>(numLines);
	        int lineNum = 0;
	        String line;
	        while ((line = reader.readLine()) != null && lineNum < numLines) {
	            System.out.println(line);
	            lineNum++;
	        }
	    }
	}
}