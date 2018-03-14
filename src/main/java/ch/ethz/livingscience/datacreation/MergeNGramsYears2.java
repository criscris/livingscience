package ch.ethz.livingscience.datacreation;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

public class MergeNGramsYears2
{

//  static File dir = new File("data/");
	static File dir = new File("C:/Users/almud/livingscience/data/");
  static int range = 13; //range of years for the NGrams
  Map<String, String> ngramToData;
  int[] totalCounts;
  static int fromYear, toYear;

  public static void main(String[] args) throws Exception
  {
//    toYear = new Integer(args[0]);
	  toYear = 2018;
    fromYear = toYear - range;
    
    MergeNGramsYears2 c = new MergeNGramsYears2();
    c.MergeFiles();
  }
  
  public MergeNGramsYears2() throws Exception
  {
    ngramToData = new TreeMap<>();
    totalCounts = new int[range+1];
  }
  
  public void MergeFiles() throws Exception
  {   
    //read the files
    for(int i=fromYear; i<=toYear;i++)
    {
      System.out.println("reading from " + i + "...");
      String fileName = "data/ngramsYears" + i + ".txt";
      readList(new File(fileName), i);
    }
    
    
    System.out.println("writing...");
    
    String fileName = "ngramsYears_" + fromYear + "_" + toYear + ".txt";
    BufferedWriter writer = new BufferedWriter(new FileWriter(new File(dir, fileName)));
    writer.write("__total");
    for(int k=0; k<=range; k++)
    {
      writer.write(";" + totalCounts[k]);
    }
    writer.write("\n");
    for (Entry<String, String> entries : ngramToData.entrySet())
    {
        int counts = 0;
        String[] parts = entries.getValue().split(";", range+1);
        for(int i=0; i<range;i++)
        {
          counts += new Integer(parts[i]);
        }
        if (counts < 10000)
        {
          writer.write(entries.getKey() + ";" + entries.getValue());
          writer.write("\n");
        }      
    }
    writer.close();
  }
  
  public void readList(File file, int year) throws Exception
  {
    BufferedReader reader = new BufferedReader(new FileReader(file));
    String line = null;
    int index = 0;
    while ((line = reader.readLine()) != null)
    {
      newLine(index, line, year);
      index++;
    }
    reader.close();
  }
  
  public void newLine(int index, String line, int year) 
  {
    String[] parts = line.split(";", 2);
    //check if was already an ngram
    String ngramValue = ngramToData.get(parts[0]);
    int yearRange = year - fromYear;
    if(ngramValue != null)
    {
      //update the new value
      int regIndex = 0;
        for(int i = 0; i < yearRange; i++)
        {
            regIndex = ngramValue.indexOf(";", regIndex+1);
        }
        String zeroes = "";
        for(int j=year+1; j<=toYear;j++)
        {
          zeroes += ";0";
        }
      String newValue = ngramValue.substring(0, regIndex) + ";" + parts[1] + zeroes;
      ngramToData.put(parts[0], newValue);
    }
    else
    {
      //check if occurrences > 5
      int ncounts = new Integer(parts[1]);
      if((ncounts > 5) && (ncounts < 10000))
      {
        totalCounts[year - fromYear] +=1;
        //add to data
        String value = "";
        for(int i = fromYear; i<year; i++)
        {
          value += "0;";
        }
        value += parts[1];
        for(int j = year+1; j<=toYear; j++)
        {
          value += ";0";
        }
        ngramToData.put(parts[0], value);
      }
    }
  }
}
