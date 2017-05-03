package ch.ethz.livingscience.ngrams;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class NGrams
{
	// English stop words, from http://www.textfixer.com/resources/common-english-words.txt
	static final String[] stopWords = { "a", "able", "about", "across", "after", "all", "almost", "also", "am", "among", "an", "and", "any", "are", "as", "at", "be", "because", "been", "but", "by", "can", "cannot", "could", "dear", "did", "do", "does", "either", "else", "ever", "every", "for", "from", "get", "got", "had", "has", "have", "he", "her", "hers", "him", "his", "how", "however", "i", "if", "in", "into", "is", "it", "its", "just", "least", "let", "like", "likely", "may", "me", "might", "most", "must", "my", "neither", "no", "nor", "not", "of", "off", "often", "on", "only", "or", "other", "our", "own", "rather", "said", "say", "says", "she", "should", "since", "so", "some", "than", "that", "the", "their", "them", "then", "there", "these", "they", "this", "tis", "to", "too", "twas", "us", "wants", "was", "we", "were", "what", "when", "where", "which", "while", "who", "whom", "why", "will", "with", "would", "yet", "you", "your" };
	
	Set<String> stopWordsSet;
	
	protected NGrams()
	{
		stopWordsSet = new HashSet<>();
		for (String stopword : stopWords) stopWordsSet.add(stopword);
	}
	
	static NGrams singleton;
	public static final synchronized NGrams getInstance()
	{
		if (singleton == null) singleton = new NGrams();
		return singleton;
	}
	
	public List<String> getNGrams(String text, int nMin, int nMax)
	{
		List<String> ngrams = new ArrayList<>();
		if (text == null) return ngrams;
		text = text.toLowerCase();
		
		List<String> words = extractWords(text);
		
		if (nMin <= 1)
		{
			for (String word : words)
			{
				if (!stopWordsSet.contains(word)) ngrams.add(word);
			}
		}
		
		for (int i=Math.max(nMin, 2); i<=nMax; i++)
		{
			for (int j=0; j<=words.size()-i; j++)
			{
				// is start word or end word a stop word?
				if (stopWordsSet.contains(words.get(j)) || stopWordsSet.contains(words.get(j+i-1))) continue;
				
				StringBuilder nword = new StringBuilder();
				for (int k=0; k<i; k++)
				{
					nword.append(words.get(j + k));
					if (k < i - 1) nword.append(" ");
				}
				ngrams.add(nword.toString());
			}
		}
		
		return ngrams;
	}
	
	public static List<String> extractWords(String text)
	{
		List<String> words = new ArrayList<>();
		int wordStart = 0;
		for (int i=0; i<text.length(); i++)
		{
			char c = text.charAt(i);
			if (!Character.isLetter(c))
			{
				if (i - 1 > wordStart)
				{
					String word = text.substring(wordStart, i);
					words.add(word);
				}
				wordStart = i + 1;
			}
		}
		if (wordStart < text.length() - 1) words.add(text.substring(wordStart, text.length()));
		return words;
	}
}