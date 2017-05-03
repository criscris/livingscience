package ch.ethz.livingscience.data.affiliation;

import java.io.Serializable;

public class City implements Serializable
{
	private static final long serialVersionUID = 6263259813212199458L;
	
	public int index;
	public String name;
	public Coords coords;
	public Country country;
	
	public City()
	{
		
	}

	public City(String name, Coords coords, Country country)
	{
		this.name = name;
		this.coords = coords;
		this.country = country;
	}
}
