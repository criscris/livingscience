package ch.ethz.livingscience.ngrams;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;

import ch.ethz.livingscience.data.ProfilesDB;

public class NGrams
{
	// English stop words, from http://www.textfixer.com/resources/common-english-words.txt
//	static final String[] stopWords = { "a", "able", "about", "across", "after", "all", "almost", "also", "am", "among", "an", "and", "any", "are", "as", "at", "be", "because", "been", "but", "by", "can", "cannot", "could", "dear", "did", "do", "does", "either", "else", "ever", "every", "for", "from", "get", "got", "had", "has", "have", "he", "her", "hers", "him", "his", "how", "however", "i", "if", "in", "into", "is", "it", "its", "just", "least", "let", "like", "likely", "may", "me", "might", "most", "must", "my", "neither", "no", "nor", "not", "of", "off", "often", "on", "only", "or", "other", "our", "own", "rather", "said", "say", "says", "she", "should", "since", "so", "some", "than", "that", "the", "their", "them", "then", "there", "these", "they", "this", "tis", "to", "too", "twas", "us", "wants", "was", "we", "were", "what", "when", "where", "which", "while", "who", "whom", "why", "will", "with", "would", "yet", "you", "your" };
	static final String[] stopWords = { "a", "aa", "able", "about", "across", "after", "all", "almost", "also", "am", "among", "an", "and", "any", "are", "as", "at", "be", "because", "been", "but", "by", "can", "cannot", "could", "dear", "did", "do", "does", "either", "else", "ever", "every", "for", "from", "get", "got", "had", "has", "have", "he", "her", "hers", "him", "his", "how", "however", "i", "if", "in", "into", "is", "it", "its", "just", "least", "let", "like", "likely", "may", "me", "might", "most", "must", "my", "neither", "no", "nor", "not", "of", "off", "often", "on", "only", "or", "other", "our", "own", "rather", "said", "say", "says", "she", "should", "since", "so", "some", "than", "that", "the", "their", "them", "then", "there", "these", "they", "this", "tis", "to", "too", "twas", "us", "wants", "was", "we", "were", "what", "when", "where", "which", "while", "who", "whom", "why", "will", "with", "would", "yet", "you", "your" };

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
	
	public List<String> getACMNGrams(String text, int nMin, int nMax, Set<String> shortnames)
	{
		List<String> ngrams = new ArrayList<>();
		if (text == null) return ngrams;
		text = text.toLowerCase();
		
		List<String> words = extractWords(text);
		
		if (nMin <= 1)
		{
			for (String word : words)
			{
				//also take away spaces from nword, instead of list on args, search in db
			    String[] pname = word.split("[\\p{Punct}\\s]+");
			    String fname = "";
			    char lastChar = 0;
			    char sLastChar = 0;
			    char tLastChar = 0;
			    if(word.length()>3) {
			    	lastChar = word.charAt(word.length() - 1);
				    sLastChar = word.charAt(word.length() - 2);
				    tLastChar = word.charAt(word.length() - 3);
			    }
			    
			    for(String p:pname) {
			       if(!stopWordsSet.contains(p)) {fname +=p;}
			    }
				if(shortnames.contains(fname)) {
					ngrams.add(word);
				}	
				else if((word.length()>3)&&(lastChar=='s')&&(shortnames.contains(word.substring(0, word.length() - 1)))) {
					ngrams.add(word.substring(0, word.length() - 1));
				}
				else if((word.length()>3)&&(sLastChar=='e')&&(lastChar=='s')&&(shortnames.contains(word.substring(0, word.length() - 2)))) {
					ngrams.add(word.substring(0, word.length() - 2));
				}
				else if((word.length()>3)&&(tLastChar=='i')&&(sLastChar=='e')&&(lastChar=='s')&&(shortnames.contains(word.substring(0, word.length() - 3)+'y'))) {
					ngrams.add(word.substring(0, word.length() - 3)+'y');
				}
				else if(shortnames.contains(word+'s')) {
					ngrams.add(word+'s');
				}
				else if(shortnames.contains(word+"es")) {
					ngrams.add(word+"es");
				}
				else if((word.length()>2)&&(lastChar=='y')&&(shortnames.contains(word.substring(0, word.length() - 1)+"ies"))) {
					ngrams.add(word.substring(0, word.length() - 1)+"ies");
				}
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
				//also take away spaces from nword, instead of list on args, search in db
			    String[] pname = nword.toString().split("[\\p{Punct}\\s]+");
			    String fname = "";
			    for(String p:pname) {
			       if(!stopWordsSet.contains(p)) {fname +=p;}
			    }
				if(shortnames.contains(fname)) {
					ngrams.add(nword.toString());
				}				
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