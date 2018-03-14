package ch.ethz.livingscience.datacreation;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import ch.ethz.livingscience.data.ProfilesDB;
import ch.ethz.livingscience.data.Publication;
import ch.ethz.livingscience.ngrams.NGrams;
import utils.text.CountableSet;
import utils.text.RandomAccessTextFile;
import utils.text.RandomAccessTextFileLineListener;
import utils.text.TextFileUtil;

import com.mongodb.BasicDBObject;
import com.mongodb.Bytes;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

public class NGramsCreator 
{
	static File dir = new File("data/arxivngrams/");
	static int chunkSize = 10000;

	public static void main(String[] args) throws Exception
	{	
		int port = new Integer(args[0]);
		ProfilesDB db = new ProfilesDB(port);
		
		int numberPubs = (int) db.collPubs.count();
		int iterations = (int) Math.ceil(numberPubs/chunkSize);
		//to avoid memory issues, iterate through the publications in chunks of 
		for(int i=0; i<iterations; i++)
		{
			countNGrams(db, i);
			mergeFiles();
		}
		//the last ngrams_old file created will be the final. So rename it
		new File(dir, "ngrams_old.txt").renameTo(new File(dir, "ngrams.txt"));
		
		ngramToProfiles(db);
		
		createIndex();
	}
	
	public static void countNGrams(ProfilesDB db, int lowerBound) throws Exception
	{
		Map<String, Integer> ngramCounts = new TreeMap<>();
		NGrams ngrams = NGrams.getInstance();
		
		lowerBound = lowerBound*chunkSize;
		int count = 0;
		int upperBound = lowerBound + chunkSize;
		DBCursor cursor = db.collPubs.find();
		while(cursor.hasNext() && count<upperBound) 
		{
			DBObject dbo = cursor.next();
			count++;
			
			//work around for the moment, just create a file with the 
			//new ngrams to then merge them
			if ( count >= lowerBound)
			{
				Publication pub = db.toPub(dbo);
				
				for (String ngram: ngrams.getNGrams(pub.title, 1, 3))
				{
					if(ngramCounts.get(ngram) != null)
					{
						ngramCounts.put(ngram, ngramCounts.get(ngram) + 1);
					}
					else
					{
						ngramCounts.put(ngram, 1);
					}
				}
				for (String ngram : ngrams.getNGrams(pub.summary, 1, 3))
				{
					if(ngramCounts.get(ngram) != null)
					{
						ngramCounts.put(ngram, ngramCounts.get(ngram) + 1);
					}
					else
					{
						ngramCounts.put(ngram, 1);
					}
				}
			
			}
			
		}
	
		int goodNgrams = 0;
		int skippedBig = 0;
		
		System.out.println("writing...");
		
		BufferedWriter writer = new BufferedWriter(new FileWriter(new File(dir, "ngrams_new.txt")));
	
		for (Entry<String, Integer> entries : ngramCounts.entrySet())
		{
			int c = entries.getValue();
			if (c > 10000) 
			{
				skippedBig++;
				continue;
			}
			if (c < 5)
			{
				continue;
			}
			goodNgrams++;
			writer.write(entries.getKey() + "\n");
		}
		writer.close();
		System.out.println(goodNgrams + " good n-grams. skippedBig=" + skippedBig);
		
//		CountableSet<String> ngramCounts = new CountableSet<>();
//		NGrams ngrams = NGrams.getInstance();
//		ProfilesDB db = new ProfilesDB(29013);
//		
//		int count = 0;
//		DBCursor cursor = db.collPubs.find();
//		while(cursor.hasNext()) 
//		{
//			DBObject dbo = cursor.next();
//			count++;
//			
//			Publication pub = db.toPub(dbo);
//			
//		
//			for (String ngram: ngrams.getNGrams(pub.title, 1, 3))
//			{
//				ngramCounts.add(ngram);
//			}
//			for (String ngram : ngrams.getNGrams(pub.summary, 1, 3))
//			{
//				ngramCounts.add(ngram);
//			}
//			
//			if (count % 20000 == 0) System.out.println(count + " " + ngramCounts.counts.size());
//		}
//		
//		List<String> goodNgrams = new ArrayList<>();
//		
//		int skippedBig = 0;
//		for (Entry<String, Integer> entries : ngramCounts.counts.entrySet())
//		{
//			int c = entries.getValue();
//			if (c > 10000) 
//			{
//				skippedBig++;
//				continue;
//			}
//			if (c < 5) continue;
//			goodNgrams.add(entries.getKey());
//		}
//		System.out.println(goodNgrams.size() + " good n-grams. skippedBig=" + skippedBig);
//		Collections.sort(goodNgrams);
//		System.out.println("sorted.");
//		TextFileUtil.writeList(goodNgrams, new File(dir, "ngrams.txt"));

	}
	
//	public static void ngramToProfiles(ProfilesDB db, int lowerBound) throws Exception
	public static void ngramToProfiles(ProfilesDB db) throws Exception
	{
//		lowerBound = lowerBound*chunkAut;
//		int upperBound = lowerBound + chunkAut;
		
		NGrams ngrams = NGrams.getInstance();
		
		Map<String, List<String>> ngramToProfiles = new HashMap<>();
		List<String> words = TextFileUtil.loadList(new File(dir, "ngrams.txt"));
		for (String ngram : words)
		{
			ngramToProfiles.put(ngram, new ArrayList<String>());
		}
		
		int count = 0;
		int countProfileEntries = 0;
		DBCursor cursor = db.collProfilesAuto.find().addOption(Bytes.QUERYOPTION_NOTIMEOUT);
		System.out.println("parsing...");
		
//		while(cursor.hasNext() && count<upperBound) 
		while(cursor.hasNext())
		{
			DBObject dbo = cursor.next();
			count++;
			
//			if(count >= lowerBound) {
			String profileID = dbo.get("_id").toString();
			
//			BasicDBObject searchObject = new BasicDBObject();
//			searchObject.put("authors", dbo.get("name").toString());
//			DBCursor resultSubset = db.collPubs.find(searchObject);
			
			for (String pubID : ProfilesDB.getList(dbo.get("pubs")))
//			while(resultSubset.hasNext())
			{
				Publication pub = db.getPub(pubID);
//				Publication pub = db.getPub(resultSubset.next().get("_id").toString());
				
				if(pub != null)
				{
				  List<String> grams = ngrams.getNGrams(pub.title, 1, 3);
				  grams.addAll(ngrams.getNGrams(pub.summary, 1, 3));
				
				  for (String ngram : grams)
				  {
				      List<String> profileIDs = ngramToProfiles.get(ngram);
					  if (profileIDs != null) 
					  {
						profileIDs.add(profileID);
						countProfileEntries++;
					  }
				  }
				}
				else
				{
					BasicDBObject searchObject = new BasicDBObject();
					searchObject.put("authors", dbo.get("name").toString());
					DBCursor resultSubset = db.collPubs.find(searchObject);
					
					while(resultSubset.hasNext())
					{
						pub = db.getPub(resultSubset.next().get("_id").toString());
						
						List<String> grams = ngrams.getNGrams(pub.title, 1, 3);
						grams.addAll(ngrams.getNGrams(pub.summary, 1, 3));
						
						for (String ngram : grams)
						{
						    List<String> profileIDs = ngramToProfiles.get(ngram);
						    if (profileIDs != null) 
							{
							  profileIDs.add(profileID);
							  countProfileEntries++;
							}
						}
					}
				}
			}
//			}

			if (count % 500 == 0) System.out.println(count + " " + countProfileEntries);
		}
		
		System.out.println("writing...");
//		BufferedWriter writer = new BufferedWriter(new FileWriter(new File(dir, "ngrams_profiles_new.txt")));
		BufferedWriter writer = new BufferedWriter(new FileWriter(new File(dir, "ngrams_profiles.txt")));
		try {
			System.out.println("words: " + words.size());
			int wcount = 0;
		for (String word : words)
		{
			wcount++;
			List<String> profiles = ngramToProfiles.get(word);
			if (profiles.size() == 0) continue;
			
			writer.write(word);
			for (String profile : profiles)
			{
				writer.write(";");
				writer.write(profile);
			}
			
			writer.write("\n");
			if (wcount % 10000 == 0) System.out.println("" + wcount);
		}
		writer.close();
		}
		catch (Exception ex)
	       {
	    	   ex.printStackTrace();
	    	   writer.close();
	       }
	}
	
	public static void createIndex() throws Exception
	{
		long time = System.currentTimeMillis();
		RandomAccessTextFile.createRandomAccessTextFile(new File(dir, "ngrams_profiles.txt"), new RandomAccessTextFileLineListener() 
		{
			int count = 0;
			
			public String createIdentifier(String line) 
			{
				count++;
				if (count % 50000 == 0) System.out.println(count);
				
				int i1 = line.indexOf(";");
				String ngram = line.substring(0, i1);
				
				return ngram;
			}
		}, new File(dir, "ngrams_profiles_index.txt"));
		System.out.println("Index created in " + (System.currentTimeMillis() - time) + " ms.");
	}
	
	public static void mergeFiles() throws Exception
	{
		Map<String, Integer> nGrams = new TreeMap<>();
		//read first old file
		System.out.println("reading old ngrams...");
		BufferedReader reader = new BufferedReader(new FileReader(new File(dir, "ngrams_old.txt")));
		String line = null;
		int count = 0;
		while ((line = reader.readLine()) != null)
		{
			count++;
			nGrams.put(line, 1);
			if (count % 50000 == 0) System.out.println(count);
		}
		reader.close();
		System.out.println("reading new ngrams...");
		count = 0;
		//read the new file
		BufferedReader reader2 = new BufferedReader(new FileReader(new File(dir, "ngrams_new.txt")));
		while ((line = reader2.readLine()) != null)
		{
			count++;
			//add if ngram is not in list
			if(nGrams.get(line) == null)
			{
				nGrams.put(line, 1);
			}			
			if (count % 50000 == 0) System.out.println(count);
		}
		reader2.close();
		
		System.out.println("writing...");
		BufferedWriter writer = new BufferedWriter(new FileWriter(new File(dir, "ngrams_old.txt")));
		
		for (Entry<String, Integer> entries : nGrams.entrySet())
		{
			writer.write(entries.getKey() + "\n");
		}
		writer.close();
		
	}
	
	public static void mergeProfiles() throws Exception
	{
		Map<String, Integer> nGrams = new TreeMap<>();
		//read first old file
		System.out.println("reading old ngrams_profiles...");
		BufferedReader reader = new BufferedReader(new FileReader(new File(dir, "ngrams_profiles_old.txt")));
		String line = null;
		int count = 0;
		while ((line = reader.readLine()) != null)
		{
			count++;
			nGrams.put(line, 1);
			if (count % 50000 == 0) System.out.println(count);
		}
		reader.close();
		System.out.println("reading new ngrams...");
		count = 0;
		//read the new file
		BufferedReader reader2 = new BufferedReader(new FileReader(new File(dir, "ngrams_profiles_new.txt")));
		while ((line = reader2.readLine()) != null)
		{
			count++;
			//add if ngram is not in list
			if(nGrams.get(line) == null)
			{
				nGrams.put(line, 1);
			}			
			if (count % 50000 == 0) System.out.println(count);
		}
		reader2.close();
		
		System.out.println("writing...");
		BufferedWriter writer = new BufferedWriter(new FileWriter(new File(dir, "ngrams_profiles_old.txt")));
		
		for (Entry<String, Integer> entries : nGrams.entrySet())
		{
			writer.write(entries.getKey() + "\n");
		}
		writer.close();
		
	}
}
