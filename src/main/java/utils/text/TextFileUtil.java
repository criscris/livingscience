package utils.text;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;


public class TextFileUtil
{
	public static List<String> loadList(File file) throws Exception
	{
		List<String> lines = new ArrayList<String>();
		
		BufferedReader reader = new BufferedReader(new FileReader(file));
		String line = null;
		while ((line = reader.readLine()) != null)
		{
			line = line.trim();
			if (line.length() > 0 
		//			&& !line.startsWith("#")
				) 
				lines.add(line);
		}
		reader.close();
		
		return lines;
	}
	
	public static void copyText_UTF8(InputStream is, OutputStream os) throws Exception
	{
		BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));

		String line = null;
		while ((line = reader.readLine()) != null)
		{
			writer.write(line);
			writer.write("\n");
		}
		
		writer.close();
		reader.close();
	}
	
	/**
	 * @return number of different text lines
	 */
	public static long compareTextLines(InputStream is1, InputStream is2) throws Exception
	{
		final BufferedReader reader = new BufferedReader(new InputStreamReader(is2));
		final long[] countDiff = new long[1];
		
		loadList(is1, new LineListener() 
		{
			public void newLine(int index, String line)
			{
				String line2 = null;
				try
				{
					line2 = reader.readLine();
				}
				catch (Exception ex)
				{
					
				}
				if (line2 == null || !line2.equals(line)) countDiff[0]++;
				
			}
		});
		
		// in case is2 is longer
		while (reader.readLine() != null)
		{
			countDiff[0]++;
		}
		
		reader.close();
		return countDiff[0];
	}
	
	public static List<String> loadList_noComments(File file) throws Exception
	{
		List<String> lines = new ArrayList<String>();
		
		BufferedReader reader = new BufferedReader(new FileReader(file));
		String line = null;
		while ((line = reader.readLine()) != null)
		{
			line = line.trim();
			if (line.length() > 0 
					&& !line.startsWith("#")
				) 
				lines.add(line);
		}
		reader.close();
		
		return lines;
	}
	
	public static Stream<List<String>> stream(InputStream is, final String separator, boolean ignoreFirstLine) throws Exception
	{
		final BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
		if (ignoreFirstLine) reader.readLine();
		
		class LineIter implements Iterator<List<String>>
		{
			String line = null;
			boolean closed = false;

			public boolean hasNext() 
			{
				if (line != null) return true;
				if (closed) return false;
				
				try 
				{
					line = reader.readLine();
				} 
				catch (IOException e) 
				{

				}
				if (line == null) 
				{
					closed = true;
					try 
					{
						reader.close();
					} 
					catch (IOException e) 
					{

					}
				}
				return line != null;
			}

			public List<String> next() 
			{
				if (!hasNext()) return null;
				List<String> parts = split(line, separator);
				line = null;
				return parts;
			}
		}
		LineIter iter = new LineIter();
		
		return StreamSupport.stream(Spliterators.spliteratorUnknownSize(iter, Spliterator.ORDERED), false);
	}
	
	public static void loadList(File file, LineListener listener) throws Exception
	{
		BufferedReader reader = new BufferedReader(new FileReader(file));
		String line = null;
		int index = 0;
		while ((line = reader.readLine()) != null)
		{
			listener.newLine(index, line);
			index++;
		}
		reader.close();
	}
	
	public static void loadList(File file, LineListener2 listener) throws Exception
	{
		BufferedRandomAccessFile raf = new BufferedRandomAccessFile(file, "r", 32768);
		
		String line = null;
		long currentPosition = raf.getFilePointer();
		int index = 0;
		while ((line = raf.getNextLine()) != null)
		{
			listener.newLine(index, line, currentPosition);
			index++;
			currentPosition = raf.getFilePointer();
		}
		raf.close();
	}
	
	public static void loadList(InputStream is, LineListener listener) throws Exception
	{
		BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
		String line = null;
		int index = 0;
		while ((line = reader.readLine()) != null)
		{
			listener.newLine(index, line);
			index++;
		}
		reader.close();
	}
	
	public static void loadList(File file, String colSeparator, CsvListener listener) throws Exception
	{
		loadList(new FileInputStream(file), colSeparator, listener);
	}
	
	public static void loadList(InputStream is, String colSeparator, CsvListener listener) throws Exception
	{
		BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
		String line = null;
		int index = 0;
		while ((line = reader.readLine()) != null)
		{
			listener.newLine(index, TextFileUtil.split(line, colSeparator));
			index++;
		}
		reader.close();
	}
	
	public static void loadList(InputStream is, LineListener listener, int maxNoOfLines) throws Exception
	{
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		String line = null;
		int index = 0;
		while ((line = reader.readLine()) != null && index < maxNoOfLines)
		{
			listener.newLine(index, line);
			index++;
		}
		reader.close();
	}
	
	public static int countLines(File file) throws Exception
	{
		BufferedReader reader = new BufferedReader(new FileReader(file));
		int count = 0;
		while (reader.readLine() != null)
		{
			count++;
		}
		reader.close();
		return count;
	}
	
	public static List<String> loadList(InputStream is) throws Exception
	{
		List<String> lines = new ArrayList<String>();
		
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		String line = null;
		while ((line = reader.readLine()) != null)
		{
			line = line.trim();
			if (line.length() > 0 && !line.startsWith("#")) lines.add(line);
		}
		reader.close();
		
		return lines;
	}
	
	public static Map<String, Integer> loadStringToIntegerMap(InputStream is) throws Exception
	{
		Map<String, Integer> map = new HashMap<>();
		
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		String line = null;
		while ((line = reader.readLine()) != null)
		{
			int i1 = line.indexOf(";");
			String key = line.substring(0, i1);
			int value = new Integer(line.substring(i1 + 1));
			
			map.put(key, value);
		}
		reader.close();
		
		
		return map;
	}
	
	public static Map<String, Integer> loadStringToIntegerMap(File file) throws Exception
	{
		return loadStringToIntegerMap(new FileInputStream(file));
	}
	
	public static Map<String, Long> loadStringToLongMap(InputStream is) throws Exception
	{
		Map<String, Long> map = new HashMap<>();
		
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		String line = null;
		while ((line = reader.readLine()) != null)
		{
			int i1 = line.indexOf(";");
			String key = line.substring(0, i1);
			long value = new Long(line.substring(i1 + 1));
			
			map.put(key, value);
			if (map.size() % 100000 == 0) System.out.println(map.size());
		}
		reader.close();
		
		return map;
	}
	
	public static Map<String, Long> loadStringToLongMap(File file) throws Exception
	{
		return loadStringToLongMap(new FileInputStream(file));
	}
	
	public static void writeMap(List<Entry<String, Long>> entries, File file) throws Exception
	{
		BufferedWriter writer = new BufferedWriter(new FileWriter(file));
		for (Entry<String, Long> entry : entries)
		{
			writer.write(entry.getKey() + ";" + entry.getValue());
			writer.write("\n");
		}
		writer.close();
	}
	
	public static void writeMapI(List<Entry<String, Integer>> entries, File file) throws Exception
	{
		BufferedWriter writer = new BufferedWriter(new FileWriter(file));
		for (Entry<String, Integer> entry : entries)
		{
			writer.write(entry.getKey() + ";" + entry.getValue());
			writer.write("\n");
		}
		writer.close();
	}
	
	public static void writeStringToStringMap(Map<String, String> map, File file) throws Exception
	{
		BufferedWriter writer = new BufferedWriter(new FileWriter(file));
		for (Entry<String, String> entry : map.entrySet())
		{
			writer.write(entry.getKey() + ";" + entry.getValue());
			writer.write("\n");
		}
		writer.close();
	}
	
	public static Map<String, String> loadStringToStringMap(InputStream is) throws Exception
	{
		Map<String, String> map = new HashMap<>();
		
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		String line = null;
		while ((line = reader.readLine()) != null)
		{
			int i1 = line.indexOf(";");
			String key = line.substring(0, i1);
			String value = line.substring(i1 + 1);
			
			map.put(key, value);
		}
		reader.close();
		
		return map;
	}
	
	public static Map<String, String> loadStringToStringMap(File file) throws Exception
	{
		return loadStringToStringMap(new FileInputStream(file));
	}

	public static String load(File file) throws Exception
	{
		return load(new FileInputStream(file));
	}
	
	public static String load(InputStream is) throws IOException
	{
		StringBuilder sb = new StringBuilder();
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		String line = null;
		while ((line = reader.readLine()) != null)
		{
			sb.append(line);
			sb.append("\n");
		}
		reader.close();
		return sb.toString();
	}
	
	/**
	 * ignores commas inside " "
	 * 
	 * may not work as expected ... 
	 */
	public static List<String> splitByComma(String line) 
	{
		List<String> parts = new ArrayList<String>();
		line = line.trim();
		if (line.length() == 0) return parts;
		
		String[] split = line.split(",");
		
		String currentPart = null;
		for (int i=0; i<split.length; i++)
		{
			if (currentPart == null)
			{
				if (split[i].startsWith("\""))
				{
					if (split[i].endsWith("\""))
					{
						parts.add(split[i].substring(1, split[i].length() - 1));
					}
					else
					{
						currentPart = split[i].substring(1);
					}
					
					
				}
				else
				{
					// just a normal part
					parts.add(split[i]);
				}
			}
			else
			{
				if (split[i].endsWith("\""))
				{
					currentPart += "," + split[i].substring(0, split[i].length() - 1);
					parts.add(currentPart);
					currentPart = null;
				}
				else
				{
					currentPart += "," + split[i];
				}
			}
		}
		
		return parts;
	}
	
	public static List<String> split(String line, String separator) 
	{
		List<String> parts = new ArrayList<String>();
		line = line.trim();
		if (line.length() == 0) return parts;
		
		int offset = 0;
		int i1 = -1;
		while ((i1 = line.indexOf(separator, offset)) >= 0)
		{
			parts.add(line.substring(offset, i1));
			offset = i1 + separator.length();
		}
		if (offset < line.length()) parts.add(line.substring(offset));
		
		return parts;
	}
	
	/**
	 * @param maxParts max number of parts. last part is the rest of string 
	 */
	public static List<String> splitMax(String line, String separator, int maxParts) 
	{
		List<String> parts = new ArrayList<String>();
		line = line.trim();
		if (line.length() == 0) return parts;
		
		int offset = 0;
		int i1 = -1;
		while ((i1 = line.indexOf(separator, offset)) >= 0)
		{
			parts.add(line.substring(offset, i1));
			offset = i1 + separator.length();
			if (parts.size() >= maxParts - 1) break;
		}
		
		if (offset < line.length()) parts.add(line.substring(offset));
		
		return parts;
	}
	
	public static List<String> splitByWhitespace(String line) 
	{
		List<String> parts = new ArrayList<String>();
		line = line.trim();
		if (line.length() == 0) return parts;
		
		
		while (line.length() > 0)
		{
			int offset = 0;
			while (offset < line.length() && !Character.isWhitespace(line.charAt(offset)))
			{
				offset++;
			}
			
			parts.add(line.substring(0, offset));
			if (offset >= line.length()) break;
			line = line.substring(offset);
			line = line.trim();
			
		}
		return parts;
	}
	
	public static List<String> splitByWhitespaceAndFullstop(String line) 
	{
		List<String> parts = new ArrayList<String>();
		line = line.trim();
		if (line.length() == 0) return parts;
		
		
		while (line.length() > 0)
		{
			int offset = 0;
			while (offset < line.length() && !Character.isWhitespace(line.charAt(offset)) && line.charAt(offset) != '.')
			{
				offset++;
			}
			
			parts.add(line.substring(0, offset));
			if (offset + 1 >= line.length()) break;
			line = line.substring(offset + 1);
			line = line.trim();
			
		}
		return parts;
	}
	
	/**
	 * non-letters and digits are separating characters
	 */
	public static List<String> splitByNonLetterAndDigit(String line) 
	{
		List<String> parts = new ArrayList<String>();
		line = line.trim();
		if (line.length() == 0) return parts;
		
		while (line.length() > 0)
		{
			int offset = 0;
			while (offset < line.length() && Character.isLetter(line.charAt(offset)))
			{
				offset++;
			}
			
			String part = line.substring(0, offset);
			if (part.length() > 0) 
			{
				parts.add(part);
			}
			
			if (offset >= line.length() - 1) break;
			line = line.substring(offset + 1);
			
		}
		return parts;
	}
	
	
	/**
	 * include letters or digits, everything else is splitting.
	 * @param line
	 * @return
	 */
	public static List<String> splitLetterOrDigitSequences(String line) 
	{
		List<String> parts = new ArrayList<String>();
		line = line.trim();
		if (line.length() == 0) return parts;
		
		char[] lineChars = line.toCharArray();
		
		// 1...letter sequence, 2...digit sequence, 0...nothing
		int[] type = new int[lineChars.length];
		for (int c=0; c<lineChars.length; c++)
		{
			type[c] = Character.isLetter(lineChars[c]) ? 1 : (Character.isDigit(lineChars[c]) ? 2 : 0); 
		}
		
		int start = 0;
		for (int c=1; c<lineChars.length; c++)
		{
			int previous = type[c-1];
			int current = type[c];
			
			if (current != previous)
			{
				if (previous != 0)
				{
					parts.add(line.substring(start, c));
				}
				start = c;
			}
		}
		if (type[type.length - 1] != 0) parts.add(line.substring(start));
		
		return parts;
	}
	
	public static String reduceInBetweenWhitespace(String text)
	{
		List<String> parts = splitByWhitespace(text);
		
		StringBuilder sb = new StringBuilder();
		
		for (int i=0; i<parts.size(); i++)
		{
			sb.append(parts.get(i));
			if (i < parts.size() - 1) sb.append(" ");
		}
		return sb.toString();
	}
	
	public static void writeList(List<String> lines, OutputStream os) throws Exception
	{
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
		for (String line : lines)
		{
			writer.write(line);
			writer.write("\n");
		}
		writer.close();
	}
	
	public static void writeList(List<String> lines, File file) throws Exception
	{
		writeList(lines, file, false);
	}
	
	public static void merge(File targetFile, File... sourceFiles) throws Exception
	{
		BufferedWriter writer = new BufferedWriter(new FileWriter(targetFile));
		
		for (File sourceFile : sourceFiles)
		{
			BufferedReader reader = new BufferedReader(new FileReader(sourceFile));
			String line = null;
			while ((line = reader.readLine()) != null)
			{
				writer.write(line);
				writer.write("\n");
			}
			reader.close();
		}
		
		writer.close();
	}
	
	public static void writeList(List<String> lines, File file, boolean append) throws Exception
	{
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, append), "UTF-8"));
		for (String line : lines)
		{
			writer.write(line);
			writer.write("\n");
		}
		writer.close();
	}
	
	public static void writeObjectList(String headerLine, List<? extends Object> lines, File file) throws Exception
	{
		BufferedWriter writer = new BufferedWriter(new FileWriter(file));
		writer.write(headerLine);
		writer.write("\n");
		for (Object line : lines)
		{
			writer.write(line.toString());
			writer.write("\n");
		}
		writer.close();
	}
	
	public static void writeObjectList(List<? extends Object> lines, File file) throws Exception
	{
		writeObjectList(lines, file, false);
	}
	
	public static void writeObjectList(List<? extends Object> lines, File file, boolean append) throws Exception
	{
		BufferedWriter writer = new BufferedWriter(new FileWriter(file, append));
		for (Object line : lines)
		{
			writer.write(line.toString());
			writer.write("\n");
		}
		writer.close();
	}
	
	public static void writeEntries(List<Entry<String, Integer>> entries, File file, String separator) throws Exception
	{
		BufferedWriter writer = new BufferedWriter(new FileWriter(file));
		for (Entry<String, Integer> entry : entries)
		{
			writer.write(entry.getKey() + separator + entry.getValue());
			writer.write("\n");
		}
		writer.close();
	}
	
	public static void writeText(String text, File file) throws IOException
	{
		writeText(text, file, false);
	}
	
	public static void writeText(String text, File file, boolean append) throws IOException
	{
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, append), "UTF-8"));
		writer.write(text);
		writer.close();
	}
	
	public static int charCount(String text, char s)
	{
		int count = 0;
		for (int i=0; i<text.length(); i++)
		{
			if (s == text.charAt(i)) count++;
		}
		return count;
	}
	
	public static int[] intArrayFromString(String text, int fromIndex, int count, String sep)
	{
		String[] parts = text.split(sep);
		int[] data = new int[count];
		for (int i=0; i<data.length; i++)
		{
			data[i] = new Integer(parts[i+fromIndex]);
		}
		return data;
	}
	
	public static float[] floatArrayFromString(String text, int fromIndex, int count, String sep)
	{
		String[] parts = text.split(sep);
		float[] data = new float[count];
		for (int i=0; i<data.length; i++)
		{
			data[i] = new Float(parts[i+fromIndex]);
		}
		return data;
	}
	
	public static int countChars(String text, char charToBeCounted)
	{
		int count = 0;
		for (int i=0; i<text.length(); i++)
		{
			char c = text.charAt(i);
			if (c == charToBeCounted) count++;
		}
		return count;
	}
	
	public static String numberArrayToString(float[] data)
	{
		StringBuilder sb = new StringBuilder();
		for (int i=0; i<data.length; i++)
		{
			float val = data[i];
			sb.append(val == 0f ? "0" : val);
			if (i < data.length - 1) sb.append(" ");
		}
		return sb.toString();
	}
	
	public static int[] stringToNumberArray(String line)
	{
		List<String> parts = split(line, " ");
		int[] data = new int[parts.size()];
		for (int i=0; i<parts.size(); i++)
		{
			data[i] = new Integer(parts.get(i));
		}
		return data;
	}
	
	public static String booleanArrayTo10String(boolean[] data)
	{
		StringBuilder sb = new StringBuilder();
		for (int i=0; i<data.length; i++)
		{
			sb.append(data[i] ? "1" : "0");
			if (i < data.length - 1) sb.append(" ");
		}
		return sb.toString();
	}
	
	public static String numberArrayToString(int[] data)
	{
		StringBuilder sb = new StringBuilder();
		for (int i=0; i<data.length; i++)
		{
			sb.append(data[i]);
			if (i < data.length - 1) sb.append(" ");
		}
		return sb.toString();
	}
	
	public static String numberArrayToString(List<Integer> data)
	{
		StringBuilder sb = new StringBuilder();
		for (int i=0; i<data.size(); i++)
		{
			sb.append(data.get(i));
			if (i < data.size() - 1) sb.append(" ");
		}
		return sb.toString();
	}
	
	public static String listToString(List<String> entries, String sep)
	{
		String s = "";
		for (String entry : entries)
		{
			s += sep + entry;
		}
		return s;
	}
	
	public static String listToStringSepOnlyInBetween(List<String> entries, String sep)
	{
		if (entries.size() == 0) return "";
		StringBuilder sb = new StringBuilder();
		sb.append(entries.get(0));
		for (int i=1; i<entries.size(); i++)
		{
			sb.append(sep);
			sb.append(entries.get(i));
		}
		return sb.toString();
	}
	
	public static String listToStringSepOnlyInBetweenO(List<? extends Object> entries, String sep)
	{
		if (entries.size() == 0) return "";
		String s = entries.get(0).toString();
		for (int i=1; i<entries.size(); i++)
		{
			s += sep + entries.get(i);
		}
		return s;
	}
	
	public static String listToStringSepOnlyInBetweenI(List<Integer> entries, String sep)
	{
		if (entries.size() == 0) return "";
		String s = "" + entries.get(0);
		for (int i=1; i<entries.size(); i++)
		{
			s += sep + entries.get(i);
		}
		return s;
	}
	
	public static void main(String[] args) throws Exception
	{
		for (String part : split("AD,\"Agriculture, Dairy & Animal Science\",0.0053212764,0.0015155402,3.5111418", ","))
		{
			System.out.println(part);
		}
		System.out.println("--");
		for (String part : "AD,\"Agriculture, Dairy & Animal Science\",0.0053212764,0.0015155402,3.5111418".split(","))
		{
			System.out.println(part);
		}
		
		System.out.println("--");
		for (String part : splitByComma("AD,\"Agriculture, Dairy & Animal Science\",0.0053212764,0.0015155402,3.5111418"))
		{
			System.out.println(part);
		}
		
		
		
		
//		System.out.println(Character.isLetter("-".charAt(0)));
		
//		for (String s : splitByWhitespaceAndFullstop("hallo.gruerzie miteinand"))
//		{
//			System.out.println(s);
//		}
	}
	
	/**
	 * for HTML pages with a lot of whitespace
	 */
	public static String removeLineWiseWhiteSpace(String text) throws Exception
	{
		StringWriter s = new StringWriter();
		BufferedWriter writer = new BufferedWriter(s);
		for (String line : TextFileUtil.loadList(new ByteArrayInputStream(text.getBytes())))
		{
			writer.write(line.trim());
			writer.write("\n");
		}
		writer.close();
		return s.toString();
	}
	
	/**
	 * @return array of indices where textToFind starts in text
	 */
	public static int[] find(String text, String textToFind)
	{
		List<Integer> indices = new ArrayList<>();
		
		int i0 = 0;
		while ((i0 = text.indexOf(textToFind, i0)) != -1)
		{
			indices.add(i0);
			i0 += textToFind.length();
		}
		
		int[] array = new int[indices.size()];
		for (int i=0; i<array.length; i++) array[i] = indices.get(i);
		return array;
	}
}
