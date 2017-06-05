package ch.ethz.livingscience.datacreation;

import java.io.BufferedWriter;
import java.io.File;
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

import com.mongodb.DBCursor;
import com.mongodb.DBObject;

public class NGramsCreator 
{
	static File dir = new File("C:/Users/Public/Documents/Documents/LivingScience/data/arxivngrams");

	public static void main(String[] args) throws Exception
	{
		//countNGrams();
		//ngramToProfiles();
		//createIndex();
	}
	
	public static void countNGrams() throws Exception
	{
//		Map<String, Integer> ngramCounts = new TreeMap<>();
//		NGrams ngrams = NGrams.getInstance();
//		ProfilesDB db = new ProfilesDB(27013);
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
//			for (String ngram: ngrams.getNGrams(pub.title, 1, 3))
//			{
//				if(ngramCounts.get(ngram) != null)
//				{
//					ngramCounts.put(ngram, ngramCounts.get(ngram) + 1);
//				}
//				else
//				{
//					ngramCounts.put(ngram, 1);
//				}
//			}
//			for (String ngram : ngrams.getNGrams(pub.summary, 1, 3))
//			{
//				if(ngramCounts.get(ngram) != null)
//				{
//					ngramCounts.put(ngram, ngramCounts.get(ngram) + 1);
//				}
//				else
//				{
//					ngramCounts.put(ngram, 1);
//				}
//			}
//			
//			if (count % 20000 == 0) System.out.println(count + " ");
//		}
//		
//		int goodNgrams = 0;
//		int skippedBig = 0;
//		
//		System.out.println("writing...");
//		
//		BufferedWriter writer = new BufferedWriter(new FileWriter(new File(dir, "ngrams2.txt")));
//	
//		for (Entry<String, Integer> entries : ngramCounts.entrySet())
//		{
//			int c = entries.getValue();
//			if (c > 10000) 
//			{
//				skippedBig++;
//				continue;
//			}
//			if (c < 5)
//			{
//				continue;
//			}
//			goodNgrams++;
//			writer.write(entries.getKey() + "\n");
//		}
//		writer.close();
//		System.out.println(goodNgrams + " good n-grams. skippedBig=" + skippedBig);
		
		CountableSet<String> ngramCounts = new CountableSet<>();
		NGrams ngrams = NGrams.getInstance();
		ProfilesDB db = new ProfilesDB(27013);
		
		int count = 0;
		DBCursor cursor = db.collPubs.find();
		while(cursor.hasNext()) 
		{
			DBObject dbo = cursor.next();
			count++;
			
			Publication pub = db.toPub(dbo);
			
		
			for (String ngram: ngrams.getNGrams(pub.title, 1, 3))
			{
				ngramCounts.add(ngram);
			}
			for (String ngram : ngrams.getNGrams(pub.summary, 1, 3))
			{
				ngramCounts.add(ngram);
			}
			
			if (count % 20000 == 0) System.out.println(count + " " + ngramCounts.counts.size());
		}
		
		List<String> goodNgrams = new ArrayList<>();
		
		int skippedBig = 0;
		for (Entry<String, Integer> entries : ngramCounts.counts.entrySet())
		{
			int c = entries.getValue();
			if (c > 10000) 
			{
				skippedBig++;
				continue;
			}
			if (c < 5) continue;
			goodNgrams.add(entries.getKey());
		}
		System.out.println(goodNgrams.size() + " good n-grams. skippedBig=" + skippedBig);
		Collections.sort(goodNgrams);
		System.out.println("sorted.");
		TextFileUtil.writeList(goodNgrams, new File(dir, "ngrams.txt"));

	}
	
	public static void ngramToProfiles() throws Exception
	{
		ProfilesDB db = new ProfilesDB(27013);
		NGrams ngrams = NGrams.getInstance();
		
		Map<String, List<String>> ngramToProfiles = new HashMap<>();
		List<String> words = TextFileUtil.loadList(new File(dir, "ngrams.txt"));
		for (String ngram : words)
		{
			ngramToProfiles.put(ngram, new ArrayList<String>());
		}
		
		int count = 0;
		int countProfileEntries = 0;
		DBCursor cursor = db.collProfilesAuto.find();
		System.out.println("parsing...");
		while(cursor.hasNext()) 
		{
			DBObject dbo = cursor.next();
			count++;
			
			String profileID = dbo.get("_id").toString();
			
			for (String pubID : ProfilesDB.getList(dbo.get("pubs")))
			{
				Publication pub = db.getPub(pubID);
				
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

			if (count % 500 == 0) System.out.println(count + " " + countProfileEntries);
		}
		
		System.out.println("writing...");
		BufferedWriter writer = new BufferedWriter(new FileWriter(new File(dir, "ngrams_profiles.txt")));
		for (String word : words)
		{
			List<String> profiles = ngramToProfiles.get(word);
			if (profiles.size() == 0) continue;
			
			writer.write(word);
			for (String profile : profiles)
			{
				writer.write(";");
				writer.write(profile);
			}
			
			writer.write("\n");
		}
		writer.close();
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
}
