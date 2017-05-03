package ch.ethz.livingscience.externalsearch;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class PublicationDataMerger2
{
	private List<IMergeDocument> docs;
	private IMergeInfo mergeInfo;
	private List<UniquePub> uniquePubs;
	private List<UniquePub> currentProviderPubs;
	
	public PublicationDataMerger2(IMergeInfo result)
	{
		this.mergeInfo = result;
		docs = new ArrayList<IMergeDocument>();
		uniquePubs = new ArrayList<UniquePub>();
		
	}
	
	int currentProvider = -1;
	public void add(IMergeDocument doc)
	{
		docs.add(doc);
		
		int index = doc.getIndex();
		if (currentProvider != index)
		{
			if (currentProviderPubs != null) uniquePubs.addAll(currentProviderPubs);
			
			currentProviderPubs = new ArrayList<UniquePub>();
			currentProvider = index;
		}
		
		checkForDuplication(doc, docs.size() - 1);
	}
	
	public void process()
	{
		// pubs
		if (currentProviderPubs != null) uniquePubs.addAll(currentProviderPubs);
		
		Collections.sort(uniquePubs, new Comparator<UniquePub>()
		{
			public int compare(UniquePub o1, UniquePub o2)
			{
				return (int) (o2.weighting*1000f - o1.weighting*1000f);
			}
		});
		
		for (UniquePub pub : uniquePubs)
		{
			mergeInfo.addPub(pub.weighting, pub.origRefs);
		}
			
		// authors
		fillUniqueAuthors();
	}
	
//	private static final String NOAFFIL = "n/a";
	private void fillUniqueAuthors()
	{
		Map<String, UniqueAuthorInfo> authorNames = new HashMap<String, UniqueAuthorInfo>(); // name
		for (UniquePub pub : uniquePubs)
		{
			// choose best merge doc
			IMergeDocument doc = null;
			for (int j=0; j<pub.origRefs.size(); j++)
			{
				IMergeDocument md = docs.get(pub.origRefs.get(j));
				if (md.getAffiliationCount() > 0)
				{
					doc = md;
					break;
				}
			}
			if (doc == null) doc = docs.get(pub.origRefs.get(0)); // take first one
			
			
			
			for (int j=0; j<doc.getAuthorCount(); j++)
			{
				String authorName = doc.getAuthorName(j);
				UniqueAuthorInfo info = authorNames.get(authorName);
				if (info == null)
				{
					info = new UniqueAuthorInfo();
					if (j < doc.getAffiliationCount())
					{
						info.affil =  doc.getAffiliation(j);
					}
					authorNames.put(authorName, info);
				}
				info.weighting += pub.weighting;
				
				pub.authors.add(info);
			}
		}

		List<Entry<String, UniqueAuthorInfo>> entryList = new ArrayList<Entry<String, UniqueAuthorInfo>>(authorNames.entrySet());
		
		Collections.sort(entryList, new Comparator<Entry<String, UniqueAuthorInfo>>()
		{
			public int compare(Entry<String, UniqueAuthorInfo> arg0, Entry<String, UniqueAuthorInfo> arg1)
			{
				return (int) ((arg1.getValue().weighting - arg0.getValue().weighting)*1000f);
			}
		});
	
		for (int i=0; i<entryList.size(); i++)
		{
			Entry<String, UniqueAuthorInfo> entry = entryList.get(i);
			String authorName = entry.getKey();
			UniqueAuthorInfo uauthor = entry.getValue();
			uauthor.index = i;
			mergeInfo.addAuthor(authorName, uauthor.affil);
		}
		
		for (UniquePub pub : uniquePubs)
		{
			List<Integer> authorIndices = new ArrayList<Integer>();
			for (UniqueAuthorInfo author : pub.authors)
			{
				authorIndices.add(author.index);
			}
			
			mergeInfo.addAuthorIndicesForPub(authorIndices);
		}
	}
	
	private void checkForDuplication(IMergeDocument doc, int index)
	{
		if (doc.getTitle() == null || doc.getTitle().length() < 2) return; // don't consider this document
		
//		System.out.println("check for dup: " + index);
		UniquePub existingPub = null;
		for (UniquePub pub : uniquePubs)
		{
			if (pub.isSame(doc.getDoi(), doc.getTitle(), doc.getYear()))
			{
				existingPub = pub;
				break;
			}	
		}
		
		if (existingPub != null)
		{
			// add possible new data
			if (existingPub.doi == null) existingPub.doi = doc.getDoi();
			existingPub.origRefs.add(index);
			existingPub.weighting += doc.getWeighting();
		}
		else
		{
			UniquePub pub = new UniquePub();
			pub.doi = doc.getDoi();
			pub.titleForDistance = doc.getTitle().toLowerCase();
			pub.year = doc.getYear();
			pub.weighting = doc.getWeighting();
			pub.origRefs.add(index);
			
			currentProviderPubs.add(pub);
		}
	}
}

class UniqueAuthorInfo
{
	String affil;
	float weighting;
	
	int index;
}

class UniquePub
{
	String doi;
	String titleForDistance;
	int year;
	
	float weighting;
	List<Integer> origRefs = new ArrayList<Integer>(1);
	
	boolean isSame(String doi, String title, int year)
	{
		return (this.doi != null && doi != null && this.doi.equalsIgnoreCase(doi)) || 
				(Math.abs(this.year - year) <= 1 && LevenshteinDistance.getLevenshteinDistance(this.titleForDistance, title.toLowerCase()) < 5);
	}
	
	List<UniqueAuthorInfo> authors = new ArrayList<UniqueAuthorInfo>();
}
