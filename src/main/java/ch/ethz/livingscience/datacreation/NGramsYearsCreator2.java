package ch.ethz.livingscience.datacreation;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import com.mongodb.DBCursor;
import com.mongodb.DBObject;

import ch.ethz.livingscience.data.ProfilesDB;
import ch.ethz.livingscience.data.Publication;
import ch.ethz.livingscience.ngrams.NGrams;


public class NGramsYearsCreator2
{

	static File dir = new File("C:/Users/Public/Documents/Documents/LivingScience/data/");

	public static void main(String[] args) throws Exception
	{
		NGramsYearsCreator2 c = new NGramsYearsCreator2();
		c.countNGramsByYear();
	}
	
	int totalCount;
	Map<String, Integer> ngramToData;
	
	public NGramsYearsCreator2() throws Exception
	{
		totalCount = 0;
		ngramToData = new TreeMap<>();
	}
	
	public void countNGramsByYear() throws Exception
	{
		NGrams ngrams = NGrams.getInstance();
		ProfilesDB db = new ProfilesDB(27013);
		
		DBCursor cursor = db.collPubs.find();
		
		int count = 0;
		
		while(cursor.hasNext()) 
		{
			DBObject dbo = cursor.next();
			
			Publication pub = db.toPub(dbo);
			
			if(pub.year==2014)
			{
				count++;
				
				for (String ngram: ngrams.getNGrams(pub.title, 1, 3))
				{
					//increase total count
					totalCount++;
					
					//check if ngram is already in list
					if (ngramToData.get(ngram) != null)
					{
						//increase count for the nGram
						int countsNGram = ngramToData.get(ngram) + 1;
						ngramToData.put(ngram, countsNGram);
					}
					else
					{
						//add new entry
						ngramToData.put(ngram, 1);
					}
				}
				for (String ngram : ngrams.getNGrams(pub.summary, 1, 3))
				{
					//increase total count
					totalCount++;
					
					//check if ngram is already in list
					if ( ngramToData.get(ngram) != null)
					{
						//increase count for the nGram
						int countsNGram = ngramToData.get(ngram) + 1;
						ngramToData.put(ngram, countsNGram);
					}
					else
					{
						//add new entry
						ngramToData.put(ngram, 1);
					}
				}	
				if (count % 20000 == 0) System.out.println(count + " counts");
			}
		}
		
		System.out.println("writing...");
		
		BufferedWriter writer = new BufferedWriter(new FileWriter(new File(dir, "ngramsYears2014.txt")));
		//write total line
		writer.write("__total");
		writer.write(";" + totalCount);
		writer.write("\n");
		for (Entry<String, Integer> entries : ngramToData.entrySet())
		{
			writer.write(entries.getKey() + ";" + entries.getValue());
			writer.write("\n");
		}
		writer.close();
	}
}

