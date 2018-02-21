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

  static File dir = new File("data/");
  static int range = 13; //range of years for the NGrams
  int totalCount;
  Map<String, Integer> ngramToData;

  public static void main(String[] args) throws Exception
  {
    int port = new Integer(args[0]);
    ProfilesDB db = new ProfilesDB(port);
    
    int year = new Integer(args[1]);
    
    NGramsYearsCreator2 c = new NGramsYearsCreator2();
    for(int i=year - range; i<=year; i++)
    {
      System.out.println("creating file for year " + i + "...");
      c.countNGramsByYear(i, db);
    } 
  }
  
  public NGramsYearsCreator2() throws Exception
  {
    totalCount = 0;
    ngramToData = new TreeMap<>();
  }
  
  public void countNGramsByYear(int year, ProfilesDB db) throws Exception
  {
    NGrams ngrams = NGrams.getInstance();
    
    DBCursor cursor = db.collPubs.find();
    
    int count = 0;
    
    while(cursor.hasNext()) 
    {
      DBObject dbo = cursor.next();
      
      Publication pub = db.toPub(dbo);
      
      if(pub.year==year)
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
    
    String fileName = "ngramsYears" + year + ".txt";
    BufferedWriter writer = new BufferedWriter(new FileWriter(new File(dir, fileName)));
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

