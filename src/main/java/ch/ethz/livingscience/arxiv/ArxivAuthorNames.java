package ch.ethz.livingscience.arxiv;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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

	// function to calculate Hamming distance (need to be experimented in near future)
	public static int hammingDist(String query, String author)
	{
	    int i = 0, count = 0;
	    while (i < query.length())
	    {
	        if (query.charAt(i) != author.charAt(i))
	            count++;
	        i++;
	    }
	    return count;
	} 
}