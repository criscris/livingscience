package ch.ethz.livingscience.externalsearch;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class SearchResult implements Serializable
{
	private static final long serialVersionUID = -2930042237683087686L;
	
	public static int searchIDCounter = 0;
	public int searchID;

	public List<ICustomSearchResult> results;
	public IMergeInfo merged;
	public List<ICustomSearchDocument> allDocs;
	
	public ICustomSearchDocument getFirstDocForMergedPub(int index)
	{
		return allDocs.get(merged.getPubIndex(index, 0));
	}
	
	public void createAllDocsList()
	{
		if (results == null) return;
		allDocs = new ArrayList<ICustomSearchDocument>();
		for (int i=0; i<results.size(); i++)
		{
			ICustomSearchResult result = results.get(i);
			List<? extends ICustomSearchDocument> docs = result.getResults();
			for (int j=0; j<docs.size(); j++)
			{
				allDocs.add(docs.get(j));
			}
		}
	}
}