package ch.ethz.livingscience.ngrams;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import utils.text.LineListener;
import utils.text.TextFileUtil;

public class NGramStore2_inMemory 
{
	public static void writeShorter(String[] args) throws Exception
	{
		File dir = new File("C:/Users/user/Documents/Student_Assistant-Living_Sciences/data");
		
		final BufferedWriter writer = new BufferedWriter(new FileWriter(new File(dir, "/ngramsYears_2004_2017.txt")));
		
		class Counter
		{
			int value = 0;
		}
		final Counter c = new Counter();
		
		// this file is 1900..2012 (both inclusive)
		TextFileUtil.loadList(new File(dir,"ngramsYears.txt"), new LineListener() {
			public void newLine(int index, String line) 
			{
				int i1 = line.indexOf(";");
				String ngram = line.substring(0, i1);
				int[] years = TextFileUtil.intArrayFromString(line.substring(i1 + 1), 100, 13, ";");
				
				int countNonZeros = 0;
				for (int i=0; i<years.length; i++) if (years[i] > 0) countNonZeros++;
				
				if (countNonZeros >= 4)
				{
					StringBuilder sb = new StringBuilder();
					sb.append(ngram);
					for (int i=0; i<years.length; i++)
					{
						sb.append(";");
						sb.append(years[i]);
					}
					
					try 
					{
						writer.write(sb.toString());
						writer.write("\n");
						c.value++;
					} 
					catch (IOException e) 
					{
						e.printStackTrace();
					}
				}
				
				if (index % 50000 == 0) System.out.println(index);
			}
		});
		
		System.out.println(c.value + " ngrams written.");
		writer.close();
	}
	
	public static void main(String[] args) throws Exception
	{
		NGramStore2_inMemory store = new NGramStore2_inMemory(
				new File("C:/Users/user/Documents/Student_Assistant-Living_Sciences/data/ngramsYears_2004_2017.txt"), 2004, 2017);	
	
		System.out.println(Arrays.toString(store.getNormalizedYearCounts("financial crisis")));
	}
	
	
	Map<String, String> ngramToData = new HashMap<>();
	
	final int minYear;
	final int maxYear; 
	final int noOfYears;
	float[] years;
	final int[] zeros;
	
	int[] yearsTotal;
	
	public NGramStore2_inMemory(File file, int minYear, int maxYear) throws Exception
	{
		this.minYear = minYear;
		this.maxYear = maxYear;
		noOfYears = maxYear - minYear + 1;
		zeros = new int[noOfYears];
		years = new float[noOfYears];
		for (int i=0; i<noOfYears; i++) years[i] = minYear + i; 
		
		long time = System.currentTimeMillis();
		TextFileUtil.loadList(file, new LineListener() {
			public void newLine(int index, String line) 
			{
				int i1 = line.indexOf(";");
				String ngram = line.substring(0, i1);
				String years = line.substring(i1 + 1);
				
				ngramToData.put(ngram, years);
			}
		});
		
		String total = ngramToData.remove("__total");
		yearsTotal = TextFileUtil.intArrayFromString(total, 0, noOfYears, ";");
		System.out.println(ngramToData.size() + " ngrams loaded in " + (System.currentTimeMillis() - time) + " ms.");
	}
	
	public int[] getYearCounts(String ngram)
	{
		String value = ngramToData.get(ngram);
		if (value == null) return zeros;
		return TextFileUtil.intArrayFromString(value, 0, noOfYears, ";");
	}
	
	public float[] getNormalizedYearCounts(String ngram)
	{
		float[] n = new float[noOfYears];
		int[] result = getYearCounts(ngram);
		for (int i=0; i<noOfYears; i++)
		{
			n[i] = yearsTotal[i] == 0 ? 0f : ((float) result[i] / yearsTotal[i]);
		}
		return n;
	}
	
	
	public float[] getYears()
	{
		return years;
	}
}
