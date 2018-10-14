package ch.ethz.livingscience.datacreation;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;


import com.mongodb.DBCursor;
import com.mongodb.DBObject;

import ch.ethz.livingscience.data.ProfilesDB;
import ch.ethz.livingscience.data.Publication;
import ch.ethz.livingscience.ngrams.NGrams;

/* 
 * Count number of different pubs where an ngram appears rather than total counts and divide by #pubs that year
 */
public class NGramsYearsPubCreator
{

	static File dir = new File("C:/Users/almud/livingscience/data");

	public static void main(String[] args) throws Exception
	{
		//obtain counts between 2000 and 2014
		NGramsYearsPubCreator c = new NGramsYearsPubCreator(2005, 2018);
		c.countNGramsByYear(2005, 2018);
	}
	
	int[] totalCount;
	Map<String, int[]> ngramToData;
	Map<String, String> oldNGram;
	int years;
	
	public NGramsYearsPubCreator(int fromYear, int toYear) throws Exception
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
				List<String> pubNgrams = ngrams.getNGrams(pub.title, 1, 3);
				pubNgrams.addAll(ngrams.getNGrams(pub.summary, 1, 3));
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
				if (count % 20000 == 0) System.out.println(count + " counts");
			}
		}
		
		System.out.println("writing...");
		
		String fileName = "ngramsYears_" + Integer.toString(fromYear) + "_" + Integer.toString(toYear) + "b.txt";
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
//			if (c[years] > 10000) 
//			{
//				skippedBig++;
//				continue;
//			}
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
		//System.out.println("skippedBig=" + skippedBig);
	}
}
