package ch.ethz.livingscience.pages;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import nu.xom.Document;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import ch.ethz.livingscience.data.ProfilesDB;
import ch.ethz.livingscience.data.Publication;
import ch.ethz.livingscience.data.affiliation.City;
import ch.ethz.livingscience.data.affiliation.Institution;
import ch.ethz.livingscience.data.affiliation.LocationsInfo;
import ch.ethz.livingscience.data.affiliation.UniList;

public class AffiliationsPage extends ProfilePubListPage
{
	UniList uniList;
	
	public AffiliationsPage(Document doc, ProfilesDB db, String profileID, UniList uniList) throws IOException
	{
		super(doc, db, profileID);
		this.uniList = uniList;
	}
	
	public void exec() throws IOException
	{
		loadPubs();
		
		List<String> domains = new ArrayList<>();
		List<Integer> domainToPubIndex = new ArrayList<>();
		
		for (int i=0; i<pubs.size(); i++)
		{
			Publication pub = pubs.get(i);
			
			if (pub.affiliations != null)
			{
				for (String domain : pub.affiliations)
				{
					domains.add(domain);
					domainToPubIndex.add(i);
				}
			}
		}
		System.out.println(domains.size() + " domains.");
		
		
		LocationsInfo locationsInfo = uniList.getLocationInfo(domains);
		
		List<Set<String>> pubsInThisCity = new ArrayList<>();
		for (int i=0; i<locationsInfo.cities.size(); i++)
		{
			locationsInfo.cities.get(i).index = i;
			pubsInThisCity.add(new HashSet<String>());
		}
		for (int i=0; i<locationsInfo.domainToInstitutionsIndices.length; i++)
		{
			int instIndex = locationsInfo.domainToInstitutionsIndices[i];
			if (instIndex < 0) continue;
			
			Publication pub = pubs.get(domainToPubIndex.get(i));
			pubsInThisCity.get(locationsInfo.institutions.get(instIndex).city.index).add(pub.id);
		}
		
		List<List<String>> citiesToInstitutions = new ArrayList<>();
		for (int i=0; i<locationsInfo.cities.size(); i++) citiesToInstitutions.add(new ArrayList<String>());
		for (Institution inst : locationsInfo.institutions)
		{
			citiesToInstitutions.get(inst.city.index).add(inst.name);
		}
		
		JSONArray result = new JSONArray();
		
		try 
		{
			for (int i=0; i<locationsInfo.cities.size(); i++)
			{
				City city = locationsInfo.cities.get(i);
				
				JSONObject place = new JSONObject();
				place.put("city", city.name);
				place.put("country", city.country.name);
				
				place.put("institutions", citiesToInstitutions.get(i));
				
				place.put("lat", city.coords.lat);
				place.put("lon", city.coords.lon);
				
				place.put("pubs", pubsInThisCity.get(i));
				
				result.put(place);
			}
		}
		catch (JSONException e) 
		{
			throw new IOException(e.getCause());
		}
		
		addJSONData(result, "mapData");
	}
}
