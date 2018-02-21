package ch.ethz.livingscience.dblp.api;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import ch.ethz.livingscience.arxiv.ArxivAuthorNames;
import ch.ethz.livingscience.arxiv.ArxivCitation;

import java.util.Set;

import utils.text.CountableSet;
import utils.text.LineListener;
import utils.text.TextFileUtil;

public class ArxivMetaTest 
{
	public static void main(String[] args) throws Exception
	{
		File dir = new File("/Users/cschulz/Documents/data/arxiv/");
		new ArxivMetaTest(new File(dir, "arxivmeta.txt"),
				TextFileUtil.loadList(new File("/Users/cschulz/Documents/projects/2013_namedis/oneDataset/all/familyNames.txt")));
		
//		ArxivAuthorNames.test();
	}
	
	int failedCount = 0;
//	int helbing = 0;
	
	public Set<String> ids = new HashSet<>(); // these ids are without the slash "/" which was used until 2007. 
	  // cond-mat0602006 instead of cond-mat/0602006
	
	public ArxivMetaTest(File file) throws Exception
	{
		this(file, null);
	}
	
	public ArxivMetaTest(File file, List<String> gsFamilyNames) throws Exception
	{
		final CountableSet<String> familyNames = new CountableSet<>();
		final CountableSet<String> familyNamesWIntial = new CountableSet<>();
		
		final Map<String, Integer> gsCounts = new HashMap<>();
		if (gsFamilyNames != null) for (String name : gsFamilyNames) gsCounts.put(name, 0);
		
		long time = System.currentTimeMillis();
		TextFileUtil.loadList(file, new LineListener() 
		{
			public void newLine(int index, String line) 
			{
				ArxivCitation citation = null;
				try
				{
					citation = new ArxivCitation(line);
					if (!citation.toString().equals(line))
					{
						System.out.println(line);
						System.out.println(citation);
					}
					ids.add(citation.arxivID);
					
					for (String author : citation.authors)
					{
						String familyName = ArxivAuthorNames.getFamilyName(author);
						if (familyName != null) 
						{
							familyNames.add(familyName);
							
							Integer count = gsCounts.get(familyName);
							if (count != null) gsCounts.put(familyName, count + 1);
						}
						
						String familyNameWInitial = ArxivAuthorNames.getFirstInitialAndFamilyName(author);
						if (familyNameWInitial != null) familyNamesWIntial.add(familyNameWInitial);
					}
					
					if (index % 500000 == 0) System.out.println(index);
				}
				catch (Exception ex)
				{
					ex.printStackTrace();
					failedCount++;
				}
			}
		});
		System.out.println((System.currentTimeMillis() - time) + " ms for loading arxiv metadata. failedCount=" + failedCount + " ids=" + ids.size());
//		System.out.println("helbing: " + helbing);
		
		List<Entry<String, Integer>> famNames = familyNames.sortedEntriesAscending();
		int totalFamNames = familyNames.counts.size();
		int famNamesWithGT5Pubs = totalFamNames;
		for (int i=0; i<famNames.size(); i++)
		{
			if (famNames.get(i).getValue() < 5) famNamesWithGT5Pubs--;
		}
		System.out.println("totalFamNames=" + totalFamNames + "  famNamesWithGT5Pubs=" + famNamesWithGT5Pubs);
		
		List<Entry<String, Integer>> famINames = familyNamesWIntial.sortedEntriesAscending();
		int totalFamINames = familyNamesWIntial.counts.size();
		int famINamesWithGT5Pubs = totalFamINames;
		for (int i=0; i<famINames.size(); i++)
		{
			if (famINames.get(i).getValue() < 5) famINamesWithGT5Pubs--;
		}
		System.out.println("totalFamINames=" + totalFamINames + "  famINamesWithGT5Pubs=" + famINamesWithGT5Pubs);
		
		CountableSet<String> gsCounts_ = new CountableSet<>();
		gsCounts_.counts = gsCounts;
		List<Entry<String, Integer>> gsNames = gsCounts_.sortedEntriesAscending();
		int total = gsCounts.size();
		int totalGT20Pubs = total;
		for (int i=0; i<gsNames.size(); i++)
		{
			if (gsNames.get(i).getValue() < 5) totalGT20Pubs--;
		}
		System.out.println("GS total=" + total + "  totalGT20Pubs=" + totalGT20Pubs);
	}
}



