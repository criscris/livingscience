package ch.ethz.livingscience.data.wikitopics;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SprikiRelations
{
	public Set<String> topics = new HashSet<String>();
	
	public SprikiRelations(File topicsFile)
	{
		System.out.println("SprikiRelations init...");
		long time = System.currentTimeMillis();
		try 
		{
			BufferedReader reader = new BufferedReader(new FileReader(topicsFile));
			String line = null;
			while ((line = reader.readLine()) != null)
			{
				line = line.trim();
				if (line.length() < 1) continue;
				line = line.toLowerCase();
//				if (line.equals("tim")) continue;
				topics.add(line);
			}
			
			System.out.println(topics.size() + " wiki topics loaded in " + (System.currentTimeMillis() - time)  + " ms.");
			
			reader.close();
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
	}
	
	
	public List<String> extractWords(String buffer)
	{
		List<String> words = new ArrayList<String>();
		// extract words
		int wordStart = 0;
		for (int i=0; i<buffer.length(); i++)
		{
			char c = buffer.charAt(i);
			if (!(Character.isLetter(c) || c == '-'))
			{
				if (i - 1 > wordStart)
				{
					String word = buffer.substring(wordStart, i);
					words.add(word);
				}
				wordStart = i + 1;
			}
		}
		String word = buffer.substring(wordStart);
		words.add(word);
		
		return words;
	}
	
	public List<String> getKeywords(List<String> words)
	{
		List<String> keywords = new ArrayList<String>();
		
		for (int w=0; w<words.size(); w++)
		{
			for (int s=Math.min(5, words.size() - w); s>=1; s--) // begin with a 5-word
			{
				String wordgroup = words.get(w);
				for (int k=1; k<s; k++) wordgroup += " " + words.get(w + k);
				
				if (wordgroup.startsWith("the ")) continue;
				if (topics.contains(wordgroup)) // topic match
				{
					keywords.add(wordgroup);

					w += s-1;
					break;
				}
			}
		}
		
		return keywords;
	}
	
	public void exec(SprikiData data) throws Exception
	{
		long time = System.currentTimeMillis();
		HashMap<String, SprikiKeyword> keywords = new HashMap<String, SprikiKeyword>(); // String is the same as SprikiKeyword.label
		List<SprikiResult> allPubs = new ArrayList<SprikiResult>(data.results1.publications);
		allPubs.addAll(data.results2.publications);
		
		for (int i=0; i<allPubs.size(); i++)
		{
			SprikiResult pub = allPubs.get(i);
			pub.keywords = new ArrayList<SprikiKeyword>();
			
			String text = pub.title;
			if (pub.summary != null) text += ". " + pub.summary;
			List<String> words = extractWords(text.toLowerCase());
			
			List<String> kwords = getKeywords(words);
			
			for (String wordgroup : kwords)
			{
				SprikiKeyword keyword = keywords.get(wordgroup);
				if (keyword == null)
				{
					keyword = new SprikiKeyword();
					keyword.label = wordgroup;
					keyword.pcaIndex = -1;
					keywords.put(wordgroup, keyword);
				}
				keyword.occurrences++;
				pub.keywords.add(keyword);
			}
		}
	
		
		Collection<SprikiKeyword> occuredTopicsSet = keywords.values();
		data.keywords = new ArrayList<SprikiKeyword>();
		for (SprikiKeyword keyword : occuredTopicsSet)
		{
			if (keyword.occurrences > 1 && keyword.occurrences < allPubs.size()) // use only topics which occur in more than one paper but not in all
			{
				keyword.pcaIndex =data.keywords.size();
				data.keywords.add(keyword);
			}
		}
		for (int i=0; i<allPubs.size(); i++)
		{
			SprikiResult pub = allPubs.get(i);
			pub.pcaIndex = data.keywords.size() + i;
			
			List<SprikiKeyword>  relevantKeywords = new ArrayList<SprikiKeyword>();
			for (SprikiKeyword keyword : pub.keywords)
			{
				if (keyword.pcaIndex >= 0) relevantKeywords.add(keyword);
			}
			pub.keywords = relevantKeywords;
		}
		
//		System.out.println(data.keywords.size() + " keywords found  in " + (System.currentTimeMillis() - time) + " ms.");
		time = System.currentTimeMillis();
		
		double[][] A = new double[data.keywords.size() + allPubs.size()][data.keywords.size()]; // [objects][data dimensions]
		if (A.length == 0 || A[0].length == 0) return;
		for (SprikiKeyword keyword : data.keywords)
		{
			A[keyword.pcaIndex][keyword.pcaIndex] = 2.0;
		}
		for (SprikiResult pub : allPubs)
		{
			for (SprikiKeyword keyword : pub.keywords)
			{
				A[pub.pcaIndex][keyword.pcaIndex] = 1.0;
			}
		}
		
		PCA pca = new PCA(A);
		double[] pcFirst = new double[A.length];
		pca.getPrincipalComponent(pcFirst, 0);
		double[] pcSecond = new double[A.length];
		pca.getPrincipalComponent(pcSecond, 1);
		normalize(pcFirst, pcSecond);
		
		for (SprikiKeyword keyword : data.keywords)
		{
			keyword.x = (float) pcFirst[keyword.pcaIndex];
			keyword.y = (float) pcSecond[keyword.pcaIndex];
		}
		for (SprikiResult pub : allPubs)
		{
			pub.x = (float) pcFirst[pub.pcaIndex];
			pub.y = (float) pcSecond[pub.pcaIndex];
		}
		
//		System.out.println("PCA computed in  " + (System.currentTimeMillis() - time) + " ms.");
	}
	
	
	private void normalize(double[] pca1, double[] pca2)
	{
		double minX = Double.MAX_VALUE;
		double maxX = -Double.MIN_VALUE;
		double minY = Double.MAX_VALUE;
		double maxY = -Double.MIN_VALUE;
		
		for (int i=0; i<pca1.length; i++)
		{
			minX = Math.min(pca1[i], minX);
			maxX = Math.max(pca1[i], maxX);
			minY = Math.min(pca2[i], minY);
			maxY = Math.max(pca2[i], maxY);
		}
		
		double rangeX = maxX - minX;
		double rangeY = maxY - minY;
		
		for (int i=0; i<pca1.length; i++)
		{
			pca1[i] = (pca1[i] - minX) / rangeX;
			pca2[i] = (pca2[i] - minY) / rangeY; // non-proportional scaling to X
		}
	}
}