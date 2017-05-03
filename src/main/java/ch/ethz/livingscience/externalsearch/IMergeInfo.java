package ch.ethz.livingscience.externalsearch;

import java.util.List;

public interface IMergeInfo 
{
	void addPub(float weighting, List<Integer> indices);
	float getPubWeighting(int index);
	int getPubIndex(int pubIndex, int refIndex);
	int getPubIndicesCount(int index);
	int getPubCount();
	
	void addAuthorIndicesForPub(List<Integer> authorIndices);
	int getAuthorIndexForPub(int pubIndex, int authorIndex);
	int getAuthorCountForPub(int pubIndex);
	
	void addAuthor(String name, String affil);
	String getAuthorName(int index);
	String getAuthorAffil(int index);
	int getAuthorCount();
}
