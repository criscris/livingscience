package ch.ethz.livingscience.externalsearch;

import java.util.List;

public interface ICustomSearchResult
{
//	Object getSource();
	List<? extends ICustomSearchDocument> getResults();
	int getTotalResults();
	String getName();
}
