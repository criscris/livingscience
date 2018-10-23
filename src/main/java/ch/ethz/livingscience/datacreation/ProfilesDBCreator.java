package ch.ethz.livingscience.datacreation;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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

public class ProfilesDBCreator implements LineListener
{
  static final String[] stopWords = { "a", "aa", "able", "about", "across", "after", "all", "almost", "also", "am", "among", "an", "and", "any", "are", "as", "at", "be", "because", "been", "but", "by", "can", "cannot", "could", "dear", "did", "do", "does", "either", "else", "ever", "every", "for", "from", "get", "got", "had", "has", "have", "he", "her", "hers", "him", "his", "how", "however", "i", "if", "in", "into", "is", "it", "its", "just", "least", "let", "like", "likely", "may", "me", "might", "most", "must", "my", "neither", "no", "nor", "not", "of", "off", "often", "on", "only", "or", "other", "our", "own", "rather", "said", "say", "says", "she", "should", "since", "so", "some", "than", "that", "the", "their", "them", "then", "there", "these", "they", "this", "tis", "to", "too", "twas", "us", "wants", "was", "we", "were", "what", "when", "where", "which", "while", "who", "whom", "why", "will", "with", "would", "yet", "you", "your" };

  Set<String> stopWordsSet;
  
  public static void main(String[] args) throws Exception
  {
    int port = new Integer(args[0]);
    ProfilesDBCreator c = new ProfilesDBCreator(port);
    c.exec();
  }
  
  ProfilesDB db;
  UniList uniList;
  
  Map<String, List<Profile>> arxivIDtoProfile = new HashMap<>();
  Map<String, String> nameToID;
  List<Profile> profiles = new ArrayList<>();
  Map<String, String> shortTitles = new HashMap<>();//short title, id
  
  public ProfilesDBCreator(int port) throws Exception
  {
	stopWordsSet = new HashSet<>();
	for (String stopword : stopWords) stopWordsSet.add(stopword);
    db = new ProfilesDB(port);
    nameToID = createNameToID();
    uniList = new UniList();
    shortTitles = getShortTitles();
  }
  
  //map with existing authors
  Map<String, String> createNameToID() throws Exception
  {
    Map<String, String> nameToID = new HashMap<>();
    DBCursor cursor = db.collProfilesAuto.find();
    System.out.println("Current db has : " + cursor.size() + " authors and " 
                        + db.collPubs.find().size() + " pubs.\n");
    while(cursor.hasNext()) 
    {
      DBObject dbo = cursor.next();
      nameToID.put(dbo.get("name").toString(), dbo.get("_id").toString());
    }
    return nameToID;
  }
  
  public void exec() throws Exception
  {
    long time = System.currentTimeMillis();
    TextFileUtil.loadList(new File("data/arxiv/arxivmeta_summaries.txt"), this);
    System.out.println("Pubs loaded in " + (System.currentTimeMillis() - time) + " ms.");
    
    // now, add all profiles
    for (int i=0; i<profiles.size(); i++)
    {
      Profile profile = profiles.get(i);
      
      boolean useClustername = true;
      int i0 = profile.name.lastIndexOf(" ");
      if (i0 != -1)
      {
        String lastName = profile.name.substring(i0 + 1).toLowerCase();
        
        List<Publication> pubs = new ArrayList<>();
        for (String pubID : profile.pubIDs)
        {
          pubs.add(db.getPub(pubID));
        }
        
        String fullName = guessFullName(lastName, pubs);
        if (fullName != null)
        {
          profile.name = fullName;
          useClustername = false;
        }
      }
      
      if (useClustername)
      {
        // make name uppercase
        profile.name = profile.name.substring(0, 1).toUpperCase() + profile.name.substring(1);
        int i1 = profile.name.indexOf(" ");
        if (i1 > 0 && i1 < profile.name.length() - 2)
        {
          profile.name = profile.name.substring(0, i1 + 1) + profile.name.substring(i1 + 1, i1 + 2).toUpperCase() + profile.name.substring(i1 + 2);
        }
      }
      
      
      BasicDBObject doc = new BasicDBObject("name", profile.name).append("pubs", profile.pubIDs);
      db.collProfilesAuto.insert(doc);
      
      if ((i+1) % 2000 == 0) System.out.println("profile" + (i+1));
    }
    
    System.out.println((System.currentTimeMillis() - time) + " ms.");
    System.out.println("Current db has : " + db.collProfilesAuto.find().size() 
                    + "authors and " + db.collPubs.find().size() + " pubs.\n");
  }
  
  int pubCount = 0;
  int autCount = 0;
  public void newLine(int index, String line) 
  {
    ArxivCitation citation = new ArxivCitation(line);
    //get short title and check if it is already in the database
    String longtitle = citation.title;
    String[] pname = longtitle.split("[\\p{Punct}\\s]+");
	String fname = "";
	for(String p:pname) {
	   if(!stopWordsSet.contains(p)) {fname +=p;}
	}
	//true if title already in db
	Boolean pubindb = shortTitles.containsKey(fname);
	if(!pubindb) {
	    String url = "http://arxiv.org/abs/" + ArxivXMLAnalyzer.addSlashToIDifOldID(citation.arxivID);
	    
	    BasicDBObject doc = new BasicDBObject();
	    if (citation.authors != null && citation.authors.size() > 0) doc.append("authors", citation.authors);
	    if (citation.journal != null && citation.journal.length() > 0) doc.append("journal", citation.journal);
	    if (citation.title != null && citation.title.length() > 0) doc.append("title", citation.title);
	    if (citation.year != 0) doc.append("year", citation.year);
	    if (citation.summary != null && citation.summary.length() > 0) doc.append("summary", citation.summary);
	    
	    doc.append("url", url);
	    
	    if (citation.doi != null && citation.doi.length() > 0) doc.append("doi", citation.doi);
	    
	    db.collPubs.insert(doc);
	    String id = doc.get("_id").toString();
	
	    // arxiv id without slash
	    List<Profile> profiles = arxivIDtoProfile.get(citation.arxivID);
	    if (profiles != null)
	    {
	      for (Profile profile : profiles)
	      {
	        profile.pubIDs.add(id);
	      }
	    }
	    
	    //for each author check if already in db, if so add pub, if not add author to db
	    for(String author : citation.authors)
	    {
	      String authorID = nameToID.get(author);
	      //author is already in db, add publication
	      if (authorID != null)
	      {
	        DBObject currentAuthor = db.collProfilesAuto.findOne(new BasicDBObject("_id", new ObjectId(authorID)));
	        List<String> autPubs = getList(currentAuthor.get("pubs"));
	        autPubs.add(id);
	        currentAuthor.put("pubs", autPubs);
	        db.collProfilesAuto.update(new BasicDBObject("_id", new ObjectId(authorID)), currentAuthor);
	      }
	      //add author to database and hashmap
	      else
	      {
	        List<String> pubsID = new ArrayList<>();
	        pubsID.add(id);
	        BasicDBObject autDoc = new BasicDBObject("name", author).append("pubs", pubsID);
	          db.collProfilesAuto.insert(autDoc);
	          authorID = autDoc.get("_id").toString();
	          nameToID.put(author, authorID);
	          autCount++;
	      }
	    }
	}
	//otherwise just update the year
	else {
		DBObject currentPub = db.collPubs.findOne(new BasicDBObject("_id", new ObjectId(shortTitles.get(fname))));
		if(Integer.parseInt(currentPub.get("year").toString()) < citation.year) {
			BasicDBObject newDocument = new BasicDBObject();
			newDocument.append("$set", new BasicDBObject().append("year", citation.year));
					
			BasicDBObject searchQuery = new BasicDBObject().append("_id", new ObjectId(shortTitles.get(fname)));

			db.collPubs.update(searchQuery, newDocument);
		}
	}
    pubCount++;
    if (pubCount % 2000 == 0) System.out.println("pub" + pubCount);
    if (autCount % 2000 == 0) System.out.println("new authors: " + autCount);
  }
  
  //list of titles in short version to check for duplicates
  public Map<String, String> getShortTitles(){
	  Map<String, String> titles = new HashMap<>();
	  DBCursor cursor = db.collPubs.find();
	  while(cursor.hasNext()) {
		  DBObject dbo = cursor.next();
		  String longtitle = dbo.get("title").toString();
		  String[] pname = longtitle.split("[\\p{Punct}\\s]+");
		  String fname = "";
		  for(String p:pname) {
		       if(!stopWordsSet.contains(p)) {fname +=p;}
		  }
		  titles.put(fname, dbo.get("_id").toString());
	  }
	  return titles;
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
  
  static String guessFullName(String lastname, List<Publication> pubs)
  {
    CountableSet<String> names = new CountableSet<>();
    
    for (Publication pub : pubs)
    {
      for (String author : pub.authors)
      {
        if (author.toLowerCase().endsWith(lastname))
        {
          if (author.length() < 3) continue;
          if (author.charAt(1) == ' ' || author.charAt(1) == '.' || author.charAt(2) == ' ') continue;
          
          names.add(author);
        }
      }
    }
    
    List<Entry<String, Integer>> entries = names.sortedEntriesDecending();
    if (entries.size() == 0) return null;
    
    return entries.get(0).getKey();
  }
  
}
