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
		double res = o.score - score;
		if(res > 0.0) {
			return (int) 1;
		}
		else if(res == 0.0) {
			return (int) 0;
		}else {
			return (int)-1;
		}
	}

}