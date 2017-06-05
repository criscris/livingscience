package ch.ethz.livingscience.datacreation;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.bson.types.ObjectId;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

import ch.ethz.livingscience.arxiv.ArxivCitation;
import ch.ethz.livingscience.arxiv.api.ArxivXMLAnalyzer;
import ch.ethz.livingscience.data.Profile;
import ch.ethz.livingscience.data.ProfilesDB;
import ch.ethz.livingscience.data.Publication;
import ch.ethz.livingscience.data.affiliation.UniList;
import ch.ethz.livingscience.ngrams.NGrams;
import utils.text.LineListener;
import utils.text.TextFileUtil;


public class NGramsYearsCreator implements LineListener
{

	static File dir = new File("C:/Users/Public/Documents/Documents/LivingScience/data/");

	public static void main(String[] args) throws Exception
	{
		//obtain counts between 2000 and 2014
		NGramsYearsCreator c = new NGramsYearsCreator(2000, 2014);
		c.countNGramsByYear(2000, 2014);
	}
	
	//int[] totalCount;
	int totalCount;
	//Map<String, int[]> ngramToData;
	Map<String, Integer> ngramToData;
	Map<String, String> oldNGram;
	//int years;
	
	public NGramsYearsCreator(int fromYear, int toYear) throws Exception
	{
//		years = toYear - fromYear + 1;
//		//store the counts per year, last index is for total count
//		totalCount = new int[years];
//		Arrays.fill(totalCount, 0);
		totalCount = 0;
		ngramToData = new TreeMap<>();
		oldNGram = new HashMap<>();
	}
	
	public void countNGramsByYear(int fromYear, int toYear) throws Exception
	{
		TextFileUtil.loadList(new File("C:/Users/Public/Documents/Documents/LivingScience/data/ngramsYears_2000_2011.txt"), this);
		NGrams ngrams = NGrams.getInstance();
		ProfilesDB db = new ProfilesDB(27013);
		
		DBCursor cursor = db.collPubs.find();
		
		int count = 0;
		
		while(cursor.hasNext()) 
		{
			DBObject dbo = cursor.next();
			
			Publication pub = db.toPub(dbo);
			
			if(pub.year==2012)
			{
				count++;
				
				for (String ngram: ngrams.getNGrams(pub.title, 1, 3))
				{
					//increase total count
					//totalCount[pub.year - 2000] += 1;
					totalCount++;
					
					//int[] countsNGram = ngramToData.get(ngram);
					
					//check if ngram is already in list
					if (ngramToData.get(ngram) != null)
					{
						//increase count for the nGram
						//countsNGram[pub.year - 2000] +=1;
						int countsNGram = ngramToData.get(ngram) + 1;
						ngramToData.put(ngram, countsNGram);
						//increase total count for the good ngrams
						//countsNGram[years] +=1;
					}
					else
					{
						//add new entry
//						countsNGram = new int[years+1];
//						Arrays.fill(countsNGram, 0);
//						countsNGram[pub.year - 2000] = 1;
//						countsNGram[years] = 1;
//						ngramToData.put(ngram, countsNGram);
						ngramToData.put(ngram, 1);
					}
//					if(count == 60000)
//					{
//						System.out.println("pub id: " + pub.id);
//					}
				}
				for (String ngram : ngrams.getNGrams(pub.summary, 1, 3))
				{
//					//increase total count
//					totalCount[pub.year - 2000] += 1;
					totalCount++;
//					
//					int[] countsNGram = ngramToData.get(ngram);
//					
//					//check if ngram is already in list
					if ( ngramToData.get(ngram) != null)
					{
//						//increase count for the nGram
//						countsNGram[pub.year - 2000] +=1;
//						countsNGram[years] +=1;
						int countsNGram = ngramToData.get(ngram) + 1;
						ngramToData.put(ngram, countsNGram);
					}
					else
					{
//						//add new entry
//						countsNGram = new int[years+1];
//						Arrays.fill(countsNGram, 0);
//						countsNGram[pub.year - 2000] = 1;
//						countsNGram[years] = 1;
//						ngramToData.put(ngram, countsNGram);
						ngramToData.put(ngram, 1);
					}
				}	
				if (count % 20000 == 0) System.out.println(count + " counts");
			}
		}
		

		//int skippedBig = 0;
		System.out.println("writing...");
		
		BufferedWriter writer = new BufferedWriter(new FileWriter(new File(dir, "ngramsYears2000_2013a.txt")));
		//write total line
		writer.write("_total");
//		for(int i=0; i<years; i++)
//		{
//			writer.write(";" + totalCount[i]);
//		}
		writer.write(";" + oldNGram.get("_total") + ";" + totalCount);
		writer.write("\n");
		for (Entry<String, Integer> entries : ngramToData.entrySet())
		{
//			int[] c = entries.getValue();
			int c = entries.getValue();
//			if (c[years] > 10000) 
//			{
//				skippedBig++;
//				continue;
//			}
			//if (c < 5) continue;
			String key = entries.getKey();
			writer.write(key);
			if(oldNGram.get(key) != null)
			{
				writer.write(";" + oldNGram.get(key) + ";" + c);
			}
			else
			{
				writer.write(";0;0;0;0;0;0;0;0;0;0;0;0;" + c);
			}
//			for(int i=0; i<years; i++)
//			{
//				writer.write(";" + c[i]);
//			}
			writer.write("\n");
		}
		writer.close();
		//System.out.println("skippedBig=" + skippedBig);
	}
	
	public void newLine(int index, String line) 
	{
		//List<String> parts = TextFileUtil.split(line, ";");
		String[] parts = line.split(";", 2);
		if(parts[0].equals("_total"))
		{
//			for(int i=0; i<12; i++)
//			{
//				totalCount[i] = new Integer(parts.get(i+1));
//			}
			oldNGram.put("_total", parts[1]);
		}
		else
		{
//			int[] ngramCounts = new int[years+1];
//			int totalCounts = 0;
//			for(int i=0; i<12; i++)
//			{
//				ngramCounts[i] = new Integer(parts.get(i+1));
//				totalCounts += ngramCounts[i];
//			}
//			ngramCounts[years] = totalCounts;
//			ngramToData.put(parts.get(0), ngramCounts);
			oldNGram.put(parts[0], parts[1]);
			ngramToData.put(parts[0],0);
		}
		
	}
}
