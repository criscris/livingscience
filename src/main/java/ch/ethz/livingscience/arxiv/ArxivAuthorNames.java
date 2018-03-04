package ch.ethz.livingscience.arxiv;

import java.util.ArrayList;
import java.util.List;

import utils.text.TextFileUtil;

/**
 * Dirk Helbing
 * D. Helbing
 * D. E. Helbing
 * D. E. J. Helbing
 * Dirk E. Helbing
 * D. Emil Helbing
 * B. de Wit
 * KY Lee
 * Fabian H. L. Essler
 * E. Y. Loh, Jr
 * A R Prasanna
 * Tellez 
 *
 */
public class ArxivAuthorNames
{
	public static void test()
	{
		d("Dirk Helbing");
		d("D. Helbing");
		d("D. E. Helbing");
		d("D. E. J. Helbing");
		d("Dirk E. Helbing");
		d("D. Emil Helbing");
		d("B. de Wit");
		d("KY Lee");
		d("Fabian H. L. Essler");
		d("E. Y. Loh, Jr");
		d("A R Prasanna");
		d("Tellez"); 
	}
	
	
	public static void d(String author)
	{
		System.out.println(author + " => " + getFirstInitialAndFamilyName(author));
	}
	
	
	/**
	 * @return e.g. stanley
	 */
	public static String getFamilyName(String author)
	{
		String name = getFirstInitialAndFamilyName(author);
		if (name == null) return null;
		int i1 = name.indexOf(" ");
		return name.substring(i1 + 1);
	}
	
	public static List<String> getFamilyNames(String author)
	{
		String name = getFamilyName(author);
		List<String> familyNames = new ArrayList<>();
		String punctuations = "-";
		if (name == null) return null;
		if (name.contains(punctuations))
		{
			List<String> parts = TextFileUtil.split(name, punctuations);
			if (parts.size()>1)
			{
				for(String part: parts)
				{
					familyNames.add(part);
				}
			}
			return familyNames;
		}
		else
		{
			int i1 = name.indexOf(" ");
			familyNames.add(name.substring(i1 + 1));
			return familyNames;
		}
	}
	
	/**
	 * author: firstname lastname (in that order)
	 * 
	 * @return e.g. h stanley
	 */
	public static String getFirstInitialAndFamilyName(String author)
	{
		author = author.toLowerCase();
		List<String> parts = TextFileUtil.split(author, " ");
		if (parts.size() < 2) return null;
		
		int last = parts.size() - 1;
		
		return parts.get(0).substring(0, 1) + " " + parts.get(last);
	}
	
	/**
	 * author: firstname 
	 * 
	 * @return e.g. harry
	 */
	public static String getFirstName(String author)
	{
		author = author.toLowerCase();
		List<String> parts = TextFileUtil.split(author, " ");
		if (parts.size() < 2) return null;
						
		return parts.get(0);
	}
	
	public static List<String> getAllAssociatedNames(String author)
	{
		author = author.toLowerCase();
		List<String> parts = TextFileUtil.split(author, " ");
		List<String> associatedNames = new ArrayList<>();
		if (parts.size() < 2) return null;
		
		//returns first name
		associatedNames.add(getFirstName(author));
		
		//returns family names
		if (getFamilyNames(author).size()>1)
		{
			for(String name: getFamilyNames(author))
			{
				associatedNames.add(name);
			}			
		}
		else
		{
			associatedNames.addAll(getFamilyNames(author));
		}
		
		//returns initial and last name
		associatedNames.add(getFirstInitialAndFamilyName(author));
		
		//returns full name
		associatedNames.add(author);
		
		//returns middle name(more than 3 characters long)
		author = author.toLowerCase();
		List<String> middlePart = TextFileUtil.split(author, " ");
		if (middlePart.size() < 2) return null;
		if(middlePart.get(1).length() > 3)
		{
			associatedNames.add(middlePart.get(1));
		}
				
		return associatedNames;
		
	}

	// function to calculate Edit distance (Levenshtein distance) (need to be experimented in near future)
	public static int distance(String s1, String s2)
	{
	     int edits[][]=new int[s1.length()+1][s2.length()+1];
	     for(int i=0;i<=s1.length();i++)
	         edits[i][0]=i;
	     for(int j=1;j<=s2.length();j++)
	         edits[0][j]=j;
	     for(int i=1;i<=s1.length();i++){
	         for(int j=1;j<=s2.length();j++){
	             int u=(s1.charAt(i-1)==s2.charAt(j-1)?0:1);
	             edits[i][j]=Math.min(
	                             edits[i-1][j]+1,
	                             Math.min(
	                                edits[i][j-1]+1,
	                                edits[i-1][j-1]+u
	                             )
	                         );
	         }
	     }
	     return edits[s1.length()][s2.length()];
	}
	 
}