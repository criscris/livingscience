package utils.text;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class CountableSet<T> 
{
	public Map<T, Integer> counts = new HashMap<T, Integer>();
	private int totalCount = 0;
	
	public class AscendingComparator implements Comparator<Entry<T, Integer>>
	{
		public int compare(Entry<T, Integer> o1, Entry<T, Integer> o2) 
		{
			return o1.getValue() - o2.getValue();
		}
	}
	public AscendingComparator ascendingComparator = new AscendingComparator();
	
	public class DescendingComparator implements Comparator<Entry<T, Integer>>
	{
		public int compare(Entry<T, Integer> o1, Entry<T, Integer> o2) 
		{
			return o2.getValue() - o1.getValue();
		}
	}
	public DescendingComparator descendingComparator = new DescendingComparator();
	
	

	
	public void add(T object)
	{
		Integer count = counts.get(object);
		if (count == null)
		{
			count = 0;
		}
		counts.put(object, count + 1);
		totalCount++;
	}
	
	public void addAll(List<T> objects)
	{
		for (T object : objects) add(object);
	}
	
	public List<Entry<T, Integer>> sortedEntriesDecending()
	{
		List<Entry<T, Integer>> entries = new ArrayList<Map.Entry<T,Integer>>(counts.entrySet());
		Collections.sort(entries, descendingComparator);
		return entries;
	}
	
	public List<Entry<T, Integer>> sortedEntriesAscending()
	{
		List<Entry<T, Integer>> entries = new ArrayList<Map.Entry<T,Integer>>(counts.entrySet());
		Collections.sort(entries, ascendingComparator);
		return entries;
	} 
	
	
	public int totalCount()
	{
		return totalCount;
	}
	
	public void add(T object, int value)
	{
		Integer count = counts.get(object);
		if (count == null)
		{
			count = 0;
		}
		if (count + value == 0) counts.remove(object);
		else counts.put(object, count + value);
		totalCount += value;
	}
	
	public void add(CountableSet<T> other)
	{
		for (Entry<T, Integer> entry : other.counts.entrySet())
		{
			add(entry.getKey(), entry.getValue());
		}
	}
	
	public List<T> getUniqueElements()
	{
		return new ArrayList<T>(counts.keySet());
	}
	
	public void debugSorted() throws Exception
	{
		List<Entry<T, Integer>> entries = new ArrayList<Map.Entry<T,Integer>>(counts.entrySet());
		Collections.sort(entries, new Comparator<Entry<T, Integer>>()
		{
			public int compare(Entry<T, Integer> o1, Entry<T, Integer> o2)
			{
				return o1.getValue() - o2.getValue();
			}
		});
		
		int count = 0;
		for (Entry<T, Integer> entry : entries)
		{
			int c = entry.getValue();
			count += c;
//			if (c > 1) 
				System.out.println(entry.getKey().toString() + ": " + c);
		}
		System.out.println("total count: " + count);
	}

	public String toString() 
	{
		StringBuilder sb = new StringBuilder();
		
		for (Entry<T, Integer> entry : counts.entrySet())
		{
			sb.append(entry.getKey() + ": " + entry.getValue());
			sb.append("\n");
		}
		
		return sb.toString();
	}
}
