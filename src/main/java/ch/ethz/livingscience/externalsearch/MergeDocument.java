package ch.ethz.livingscience.externalsearch;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class MergeDocument implements Serializable, IMergeDocument
{
	private static final long serialVersionUID = -4414307131593114912L;
	
	public int index;
	public float weighting;
	
	public String title;
	public String doi;
	public String url;
	public String journal;
	public int year;
	
	public List<String> authorNames;
	public List<String> authorAffils;
	
	public MergeDocument()
	{
		
	}

	public int getIndex()
	{
		return index;
	}

	public float getWeighting()
	{
		return weighting;
	}

	public String getTitle()
	{
		return title;
	}

	public String getDoi()
	{
		return doi;
	}

	public String getUrl()
	{
		return url;
	}

	public String getJournal()
	{
		return journal;
	}

	public int getYear()
	{
		return year;
	}

	public String getAuthorName(int index)
	{
		return authorNames.get(index);
	}

	public int getAuthorCount()
	{
		return authorNames == null ? 0 : authorNames.size();
	}

	public String getAffiliation(int index)
	{
		return authorAffils.get(index);
	}

	public int getAffiliationCount()
	{
		return authorAffils == null ? 0 : authorAffils.size();
	}
	
	public static List<MergeDocument> create(List<ICustomSearchResult> searchResults)
	{
		List<MergeDocument> mergeDocs = new ArrayList<MergeDocument>();
		
		for (int i=0; i<searchResults.size(); i++)
		{
			ICustomSearchResult result = searchResults.get(i);
			
//			JsArray<PlosDocument> plosDocs = null;
//			if (result.getName().equals(PlosSearchData.name))
//			{
//				plosDocs = ((PlosSearchData) result.getSource()).getResultsJSO();
//			}
			
			List<? extends ICustomSearchDocument> docs = result.getResults();
			for (int j=0; j<docs.size(); j++)
			{
				ICustomSearchDocument doc = docs.get(j);
				MergeDocument mergeDoc = new MergeDocument();
				
				float weighting = 5f;
				float f = 50 - j;
				if (f > 0) weighting += f*0.1f;
				
//				List<String> affils = null;
//				if (plosDocs != null)
//				{
//					PlosDocument plosDoc = plosDocs.get(j);
//					affils = new ArrayList<String>();
//					for (int k=0; k<plosDoc.getAffiliationCount(); k++)
//					{
//						affils.add(plosDoc.getAffiliation(k));
//					}
//				}
				
				mergeDoc.index = i;
				mergeDoc.weighting = weighting;
				mergeDoc.title = doc.getTitle();
				mergeDoc.doi = doc.getDoi();
				mergeDoc.url = doc.getUrl();
				mergeDoc.journal = doc.getJournal();
				mergeDoc.year = doc.getYear();
				mergeDoc.authorNames = doc.getAuthors();
//				mergeDoc.authorAffils = affils;
				
				mergeDocs.add(mergeDoc);
			}
		}
		
		return mergeDocs;
	}
}
