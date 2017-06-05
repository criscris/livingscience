package ch.ethz.livingscience.datacreation;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import utils.text.LineListener;

public class MergeNGramsYears implements LineListener
{

	static File dir = new File("C:/Users/Public/Documents/Documents/LivingScience/data/");

	public static void main(String[] args) throws Exception
	{
		MergeNGramsYears c = new MergeNGramsYears();
		c.MergeFiles();
	}
	
	Map<String, String> ngramToData;
	
	public MergeNGramsYears() throws Exception
	{
		ngramToData = new TreeMap<>();
	}
	
	public void MergeFiles() throws Exception
	{
		//read first full file
		readList(new File("C:/Users/Public/Documents/Documents/LivingScience/data/ngramsYears_2000_2013.txt"));
		//read the new file
		readList2(new File("C:/Users/Public/Documents/Documents/LivingScience/data/ngramsYears2014.txt"));
		
		System.out.println("writing...");
		
		BufferedWriter writer = new BufferedWriter(new FileWriter(new File(dir, "ngramsYears_2000_2014.txt")));
		for (Entry<String, String> entries : ngramToData.entrySet())
		{
            writer.write(entries.getKey() + ";" + entries.getValue());
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
		String[] parts = line.split(";", 2);
		//add a new 0 for the new year
        String newValue = parts[1] + ";0";
        ngramToData.put(parts[0], newValue);
		
	}
	
	public void readList2(File file) throws Exception
	{
		BufferedReader reader = new BufferedReader(new FileReader(file));
		String line = null;
		int index = 0;
		while ((line = reader.readLine()) != null)
		{
			newLine2(index, line);
			index++;
		}
		reader.close();
	}
	
	public void newLine2(int index, String line) 
	{
		String[] parts = line.split(";", 2);
		//check if was already an ngram
		String ngramValue = ngramToData.get(parts[0]);
		if(ngramValue != null)
		{
			//update the new value
			int regIndex = 0;
		    for(int i = 0; i < 14; i++)
		    {
		        regIndex = ngramValue.indexOf(";", regIndex+1);
		    }
			String newValue = ngramValue.substring(0, regIndex) + ";" + parts[1];
			ngramToData.put(parts[0], newValue);
		}
		else
		{
			//check if occurrences > 5
			if(new Integer(parts[1]) > 5)
			{
				//add to data
				String value = "0;0;0;0;0;0;0;0;0;0;0;0;0;0;" + parts[1];
				ngramToData.put(parts[0], value);
			}
		}
	}
}
