package utils.text;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RandomAccessTextFile 
{
//	Map<String, Long> idToPosition;
	
	String[] ids;
	long[] positions;
	
	
	RandomAccessFile raf;
	
	public RandomAccessTextFile(File sourceTextFile, File indexFile) throws Exception
	{
		this(sourceTextFile);
		
		int ngrams = TextFileUtil.countLines(indexFile);
		ids = new String[ngrams];
		positions = new long[ngrams];
		
		BufferedReader reader = new BufferedReader(new FileReader(indexFile));
		String line = null;
		int index = 0;
		while ((line = reader.readLine()) != null)
		{
			int i1 = line.indexOf(";");
			ids[index] = line.substring(0, i1);
			positions[index] = new Long(line.substring(i1 + 1));
			index++;
		}
		reader.close();
		
//		idToPosition = TextFileUtil.loadStringToLongMap(indexFile);//
	}
	
	private RandomAccessTextFile(File sourceTextFile) throws FileNotFoundException
	{
		raf = new RandomAccessFile(sourceTextFile, "r");
	}
	
	public static RandomAccessTextFile createRandomAccessTextFile(File sourceTextFile, RandomAccessTextFileLineListener listener, File indexFileOut) throws Exception
	{
		RandomAccessTextFile r = new RandomAccessTextFile(sourceTextFile);
//		Map<String, Long> idToPosition = new HashMap<String, Long>();
		List<String> lines = new ArrayList<>();
		
		long position = r.raf.getFilePointer();
		String line = null;
		while ((line = r.raf.readLine()) != null) // quite slow compared to BufferedReader?
		{
			String id = listener.createIdentifier(line);
			if (id != null) 
				//r.idToPosition.put(id, position);
				lines.add(id + ";" + position);
			position = r.raf.getFilePointer();
		}
		
//		List<Entry<String, Long>> entries = new ArrayList<>(r.idToPosition.entrySet());
//		Collections.sort(entries, new Comparator<Entry<String, Long>>()
//		{
//			public int compare(Entry<String, Long> o1, Entry<String, Long> o2) 
//			{
//				return Long.compare(o1.getValue(), o2.getValue());
//			}
//		});
//		TextFileUtil.writeMap(entries, indexFileOut);
		TextFileUtil.writeList(lines, indexFileOut);
		
		return r;
	}
	
	public String getLine(String id) throws IOException
	{
//		Long position = idToPosition.get(id);
		
	//	long time = System.currentTimeMillis();
		int index = Arrays.binarySearch(ids, id);
		if (index < 0) return null;
		
		long position = positions[index];
	//	long time1 = System.currentTimeMillis();
		
		raf.seek(position);
		String line =  raf.readLine();
	//	long time2 = System.currentTimeMillis();
		
		
	//	System.out.println((time1 - time) + " ms for searching. " + (time2 - time1) + " ms for reading.");
		
		return line;
	}
	
	public int size()
	{
		return ids.length;
	}
}
