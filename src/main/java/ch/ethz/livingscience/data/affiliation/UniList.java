package ch.ethz.livingscience.data.affiliation;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Elements;

public class UniList
{
	public Map<String, Institution> domainToInstMap = new HashMap<String, Institution>();
	
	public UniList() throws Exception
	{
		long time = System.currentTimeMillis();
	    Builder bob = new Builder();
		Document doc = bob.build(UniList.class.getResourceAsStream("unilist.xml"));
		Element root = doc.getRootElement(); // livingscience
		
		Elements countries = root.getChildElements();
		int count = 0;
		for (int i=0; i<countries.size(); i++)
		{
			Element countryElem = countries.get(i);
			Country country = new Country(countryElem.getAttributeValue("name"));
			
			Elements cities = countryElem.getChildElements();
			for (int j=0; j<cities.size(); j++)
			{
				Element cityElem = cities.get(j);
				City city = new City(cityElem.getAttributeValue("name"), new Coords(new Double(cityElem.getAttributeValue("lon")), new Double(cityElem.getAttributeValue("lat"))), country);
				
				Elements unis = cityElem.getChildElements();
				for (int k=0; k<unis.size(); k++)
				{
					Element uniElem = unis.get(k);
					Institution inst = new Institution(uniElem.getAttributeValue("label"), uniElem.getAttributeValue("domain"), city);
					
					domainToInstMap.put(inst.domain, inst);
					
					String altDomain = uniElem.getAttributeValue("altDomain");
					if (altDomain != null) domainToInstMap.put(altDomain, inst);
					
					count++;
				}
			}
		}
		
		System.out.println(count + " universities loaded in " + (System.currentTimeMillis() - time) + " ms.");
		
		init2();
	}
	
	public Institution getInstitution(URL url)
	{
		String u = url.toString();
		for (Institution inst : domainToInstMap.values())
		{
			if (u.contains("/" + inst.domain) || u.contains("." + inst.domain))
			{
				return inst;
			}
		}
		return null;
	}
	
	public Institution getInstitution(String emailDomain)
	{
		int i1 = emailDomain.indexOf(".");
		if (i1 == -1) return null;
		
		Institution inst = domainToInstMap.get(emailDomain);
		if (inst != null) return inst;
		
		return getInstitution(emailDomain.substring(i1 + 1));
	}
	
	class Composite
	{
		Institution inst;
		Map<String, Composite> map;
		
		public Composite(Institution inst)
		{
			this.inst = inst;
		}
		
		public Composite(Map<String, Composite> map)
		{
			this.map = map;
		}
	}
	
	Map<String, Composite> domainpartToNextDomainOrInstMap = new HashMap<String, Composite>();
	private void init2() throws Exception
	{
		List<Entry<String, Institution>> entryList = new ArrayList<Entry<String,Institution>>(domainToInstMap.entrySet());
		for (Entry<String, Institution> entry : entryList)
		{
			String domain = entry.getKey();
			String[] subs = domain.split("\\.");
			if (subs.length <= 1) throw new Exception("invalid domain: " + domain);
			
			Map<String, Composite> currentLevelMap = domainpartToNextDomainOrInstMap;
			for (int i=subs.length-1; i>0; i--)
			{
				Composite comp = currentLevelMap.get(subs[i]);
				if (comp == null)
				{
					comp = new Composite(new HashMap<String, Composite>());
					currentLevelMap.put(subs[i], comp);
				}
				if (comp.inst != null)
				{
					throw new Exception("Domain " + domain + " contained in some other at level " + subs[i] + ".");
				}
				currentLevelMap = comp.map;
			}
			if (currentLevelMap.put(subs[0], new Composite(entry.getValue())) != null) // last hierarchy
				throw new Exception("Double domain mapping: " + domain);
		}
		
//		debugMapHierarchy(domainpartToNextDomainOrInstMap, 0);
	}
	
	public void debugMapHierarchy(Map<String, Composite> hierarchy, int level)
	{
		List<Entry<String, Composite>> entryList = new ArrayList<Entry<String,Composite>>(hierarchy.entrySet());
		
		List<String> institutes = new ArrayList<String>();
		for (int i=0; i<entryList.size(); i++)
		{
			for (int k=0; k<level; k++) System.out.print("\t");
			System.out.println(entryList.get(i).getKey());
			
			if (entryList.get(i).getValue().inst != null)
			{
				institutes.add(entryList.get(i).getKey());
				continue;
			}
			
			debugMapHierarchy(entryList.get(i).getValue().map, level + 1);
		}
	}
	
	public LocationsInfo getLocationInfoExactMatch(String[] domains)
	{
		LocationsInfo info = new LocationsInfo();
		info.domainToInstitutionsIndices = new int[domains.length];
		info.institutions = new ArrayList<Institution>();
		Set<City> cities = new HashSet<City>();
		Set<Country> countries = new HashSet<Country>();
		
		for (int i=0; i<domains.length; i++)
		{
			if (domains[i] == null)
			{
				info.domainToInstitutionsIndices[i] = -1;
				continue;
			}
			Institution uni = domainToInstMap.get(domains[i]);
			
			if (uni == null)
			{
				info.domainToInstitutionsIndices[i] = -1;
				continue;
			}
			
			int index = info.institutions.indexOf(uni);
			if (index == -1)
			{
				index = info.institutions.size();
				info.institutions.add(uni);
			}
			info.domainToInstitutionsIndices[i] = index;
			
			if (cities.add(uni.city)) countries.add(uni.city.country);
			
		}
		
		info.cities = new ArrayList<City>(cities);
		info.countries = new ArrayList<Country>(countries);
		
		return info;
	}
	
	public LocationsInfo getLocationInfo(List<String> domains)
	{
		LocationsInfo info = new LocationsInfo();
		info.domainToInstitutionsIndices = new int[domains.size()];
		info.institutions = new ArrayList<Institution>();
		Set<City> cities = new HashSet<City>();
		Set<Country> countries = new HashSet<Country>();
		
		for (int i=0; i<domains.size(); i++)
		{
			if (domains.get(i) == null)
			{
				info.domainToInstitutionsIndices[i] = -1;
				continue;
			}
			
			String[] subs = domains.get(i).split("\\.");
			Institution uni = null;
			Map<String, Composite> currentLevelMap = domainpartToNextDomainOrInstMap;
			for (int j=subs.length-1; j>=0; j--)
			{
				Composite comp = currentLevelMap.get(subs[j]);
				if (comp == null) break; // no match
				
				if (comp.inst != null)
				{
					uni = comp.inst;
					break;
				}
				
				currentLevelMap = comp.map;
			}
			
			if (uni == null)
			{
				info.domainToInstitutionsIndices[i] = -1;
				continue;
			}
			
			int index = info.institutions.indexOf(uni);
			if (index == -1)
			{
				index = info.institutions.size();
				info.institutions.add(uni);
			}
			info.domainToInstitutionsIndices[i] = index;
			
			if (cities.add(uni.city)) countries.add(uni.city.country);
			
		}
		
		info.cities = new ArrayList<City>(cities);
		info.countries = new ArrayList<Country>(countries);
		
		return info;
	}
}
