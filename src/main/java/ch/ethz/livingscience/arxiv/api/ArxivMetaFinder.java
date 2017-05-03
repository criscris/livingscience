package ch.ethz.livingscience.arxiv.api;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

import ch.ethz.livingscience.arxiv.ArxivAuthorNames;
import ch.ethz.livingscience.arxiv.ArxivCitation;
import utils.lang.ResizableIntArray;
import utils.text.LineListener;
import utils.text.TextFileUtil;
import utils.text.distance.Levenshtein;

public class ArxivMetaFinder 
{
	public static void main(String[] args) throws Exception
	{		
//		ArxivJournalParser.test();
//		if (true) return;
		
		File dir = new File("/Users/cschulz/Documents/data/arxiv/");
		ArxivMetaFinder arxivMetaFinder = new ArxivMetaFinder(new File(dir, "arxivmeta.txt"));
		
		ArxivCitation citation = new ArxivCitation();
		citation.year = 2010;
		citation.authors.add("d helbing");
		citation.authors.add("s balietti");
		citation.title = "How citation boosts promote scientific paradigm shifts and Nobel Prizes";
		citation.journal = "PLoS One"; 
		citation.volume =  "2011";
		citation.issue = "18975";
		citation.pages = "5-6";
		long time = System.currentTimeMillis();
		System.out.println("matching arxivID = " + arxivMetaFinder.findArxivIDMostSimilarTo(citation));
		System.out.println((System.currentTimeMillis() - time) + " ms for searching.");
		
//		matchReferences(new File(dir, "arxivreferences.txt"), arxivMetaFinder);
	}
	
	
	static int refCount = 0;
	static int foundCount = 0;
	public static void matchReferences(File referencesFile, final ArxivMetaFinder finder) throws Exception
	{
		final long startTime = System.currentTimeMillis();
		
		TextFileUtil.loadList(referencesFile, new LineListener() 
		{
			public void newLine(int index, String line) 
			{
				ArxivCitation citation = null;
				try
				{
					citation = new ArxivCitation(line);
					if (!citation.toString().equals(line))
					{
						System.out.println(line);
						System.out.println(citation);
					}
					if (citation.arxivID.length() > 0) 
					{
//						arxivIDKnownCount++;
						
//						if (meta.ids.contains(citation.arxivID)) arxivIDFoundCount++;
//						else System.out.println(citation.arxivID);
					}
					
					System.out.println(refCount + " S " + citation);
					ArxivCitation foundCitation = finder.findArxivIDMostSimilarTo(citation);
					System.out.println(refCount + " F " + foundCitation);
					
					refCount++;
					if (foundCitation != null) foundCount++;
					if (refCount % 500 == 0) 
					{
						long dt = System.currentTimeMillis() - startTime;
						
						System.out.println(refCount + " pubs. " + foundCount + " found pub. " + dt + " ms. " + dt / refCount + " ms per pub.");
					}
					
				}
				catch (Exception ex)
				{
					ex.printStackTrace();
				}
			}
		});
	}
	
	Map<String, ArxivCitation> idToMeta = new HashMap<>();
	List<ArxivCitation> metaList = new ArrayList<>();
	
	public ArxivMetaFinder(File arxivMetaFile) throws Exception
	{
		long time = System.currentTimeMillis();
		TextFileUtil.loadList(arxivMetaFile, new LineListener() 
		{
			public void newLine(int index, String line) 
			{
				ArxivCitation citation = null;
				try
				{
					citation = new ArxivCitation(line);
					if (idToMeta.get(citation.arxivID) == null) 
					{
						idToMeta.put(citation.arxivID, citation);
						metaList.add(citation);
					}
					
					if (index % 100000 == 0) System.out.println(index);
				}
				catch (Exception ex)
				{
					ex.printStackTrace();
				}
			}
		});
		System.out.println((System.currentTimeMillis() - time) + " ms for loading arxiv metadata. ids=" + idToMeta.size());
		
		time = System.currentTimeMillis();
		initIndices();
		System.out.println( (System.currentTimeMillis() - time) + " ms for initializing indices.");
		
		
	}
	
	void initIndices() throws Exception
	{
		Collections.sort(metaList, new Comparator<ArxivCitation>() 
		{
			public int compare(ArxivCitation o1, ArxivCitation o2) 
			{
				return o1.year - o2.year;
			}
		});
		
		scores = new float[metaList.size()];
		
		authorIndex = new AuthorIndex();
		titleIndex = new TitleIndex();
		journalIndex = new JournalIndex();
		yearIndex = new YearIndex();
	}
	
	AuthorIndex authorIndex;
	TitleIndex titleIndex;
	JournalIndex journalIndex;
	YearIndex yearIndex;
	
	public float lastBestScore()
	{
		return lastBestScore;
	}
	
	float[] scores;
	float lastBestScore = 0f;
	public synchronized ArxivCitation findArxivIDMostSimilarTo(ArxivCitation citation)
	{
		Arrays.fill(scores, 0f);
		
		yearIndex.computeScore(citation);
		titleIndex.computeScore(citation);
		authorIndex.computeScore(citation);
		journalIndex.computeScore(citation);
		
		class Result implements Comparable<Result>
		{
			float score;
			int index;
			
			public int compareTo(Result o) 
			{
				return (int) ((score - o.score) * 10000f);
			}
			
			public Result(float score, int index)
			{
				this.score = score;
				this.index = index;
			}
		}
		
		PriorityQueue<Result> bestResults = new PriorityQueue<>();
		
//		int countNonZero = 0;
		for (int i=0; i<scores.length; i++)
		{
			float score = scores[i];
			if (score >= 2.1f) 
			{
//				countNonZero++;
				
				Result worstOfBest = bestResults.peek();
				if (worstOfBest == null || score >= worstOfBest.score)
				{
					bestResults.add(new Result(score, i));
					if (bestResults.size() > 10)
					{
						bestResults.poll();
					}
				}
			}
		}
//		System.out.println(countNonZero + " qualifying scores.");
		
		
		Result r = null;
		Result[] bestResultArray = new Result[bestResults.size()];
		int j = 0;
		while ((r = bestResults.poll()) != null)
		{
//			System.out.println(r.score + " " + metaList.get(r.index));
			bestResultArray[j] = r;
			j++;
		}
		
		lastBestScore = 0f;
		if (bestResultArray.length == 0) return null;
		if (bestResultArray.length == 1)
		{
			lastBestScore = bestResultArray[0].score;
			return metaList.get(bestResultArray[0].index);
		}
		
		lastBestScore = bestResultArray[bestResultArray.length - 1].score;
		if (bestResultArray[bestResultArray.length - 1].score > bestResultArray[bestResultArray.length - 2].score)
		{
			return metaList.get(bestResultArray[bestResultArray.length - 1].index);
		}
		return null; 
	}
	
	interface Scorer
	{
		void computeScore(ArxivCitation citation);
	}
	
	
	class AuthorIndex implements Scorer
	{
		Map<String, ResizableIntArray> familyNameToIndex = new HashMap<>();
		
		public AuthorIndex()
		{
			for (int i=0; i<metaList.size(); i++)
			{
				ArxivCitation citation = metaList.get(i);
				
				for (String author : citation.authors)
				{
					String familyName = ArxivAuthorNames.getFamilyName(author);
					if (familyName == null) continue;
					
					ResizableIntArray indices = familyNameToIndex.get(familyName);
					if (indices == null)
					{
						indices = new ResizableIntArray(1);
						familyNameToIndex.put(familyName, indices);
					}
					indices.add(i);
				}
			}
		}
		
		public void computeScore(ArxivCitation citation) 
		{
			Set<Integer> candidateIndices = new HashSet<>();
			
			class Author
			{
				String familyName;
				Levenshtein familyNameLeven;
				String initial;
			}
			
			List<Author> authors = new ArrayList<>();
			
			for (String author : citation.authors)
			{
				String familyName = ArxivAuthorNames.getFamilyName(author);
				if (familyName == null) continue;
				
				ResizableIntArray indices = familyNameToIndex.get(familyName);
				if (indices != null)
				{
					for (int i=0; i<indices.size(); i++)
					{
						candidateIndices.add(indices.get(i));
					}
				}
				
				String familyNameWInitial = ArxivAuthorNames.getFirstInitialAndFamilyName(author);
				
				Author a = new Author();
				a.familyName = familyName;
				a.familyNameLeven = new Levenshtein(familyName);
				a.initial = familyNameWInitial.substring(0, 1);
				authors.add(a);
			}
			if (authors.size() == 0) return;
			
			for (int index : candidateIndices)
			{
				float score = 0f;
				ArxivCitation candidateCitation = metaList.get(index);
				List<Author> candidateAuthors = new ArrayList<>();
				for (String cauthor : candidateCitation.authors)
				{
					String familyNameWI = ArxivAuthorNames.getFirstInitialAndFamilyName(cauthor);
					if (familyNameWI == null) continue;
					Author ca = new Author();
					ca.initial = familyNameWI.substring(0, 1);
					ca.familyName = familyNameWI.substring(2);
					candidateAuthors.add(ca);
				}
				
				for (Author author : authors)
				{
					float bestScore = 0f;
					for (Author cauthor : candidateAuthors)
					{
						float cscore = 0f;
						int maxDistance = Math.min(author.familyName.length(), cauthor.familyName.length()) / 9 + 1;
						
						int distance = author.familyNameLeven.getDistance(cauthor.familyName, maxDistance + 1);
						if (distance <= maxDistance)
						{	
							cscore += 1f - (float) distance / maxDistance;
							cscore += cauthor.initial.equals(author.initial) ? 0.3f : 0f;
						}
						
						bestScore = Math.max(bestScore, cscore);
					}
					
					score += bestScore;
				}
				
				score /= authors.size();
				
				scores[index] += score;
			}
		}
	}
	
	class TitleIndex implements Scorer
	{
		Map<String, ResizableIntArray> threeGramToIndex = new HashMap<>();
		
		public TitleIndex()
		{
			for (int i=0; i<metaList.size(); i++)
			{
				ArxivCitation citation = metaList.get(i);
				if (citation.title.length() < 10) continue;
				
				for (String gram3 : get3Grams(citation.title.toLowerCase()))
				{
					ResizableIntArray indices = threeGramToIndex.get(gram3);
					if (indices == null)
					{
						indices = new ResizableIntArray(1);
						threeGramToIndex.put(gram3, indices);
					}
					indices.add(i);
				}
			}
			System.out.println(threeGramToIndex.size() + " title 3-grams.");
		}
		
		public void computeScore(ArxivCitation citation) 
		{
			Set<Integer> candidateIndices = new HashSet<>();
			String title = citation.title.toLowerCase();
			for (String gram3 : get3Grams(title))
			{
				
				ResizableIntArray indices = threeGramToIndex.get(gram3);
				if (indices != null)
				{
					for (int i=0; i<indices.size(); i++) candidateIndices.add(indices.get(i));
				}
			}
			if (candidateIndices.size() == 0) return;
			
			
			Levenshtein titleLeven = new Levenshtein(title);
			
			for (int index : candidateIndices)
			{
				ArxivCitation candidateCitation = metaList.get(index);
				String candTitle = candidateCitation.title.toLowerCase();
				
				int maxDistance = Math.min(title.length(), title.length()) / 9 + 1;
				
				int distance = titleLeven.getDistance(candTitle, maxDistance + 1);
				if (distance <= maxDistance)
				{	
					scores[index] += 1f - (float) distance / maxDistance;
				}
			}
		}
		
		List<String> get3Grams(String title)
		{
			List<String> grams = new ArrayList<>();
			List<String> parts = TextFileUtil.splitByNonLetterAndDigit(title);
			
			for (int i=0; i<parts.size() - 2; i++)
			{
				String gram = parts.get(i) + " " + parts.get(i + 1) + " " + parts.get(i + 2);
				gram = gram.toLowerCase();
				grams.add(gram);
			}
			
			return grams;
		}
	}
	
	
	
	class JournalIndex implements Scorer
	{
		Map<String, ResizableIntArray> journalNameToIndex = new HashMap<>();
		Map<String, ResizableIntArray> numbers3gramToIndex = new HashMap<>();
		Map<String, ResizableIntArray> numbers4gramToIndex = new HashMap<>();
		
		public JournalIndex()
		{
			// arxiv meta has everything in journal field
			for (int i=0; i<metaList.size(); i++)
			{
				ArxivCitation citation = metaList.get(i);
				if (citation.journal.length() < 5) continue;
				
				JournalInfo info = ArxivJournalParser.parseJournal(citation.journal);
				if (info != null)
				{
					ResizableIntArray indices = journalNameToIndex.get(info.journalName);
					if (indices == null)
					{
						indices = new ResizableIntArray(1);
						journalNameToIndex.put(info.journalName, indices);
					}
					indices.add(i);
					
					String number4gram = info.get4GramNumber();
					if (number4gram != null)
					{
						indices = numbers4gramToIndex.get(number4gram);
						if (indices == null)
						{
							indices = new ResizableIntArray(1);
							numbers4gramToIndex.put(number4gram, indices);
						}
						indices.add(i);
					}
					
					for (String number3gram : info.get3GramNumbers())
					{
						indices = numbers3gramToIndex.get(number3gram);
						if (indices == null)
						{
							indices = new ResizableIntArray(1);
							numbers3gramToIndex.put(number3gram, indices);
						}
						indices.add(i);
					}
				}
			}
			
			System.out.println("journal indices: " + journalNameToIndex.size() + " journals. " + numbers3gramToIndex.size() + " 3-grams. " + numbers4gramToIndex.size() + " 4-grams.");
		}
		
		public void computeScore(ArxivCitation citation) 
		{
			JournalInfo info = getJournalInfo(citation);
//			System.out.println("searched journal " + info.journalName + " " + Arrays.toString(info.numbers));
			List<String> number3grams = info.get3GramNumbers();
			String number4gram = info.get4GramNumber();
			
			Set<Integer> nameMatchIndices = new HashSet<>();
			if (info.journalName != null)
			{
				ResizableIntArray nameMatches = journalNameToIndex.get(info.journalName);
				if (nameMatches != null) for (int i=0; i<nameMatches.size(); i++) nameMatchIndices.add(nameMatches.get(i));
			}
			
			Set<Integer> gram4MatchIndices = new HashSet<>();
			if (number4gram != null)
			{
				ResizableIntArray gram4Matches = numbers4gramToIndex.get(number4gram);
				if (gram4Matches != null)
				{
					for (int i=0; i<gram4Matches.size(); i++)
					{
						int index = gram4Matches.get(i);
						gram4MatchIndices.add(index);
						
						scores[index] += nameMatchIndices.contains(index) ? 2f : 1f;
					}
				}
			}

			for (String gram3 : number3grams)
			{
				ResizableIntArray gram3Matches = numbers3gramToIndex.get(gram3);
				if (gram3Matches != null)
				{
					for (int i=0; i<gram3Matches.size(); i++)
					{
						int index = gram3Matches.get(i);
						if (gram4MatchIndices.contains(index)) continue;
						
						scores[index] += nameMatchIndices.contains(index) ? 0.9f : 0.5f;
					}
				}
			}
		}
		
		public JournalInfo getJournalInfo(ArxivCitation citation)
		{
			JournalInfo info = ArxivJournalParser.parseJournal(citation.journal);
			if (info == null) info = new JournalInfo();
			
			// add volume, issue, pages, year
			Set<Integer> numbers = new HashSet<>();
			if (info.numbers != null) for (int i : info.numbers) numbers.add(i);
			
			if (citation.issue.length() > 0)
			{
				try
				{
					numbers.add(new Integer(citation.issue));
				}
				catch (Exception ex)
				{	
				}
			}
			if (citation.volume.length() > 0)
			{
				try
				{
					numbers.add(new Integer(citation.volume));
				}
				catch (Exception ex)
				{	
				}
			}
			if (citation.pages.length() > 0)
			{
				List<String> parts = TextFileUtil.splitLetterOrDigitSequences(citation.pages);
				for (String part : parts)
				{
					try
					{
						numbers.add(new Integer(part));
					}
					catch (Exception ex)
					{	
					}
				}
			}
			
			if (citation.year > 0 && numbers.size() <= 3)
			{
				numbers.add(citation.year);
			}
			
			info.numbers = new int[numbers.size()];
			List<Integer> numbersList = new ArrayList<>(numbers);
			for (int i=0; i<numbersList.size(); i++)
			{
				info.numbers[i] = numbersList.get(i);
			}
			Arrays.sort(info.numbers, 0, info.numbers.length);
			
			return info;
		}
	}
	
	class YearIndex implements Scorer
	{
		class Year
		{
			int start;
			int endInclusive;
		}
		final int minimumYear = 1985;
		final int maximumYear = Calendar.getInstance().get(Calendar.YEAR);
		Year[] years;
		
		
		public YearIndex()
		{
			years = new Year[maximumYear - minimumYear + 1];
			for (int i=0; i<years.length; i++)
			{
				years[i] = new Year();
				years[i].start = Integer.MAX_VALUE;
			}
			
			// assuming sorted list by year
			for (int i=0; i<metaList.size(); i++)
			{
				int year = metaList.get(i).year;
				if (year < minimumYear || year > maximumYear) continue;
				
				Year y = years[year - minimumYear];
				y.start = Math.min(y.start, i);
				y.endInclusive = Math.max(y.endInclusive, i);
			}
		}
		
		int[] yearOffsets = new int[] { -4, -3, -2, -1, 0, 1, 2 };
		float[] weights = new float[] { 0.1f, 0.2f, 0.4f, 0.8f, 1f, 0.8f, 0.4f};
		
		public void computeScore(ArxivCitation citation) 
		{
			for (int i=0; i<yearOffsets.length; i++)
			{
				int year = citation.year + yearOffsets[i];
				if (year < minimumYear || year > maximumYear) continue;
				Year y = years[year - minimumYear];
				float weight = weights[i];
				
				for (int j=y.start; j<=y.endInclusive; j++)
				{
					scores[j] += weight;
				}
			}
		}
	}
}

class ArxivJournalParser
{
	public static void test()
	{
		d("plos one 6(5), e18975 (2011)");
		d("astrophys.j.694:820-832,2009");
		d("astrophys.j.213452525 694:820-832,2009");
		d("j. math. phys. 50, 023501 (2009)");
		d("journal of geometric analysis: volume 20, issue 2 (2010), page  439.");
		d("phys. med. biol. 54 (2009) n67-n73");
		d("journal of mathematical physics, 50 (1), 012103, 2009.");
		d("phys.rev.d78:114507,2008");
		d("journ. of mod. opt. vol. 54, 2307 (2007)");
		d("phys. rev. a 76, 052319 (2007)");
		d("Nature 426, pp.51-54");
		d("Int.J.Mod.Phys.A28, 1350118, 2013");
		d("Phys. Rev. B 88, 115132 (2013)");
	}
	
	public static void d(String journal)
	{
		JournalInfo j = parseJournal(journal);
		System.out.print(journal + " => ");
		if (j == null) System.out.println("null");
		else System.out.println(j.journalName + " " + Arrays.toString(j.numbers));
	}

	static final Set<String> stopWords;
	static
	{
		String[] stopWords_ = new String[] { "volume", "vol", "issue", "page" };
		stopWords = new HashSet<>();
		for (String stopWord : stopWords_) stopWords.add(stopWord);
	}
	

	
	 
	public static JournalInfo parseJournal(String journal)
	{
		journal = journal.toLowerCase();
		List<String> parts = TextFileUtil.splitLetterOrDigitSequences(journal);
		List<String> partsCleaned = new ArrayList<>();
		for (String part : parts) if (!stopWords.contains(part)) partsCleaned.add(part);
		if (partsCleaned.size() == 0) return null;
		
		List<String> words = new ArrayList<>();
		List<Integer> numbers = new ArrayList<>();
		
		for (String part : partsCleaned)
		{
			Integer number = null;
			try
			{
				number = new Integer(part);
			}
			catch (Exception ex)
			{
				
			}
			
			if (number != null) numbers.add(number);
			else if (numbers.size() == 0) words.add(part);
		}
		
		if (words.size() == 0) return null;
		
		JournalInfo j = new JournalInfo();
		j.journalName = words.get(0);
		for (int i=1; i<words.size(); i++) j.journalName += " " + words.get(i);
		
		int noOfNumbers = Math.min(4, numbers.size());
		j.numbers = new int[noOfNumbers];
		for (int i=0; i<noOfNumbers; i++) j.numbers[i] = numbers.get(numbers.size() - 1 - i);
		Arrays.sort(j.numbers, 0, j.numbers.length);
		
		return j;
	}
}

class JournalInfo
{
	String journalName;
	int[] numbers; // sorted ascending, max. 4 entries
	
	List<String> get3GramNumbers()
	{
		List<String> s = new ArrayList<>();
		if (numbers.length >= 3)
		{
			s.add(getNumberString(numbers, 0, 1, 2));
			
			if (numbers.length >= 4)
			{
				s.add(getNumberString(numbers, 0, 1, 3));
				s.add(getNumberString(numbers, 0, 2, 3));
				s.add(getNumberString(numbers, 1, 2, 3));
			}
		}

		return s;
	}
	
	String get4GramNumber()
	{
		if (numbers.length < 4) return null;
		
		return getNumberString(numbers, 0, 1, 2, 3);
	}
	
	public static String getNumberString(int[] numbers, int... indices)
	{
		String s = "" + numbers[indices[0]];
		for (int i=1; i<indices.length; i++) s += "_" + numbers[indices[i]];
		return s;
	}
}