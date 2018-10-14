package ch.ethz.livingscience.datacreation;

import static com.mongodb.client.model.Filters.eq;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bson.types.ObjectId;

import java.util.Set;
import java.util.TreeMap;

import com.mongodb.BasicDBObject;
import com.mongodb.Bytes;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

import ch.ethz.livingscience.data.ProfilesDB;
import ch.ethz.livingscience.data.Publication;
import ch.ethz.livingscience.ngrams.NGrams;

/* 
 * Count number of different pubs where an ngram appears rather than total counts and divide by #pubs that year
 */
public class ACMgramsYears
{

	static File dir = new File("C:/Users/almud/livingscience/data");
	static final String[] stopWords = { "a", "aa", "able", "about", "across", "after", "all", "almost", "also", "am", "among", "an", "and", "any", "are", "as", "at", "be", "because", "been", "but", "by", "can", "cannot", "could", "dear", "did", "do", "does", "either", "else", "ever", "every", "for", "from", "get", "got", "had", "has", "have", "he", "her", "hers", "him", "his", "how", "however", "i", "if", "in", "into", "is", "it", "its", "just", "least", "let", "like", "likely", "may", "me", "might", "most", "must", "my", "neither", "no", "nor", "not", "of", "off", "often", "on", "only", "or", "other", "our", "own", "rather", "said", "say", "says", "she", "should", "since", "so", "some", "than", "that", "the", "their", "them", "then", "there", "these", "they", "this", "tis", "to", "too", "twas", "us", "wants", "was", "we", "were", "what", "when", "where", "which", "while", "who", "whom", "why", "will", "with", "would", "yet", "you", "your" };

	public static void main(String[] args) throws Exception
	{
		//obtain counts between 2000 and 2014
		ACMgramsYears c = new ACMgramsYears(2005, 2018);
		c.countNGramsByYear(2005, 2018);
//		
//		 DBObject currentAuthor = db.collProfilesAuto.findOne(new BasicDBObject("_id", new ObjectId(authorID)));
//	        List<String> autPubs = getList(currentAuthor.get("pubs"));
//	        autPubs.add(id);
//	        currentAuthor.put("pubs", autPubs);
//	        db.collProfilesAuto.update(new BasicDBObject("_id", new ObjectId(authorID)), currentAuthor);
		
//		BasicDBObject searchObject = new BasicDBObject();
//		searchObject.put("shortname", "gene");
//		DBCursor resultSubset = db.collAcm.find(searchObject);
//		System.out.println(resultSubset.length());
//		DBCursor cursor = db.collAcm.find();
//		Set<String> stopWordsSet = new HashSet<>(Arrays.asList(stopWords));
//		while(cursor.hasNext()) 
//	    {
//	      DBObject dbo = cursor.next();
//	      String name = dbo.get("name").toString().toLowerCase();
//	      String id = dbo.get("_id").toString();
//	      String[] pname = name.split("[\\p{Punct}\\s]+");
//	      String fname = "";
//	      for(String p:pname) {
//	    	  if(!stopWordsSet.contains(p)) {fname +=p;}
//	      }
//	      dbo.put("shortname", fname);
//	      db.collAcm.update(new BasicDBObject("_id", new ObjectId(id)), dbo);
//	    }
	}
	
	int[] totalCount;
	Map<String, int[]> ngramToData;
	Map<String, String> oldNGram;
	int years;
	
	public ACMgramsYears(int fromYear, int toYear) throws Exception
	{
		years = toYear - fromYear + 1;
		//store the total number of pubs per year
		totalCount = new int[years];
		Arrays.fill(totalCount, 0);
		ngramToData = new TreeMap<>();
		oldNGram = new HashMap<>();
	}
	
	public void countNGramsByYear(int fromYear, int toYear) throws Exception
	{
		NGrams ngrams = NGrams.getInstance();
		ProfilesDB db = new ProfilesDB(27013);
		
		DBCursor cursor = db.collPubs.find();
		Set<String> shortnames = new HashSet<>();
		DBCursor cursor2 = db.collAcm.find();
		while(cursor2.hasNext()) {
			DBObject dbo2 = cursor2.next();
			String sname = dbo2.get("shortname").toString();
			shortnames.add(sname);
			System.out.println(sname);
		}
		
		int count = 0;
		
		while(cursor.hasNext()) 
		{
			DBObject dbo = cursor.next();
			
			Publication pub = db.toPub(dbo);
			
			if(pub.year>=fromYear & pub.year<=toYear)
			{
				int index = pub.year - fromYear;
				totalCount[index]+=1;
				count++;
				// get list of ngrams for this pub and remove duplicates
				List<String> pubNgrams = ngrams.getACMNGrams(pub.title, 1, 3, shortnames);
				pubNgrams.addAll(ngrams.getACMNGrams(pub.summary, 1, 3, shortnames));
				Set<String> hs = new HashSet<>();
				hs.addAll(pubNgrams);
				pubNgrams.clear();
				pubNgrams.addAll(hs);
				
				for (String ngram: pubNgrams)
				{	
					int[] countsNGram = ngramToData.get(ngram);
					
					//check if ngram is already in list
					if (ngramToData.get(ngram) != null)
					{
						//increase count for the nGram
						countsNGram[index] +=1;
						countsNGram[years] +=1;
						ngramToData.put(ngram, countsNGram);
					}
					else
					{
						//add new entry
						countsNGram = new int[years+1];
						Arrays.fill(countsNGram, 0);
						countsNGram[index] = 1;
						countsNGram[years] = 1;
						ngramToData.put(ngram, countsNGram);
					}
				}
				if (count % 2000 == 0) System.out.println(count + " counts");
			}
		}
		
		System.out.println("writing...");
		
		String fileName = "ngramsYears_" + Integer.toString(fromYear) + "_" + Integer.toString(toYear) + "acm.txt";
		BufferedWriter writer = new BufferedWriter(new FileWriter(new File(dir, fileName)));
		//write total line
		writer.write("__total");
		for(int i=0; i<years; i++)
		{
			writer.write(";" + totalCount[i]);
		}
		writer.write("\n");
		for (Entry<String, int[]> entries : ngramToData.entrySet())
		{
			int[] c = entries.getValue();
			if (c[years] < 13) continue;
			String key = entries.getKey();
			writer.write(key);
			for(int i=0; i<years; i++)
			{
				writer.write(";" + c[i]);
			}
			writer.write("\n");
		}
		writer.close();
	}
}
