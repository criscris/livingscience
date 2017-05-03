package ch.ethz.livingscience.externalsearch;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class MergeInfo implements Serializable, IMergeInfo
{
	private static final long serialVersionUID = 4327905169741313734L;
	
	private List<Float> weightings = new ArrayList<Float>();
	private List<List<Integer>> indicesList = new ArrayList<List<Integer>>();
	private List<List<Integer>> auhtorIndicesList = new ArrayList<List<Integer>>();
	
	private List<String> authorNames = new ArrayList<String>();
	private List<String> authorAffils = new ArrayList<String>();

	public MergeInfo()
	{
		
	}

	public void addPub(float weighting, List<Integer> indices)
	{
		weightings.add(weighting);
		indicesList.add(indices);
	}

	public float getPubWeighting(int index)
	{
		return weightings.get(index);
	}

	public int getPubIndex(int pubIndex, int refIndex)
	{
		return indicesList.get(pubIndex).get(refIndex);
	}

	public int getPubIndicesCount(int index)
	{
		return indicesList.get(index).size();
	}

	public int getPubCount()
	{
		return indicesList.size();
	}
	

	public void addAuthor(String name, String affil)
	{
		authorNames.add(name);
		authorAffils.add(affil);
	}

	public String getAuthorName(int index)
	{
		return authorNames.get(index);
	}

	public String getAuthorAffil(int index)
	{
		return authorAffils.get(index);
	}


	public int getAuthorCount()
	{
		return authorNames.size();
	}

	
	public void addAuthorIndicesForPub(List<Integer> authorIndices)
	{
		auhtorIndicesList.add(authorIndices);
	}

	public int getAuthorIndexForPub(int pubIndex, int authorIndex)
	{
		return auhtorIndicesList.get(pubIndex).get(authorIndex);
	}

	public int getAuthorCountForPub(int pubIndex)
	{
		return auhtorIndicesList.get(pubIndex).size();
	}
}
