package ch.ethz.livingscience.externalsearch;

import java.util.List;

public class Merger 
{
	public MergeInfo process(List<MergeDocument> docs)
	{
		long time = System.currentTimeMillis();
		
		MergeInfo mergeInfo = new MergeInfo();
		PublicationDataMerger2 merger = new PublicationDataMerger2(mergeInfo);
		
		for (MergeDocument doc : docs)
		{
			merger.add(doc);
		}
		
		merger.process();
		
		System.out.println("PublicationDataMergerService took " + (System.currentTimeMillis() - time) + " ms.");
		
		return mergeInfo;
	}
}
