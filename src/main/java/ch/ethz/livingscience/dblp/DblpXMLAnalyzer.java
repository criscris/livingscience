package ch.ethz.livingscience.dblp;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import org.bson.types.ObjectId;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

import ch.ethz.livingscience.data.ProfilesDB;
import utils.text.TextFileUtil;

public class DblpXMLAnalyzer {
	public static void main(String[] args) throws Exception
	{
		//int port = new Integer(args[0]);
		int port = 27013;
		DblpXMLAnalyzer c = new DblpXMLAnalyzer(port);
		c.execAll();
//		c.test();
	}
	
	
	ProfilesDB db;
	int existingAut, newAut;
	int existingPub, newPub;
	
	public DblpXMLAnalyzer(int port) throws Exception
	{
		db = new ProfilesDB(port);
		existingAut = 0;
		newAut = 0;
		existingPub = 0;
		newPub = 0;
	}
	public void test() throws Exception
	{
		String tes = "__qName:author__qValue:Isra__qValue:ë__qValue:l-C__qValue:é__qValue:sar Lerman";
		List<String> tagParts = TextFileUtil.split(tes, "__qValue:");
		String autName = "";
		for(int j=1; j<tagParts.size();j++) {
			autName += tagParts.get(j);
		}
		System.out.println(autName);
//		System.out.println("Number of publications before update: " + db.collPubs.count());
//		System.out.println("Number of authors before update: " + db.collProfilesAuto.count());
		
//		DBObject currentAuthor = checkAuthor("Frank Manola");
//		System.out.println(currentAuthor.get("pubs"));
//		List<String> autPubs = getList(currentAuthor.get("pubs"));
//		for(String pub:autPubs) {
//			System.out.println(pub);
//		}
//		System.out.println(currentAuthor);
//		DBCursor myDoc = db.collProfilesAuto.find().sort(new BasicDBObject("_id",-1)).limit(9);
//		DBCursor myDoc1 = db.collPubs.find().sort(new BasicDBObject("_id",-1)).limit(12);
//		for(int i=1;i<10;i++) {
//		db.collProfilesAuto.remove(myDoc.next());
//			//System.out.println(myDoc.next());
//		}
//		for(int i=1;i<13;i++) {
//			db.collPubs.remove(myDoc1.next());
//				//System.out.println(myDoc.next());
//		}
//		System.out.println("Number of publications before update: " + db.collPubs.count());
//		System.out.println("Number of publications before update: " + db.collProfilesAuto.count());
	}
	public void execAll() throws Exception
	{
		try
		{
			System.out.println("Number of publications before update: " + db.collPubs.count());
			System.out.println("Number of authors before update: " + db.collProfilesAuto.count());
			
		   	BufferedReader reader = new BufferedReader(new FileReader("C:/Users/almud/Documents/LivingScience/files/dblp/dump/sorted/test.txt"));
		    String line = null;
		    while ((line = reader.readLine()) != null)
		    {
		       	DblpCitation pub = newLine(line);
		       	
		       	if(pub != null) {
		       	  //check if the publication is already in the database: needs improvement, now just checking the title as it is
		       	  boolean isInDb = checkPub(pub.title);
		       	  if(!isInDb) {
		       		  newPub++;
		       		  addPub(pub);
		          }
		       	  else {
		       	      existingPub++;
		       	  }
		       	}
		    }
		    reader.close();
		    
		    System.out.println("existing authors: " + existingAut + "; new authors: " + newAut);
			System.out.println("existing pubs: " + existingPub + "; new pubs: " + newPub);
			
			System.out.println("Number of publications after update: " + db.collPubs.count());
			System.out.println("Number of authors after update: " + db.collProfilesAuto.count());
		}
		catch (Exception ex)
		{
		   	ex.printStackTrace();
		}
	}
		
	DblpCitation newLine(String line) throws Exception
	{
		//Each part corresponds to an xml tag and its content, get(0) is the empty string
		List<String> fullTag = TextFileUtil.split(line, "__qName:");

		DblpCitation pub = new DblpCitation();
		
		boolean hasAut = false;
	
		//start the loop at 1 because 0 is empty
		for (int i=1; i<fullTag.size(); i++)
		{
			List<String> tagParts = TextFileUtil.split(fullTag.get(i), "__qValue:");
			//to get the tag name we need to separate it from the attributes
			List<String> tagNameParts = TextFileUtil.split(tagParts.get(0), " ");
			String tagName = tagNameParts.get(0);
			if(tagParts.size() >1) {
			 switch (tagName)
			 {
			 case "ee":
			 	//sample ee: https://doi.org/10.1287/opre.46.3.396
				List<String> fullDoi = TextFileUtil.split(tagParts.get(1),"doi.org/");
				if(fullDoi.size()>1) {
					String doi = fullDoi.get(1);
					pub.doi = doi;
				}
			 break;
			 //the update date appears as an attribute for the article tag
			 case "article":
				String mdate = tagNameParts.get(1);
				String date = TextFileUtil.split(mdate, "\"").get(1);
				int year = Integer.parseInt(TextFileUtil.split(date, "-").get(0));
				
				if(year > 1900) {pub.year = year;}
			 break;
			 case "year":
				//only use this for the year if there wasn't updated date
				if(pub.year < 0) {
				   pub.year = Integer.parseInt(tagParts.get(1));
				}
			 break;	
			 case "journal":
				pub.journal = tagParts.get(1).replaceAll(";", ",").replaceAll("\n", "");
			 break;
			 case "pages":
				pub.pages = tagParts.get(1);
			 break;
			 case "volume":
				pub.volume = tagParts.get(1);
			 break;
			 case "number":
				pub.issue = tagParts.get(1);
			 break;
			 case "title":
				pub.title = tagParts.get(1).replaceAll(";", ",").replaceAll("\n", "");
			 break;
			 case "author":
				hasAut = true;
				String autName = "";
				for(int j=1; j<tagParts.size();j++) {
					autName += tagParts.get(j);
				}
				pub.authors.add(autName.replaceAll(";", ",").replaceAll("\n", ""));
			 break;
//			 case "url":
//				pub.url = tagParts.get(1);
//			 break;	
			 }
			}
		}
		
		if(!hasAut) {pub = null;}
		return pub;
	}
	
	//checks if the title of a publication is already in the database
	boolean checkPub (String title) throws Exception
	{
		boolean isInDb = false;
		BasicDBObject searchObject = new BasicDBObject();
		searchObject.put("title", title);
		BasicDBObject fieldObject = new BasicDBObject();
		fieldObject.put("_id", 1);
		DBCursor resultSubset = db.collPubs.find(searchObject, fieldObject);
		if(resultSubset.size() > 0) {isInDb = true;}
		return isInDb;
	}
	
	//checks if an author is already in the database and returns the author
	DBObject checkAuthor (String authorName) throws Exception
	{
		DBObject author = null;
		BasicDBObject searchObject = new BasicDBObject();
		searchObject.put("name", authorName);
		BasicDBObject fieldObject = new BasicDBObject();
		fieldObject.put("_id", 1);
		fieldObject.put("pubs", 1);
		DBCursor resultSubset = db.collProfilesAuto.find(searchObject, fieldObject);
		if(resultSubset.size() > 0) {author = resultSubset.next();}
		return author;
	}
	
	public void addPub(DblpCitation citation) throws Exception
	{
		BasicDBObject doc = new BasicDBObject();
	    if (citation.authors != null && citation.authors.size() > 0) doc.append("authors", citation.authors);
	    if (citation.journal != null && citation.journal.length() > 0) doc.append("journal", citation.journal);
	    if (citation.title != null && citation.title.length() > 0) doc.append("title", citation.title);
	    if (citation.year != 0) doc.append("year", citation.year);
//	    if (citation.url != null && citation.url.length() > 0) doc.append("url", citation.url);
	    
	    if (citation.doi != null && citation.doi.length() > 0) doc.append("doi", citation.doi);
	    
	    db.collPubs.insert(doc);
	    String id = doc.get("_id").toString();
	    
	    //for each author check if already in db, if so add pub, if not add author to db
	    for(String author : citation.authors)
	    {
	      DBObject currentAuthor = checkAuthor(author);
	      //author is already in db, add publication
	      if (currentAuthor != null)
	      {
	    	existingAut++;
	    	System.out.println("exists: " + author);
	    	String authorID = currentAuthor.get("_id").toString();
	    	DBObject aut = db.collProfilesAuto.findOne(new BasicDBObject("_id", new ObjectId(authorID)));
	        List<String> autPubs = getList(aut.get("pubs"));
	        autPubs.add(id);
	        aut.put("pubs", autPubs);
	        db.collProfilesAuto.update(new BasicDBObject("_id", new ObjectId(authorID)), aut);
	      }
	      //add author to database
	      else
	      {
	    	newAut++;
	        List<String> pubsID = new ArrayList<>();
	        pubsID.add(id);
	        System.out.println("new: " + author);
	        BasicDBObject autDoc = new BasicDBObject("name", author).append("pubs", pubsID);
	        db.collProfilesAuto.insert(autDoc);
	      }
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

}
