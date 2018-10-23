package ch.ethz.livingscience.ngrams;

public class NGramScore implements Comparable<NGramScore>{
	public String name;
	public double score;
	public float[] result;
	
	public NGramScore(String name, Double score, float[] result)
	{
		this.name = name;
		this.score = score;
		this.result = result;
	}

	public int compareTo(NGramScore o) 
	{
		return (int) ((o.score - score) * Math.pow(10, 20));
	}

}