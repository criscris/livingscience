package ch.ethz.livingscience.externalsearch;


public interface IMergeDocument
{
	int getIndex();
	float getWeighting();
	
	String getTitle();
	String getDoi();
	String getUrl();
	String getJournal();
	int getYear();
	  
	String getAuthorName(int index);
	int getAuthorCount();
	
	String getAffiliation(int index);
	int getAffiliationCount();
}
