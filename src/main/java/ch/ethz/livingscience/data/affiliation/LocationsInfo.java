package ch.ethz.livingscience.data.affiliation;

import java.io.Serializable;
import java.util.List;

public class LocationsInfo implements Serializable
{
	private static final long serialVersionUID = 1730824360977560784L;
	
	public int[] domainToInstitutionsIndices;
	public List<Institution> institutions;
	public List<City> cities;
	public List<Country> countries;
	
	public LocationsInfo()
	{
		
	}
}
