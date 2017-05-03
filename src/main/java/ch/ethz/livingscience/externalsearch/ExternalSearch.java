package ch.ethz.livingscience.externalsearch;

import java.util.ArrayList;
import java.util.List;

public class ExternalSearch 
{
	List<ExternalSearchProvider> providers;
	String query;
	boolean isAuthorRequest;

	public ExternalSearch(List<ExternalSearchProvider> providers, String query, boolean isAuthorRequest)
	{
		this.providers = providers;
		this.query = query;
		this.isAuthorRequest = isAuthorRequest;
	}
	
	boolean[] completed;
	ICustomSearchResult[] results;
	
	SearchResult result;
	
	public void execAsync()
	{
		completed = new boolean[providers.size()];
		results = new ICustomSearchResult[providers.size()];
		
		for (int i=0; i<providers.size(); i++)
		{
			final int index = i;
			final ExternalSearchProvider provider = providers.get(i);
			
			new Thread(new Runnable()
			{
				public void run() 
				{
					results[index] = provider.search(query, isAuthorRequest);
					completed[index] = true;
				}
			}).start();
		}
		
		new Thread(new Runnable() 
		{
			public void run() 
			{
				long time = System.currentTimeMillis();
				while (true)
				{
					try 
					{
						Thread.sleep(500);
					} 
					catch (InterruptedException e) 
					{

					}
					
					boolean allgood = true;
					for (int i=0; i<completed.length; i++)
					{
						if (!completed[i])
						{
							allgood = false;
							break;
						}
					}
					
					if (allgood) break;
					if (System.currentTimeMillis() - time > 30000) break;
				}
				
				doMerging();
			}
		}).start();
	}
	
	private void doMerging()
	{
		List<ICustomSearchResult> resultsList = new ArrayList<>();
		for (int i=0; i<results.length; i++)
		{
			if (results[i] != null) resultsList.add(results[i]);
		}
		
		List<MergeDocument> mergeDocs = MergeDocument.create(resultsList);
		
		SearchResult result = new SearchResult();
		result.merged = getMergeInfo(mergeDocs);
		result.results = resultsList;
		result.createAllDocsList();
		
		this.result = result;
	}
	
	private MergeInfo getMergeInfo(List<MergeDocument> mergeDocs)
	{
		long time = System.currentTimeMillis();
		
		MergeInfo mergeInfo = new MergeInfo();
		PublicationDataMerger2 merger = new PublicationDataMerger2(mergeInfo);
		
		for (MergeDocument doc : mergeDocs)
		{
			merger.add(doc);
		}
		
		merger.process();
		
		System.out.println("Merging took " + (System.currentTimeMillis() - time) + " ms.");
		
		return mergeInfo;
	}
	
	public boolean isDone()
	{
		return result != null;
	}
	
	public SearchResult getResult()
	{
		return result;
	}
}
