package ch.ethz.livingscience.arxiv;

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
}