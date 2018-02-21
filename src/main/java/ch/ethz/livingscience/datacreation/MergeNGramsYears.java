package ch.ethz.livingscience.datacreation;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintStream;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import utils.text.LineListener;

public class MergeNGramsYears
  implements LineListener
{
  static File dir = new File("data/");
  Map<String, String> ngramToData;
  
  public static void main(String[] args) throws Exception {
    MergeNGramsYears c = new MergeNGramsYears();
    c.MergeFiles();
  }
  

  public MergeNGramsYears()
    throws Exception
  {
    ngramToData = new TreeMap();
  }
  
  public void MergeFiles()
    throws Exception
  {
    System.out.println("reading from 2000-2011...");
    readList(new File("data/ngramsYears_2000_2011.txt"));
    

    for (int i = 2012; i <= 2017; i++)
    {
      System.out.println("reading from " + i + "...");
      String fileName = "data/ngramsYears" + i + ".txt";
      readList2(new File(fileName), i);
    }
    

    System.out.println("writing...");
    
    BufferedWriter writer = new BufferedWriter(new FileWriter(new File(dir, "ngramsYears_2004_2017.txt")));
    for (Map.Entry<String, String> entries : ngramToData.entrySet())
    {
      writer.write((String)entries.getKey() + ";" + (String)entries.getValue());
      writer.write("\n");
    }
    writer.close();
  }
  
  public void readList(File file) throws Exception
  {
    BufferedReader reader = new BufferedReader(new FileReader(file));
    String line = null;
    int index = 0;
    while ((line = reader.readLine()) != null)
    {
      newLine(index, line);
      index++;
    }
    reader.close();
  }
  
  public void newLine(int index, String line)
  {
    String[] parts = line.split(";", 6);
    
    String newValue = parts[5] + ";0;0;0;0;0;0";
    ngramToData.put(parts[0], newValue);
  }
  
  public void readList2(File file, int year)
    throws Exception
  {
    BufferedReader reader = new BufferedReader(new FileReader(file));
    String line = null;
    int index = 0;
    while ((line = reader.readLine()) != null)
    {
      newLine2(index, line, year);
      index++;
    }
    reader.close();
  }
  
  public void newLine2(int index, String line, int year)
  {
    String[] parts = line.split(";", 2);
    
    String ngramValue = (String)ngramToData.get(parts[0]);
    int yearRange = year - 2004;
    if (ngramValue != null)
    {

      int regIndex = 0;
      for (int i = 0; i < yearRange; i++)
      {
        regIndex = ngramValue.indexOf(";", regIndex + 1);
      }
      String zeroes = "";
      for (int j = year + 1; j <= 2017; j++)
      {
        zeroes = zeroes + ";0";
      }
      String newValue = ngramValue.substring(0, regIndex) + ";" + parts[1] + zeroes;
      ngramToData.put(parts[0], newValue);



    }
    else if (new Integer(parts[1]).intValue() > 5)
    {

      String value = "";
      for (int i = 2004; i < year; i++)
      {
        value = value + "0;";
      }
      value = value + parts[1];
      for (int j = year + 1; j <= 2017; j++)
      {
        value = value + ";0";
      }
      ngramToData.put(parts[0], value);
    }
  }
}