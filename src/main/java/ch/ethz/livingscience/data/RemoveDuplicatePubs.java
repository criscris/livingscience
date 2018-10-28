package ch.ethz.livingscience.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bson.types.ObjectId;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

public class RemoveDuplicatePubs {
	static final String[] stopWords = { "a", "aa", "able", "about", "across", "after", "all", "almost", "also", "am", "among", "an", "and", "any", "are", "as", "at", "be", "because", "been", "but", "by", "can", "cannot", "could", "dear", "did", "do", "does", "either", "else", "ever", "every", "for", "from", "get", "got", "had", "has", "have", "he", "her", "hers", "him", "his", "how", "however", "i", "if", "in", "into", "is", "it", "its", "just", "least", "let", "like", "likely", "may", "me", "might", "most", "must", "my", "neither", "no", "nor", "not", "of", "off", "often", "on", "only", "or", "other", "our", "own", "rather", "said", "say", "says", "she", "should", "since", "so", "some", "than", "that", "the", "their", "them", "then", "there", "these", "they", "this", "tis", "to", "too", "twas", "us", "wants", "was", "we", "were", "what", "when", "where", "which", "while", "who", "whom", "why", "will", "with", "would", "yet", "you", "your" };
	static ProfilesDB db;

	public static void main(String[] args) throws Exception
	{
		exec3(new Integer(args[0]));
	}
	public static void exec2(int port) throws Exception {
		db = new ProfilesDB(port);
		DBCursor cursor = db.collProfilesAuto.find();
		int count = 0;
		while(cursor.hasNext()) {
			count++;
			if(count>2387870) {
				DBObject aut = cursor.next();
				System.out.println(aut.get("name").toString());
				HashMap<String,String> autPubs = fixpubs(aut.get("pubs"));
				aut.put("pubs", autPubs.values());
				db.collProfilesAuto.update(new BasicDBObject("_id", new ObjectId(aut.get("_id").toString())), aut);
			}
			else {
				cursor.next();
			}
		}
	}
	//remove authors with no pubs
	public static void exec3(int port) throws Exception {
		db = new ProfilesDB(port);
		DBCursor cursor = db.collProfilesAuto.find();
		while(cursor.hasNext()) {
				DBObject aut = cursor.next();
				List<String> autPubs = getList(aut.get("pubs"));
				if(autPubs.size()==0) {
					System.out.println(aut.get("name").toString());
					db.collProfilesAuto.remove(new BasicDBObject("_id", new ObjectId(aut.get("_id").toString())));
				}
		}
	}
	public static void exec(int port) throws Exception {
		Set<String> stopWordsSet = new HashSet<>(Arrays.asList(stopWords));
		db = new ProfilesDB(port);
		DBCursor cursor = db.collProfilesAuto.find();
		int count = 0;
		while(cursor.hasNext()) {
			count++;
			if(count>2387850) {
			DBObject aut = cursor.next();
			System.out.println(aut.get("name").toString());
			List<String> autPubs = getList(aut.get("pubs"));
			//here we will only add good pubs
			Map<String,String> newautPubs = new HashMap<>();;
			for(String pubId:autPubs) {
				//check if pub exists
				DBObject pub = db.collPubs.findOne(new BasicDBObject("_id", new ObjectId(pubId)));
				if(pub!=null) {
					  //if the pub doesnt have title, remove
					  if(pub.get("title")==null) {
						  db.collPubs.remove(new BasicDBObject("_id", new ObjectId(pubId)));
					  }
					  else {
						  String title = pub.get("title").toString().toLowerCase();
					      String[] ptitle =title.split("[\\p{Punct}\\s]+");
					      String ftitle = "";
					      for(String p:ptitle) {
					    	  if(!stopWordsSet.contains(p)) {ftitle +=p;}
					      }
					      //check if already in list
					      if(newautPubs.containsKey(ftitle)) {
					    	  //keep the most recent
					    	  String oldId = newautPubs.get(ftitle);
					    	  int thisYear = new Integer(pub.get("year").toString());
					    	  int exYear = 0;
					    	  if(db.collPubs.findOne(new BasicDBObject("_id", new ObjectId(oldId))).get("year")!=null) {
					    		  exYear = new Integer(db.collPubs.findOne(
						    			  new BasicDBObject("_id", new ObjectId(oldId))).get("year").toString());
					    	  }
					    	  if(thisYear>exYear) {
					    		  newautPubs.replace(ftitle, oldId, pubId);
					    		  //remove the other pub from the collection
					    		  db.collPubs.remove(new BasicDBObject("_id", new ObjectId(oldId)));
					    	  }
					    	  else{
					    		  //remove this pub from the collection
					    		  db.collPubs.remove(new BasicDBObject("_id", new ObjectId(pubId)));
					    	  }
					    	  System.out.println(ftitle);
					      }
					      else {
					    	  newautPubs.put(ftitle, pubId);
					      } 
					  }
				}
			
			}
			aut.put("pubs", newautPubs.values());
			db.collProfilesAuto.update(new BasicDBObject("_id", new ObjectId(aut.get("_id").toString())), aut);
			}
			else {
				cursor.next();
			}
			if (count % 100 == 0) System.out.println(count);
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
	
	public static HashMap<String, String> fixpubs(Object result)
	{
		HashMap<String, String> fixed = (HashMap) result;
		return fixed;
	}

}
