package ch.ethz.livingscience.data.affiliation;

import java.io.Serializable;

public class Institution implements Serializable
{
	private static final long serialVersionUID = 7302499046619006958L;
	
	public String name;
	public String domain;
	public City city;
	//public Coords coords;  optional, city roughly provides them; would make sense for higher zoom steps / satellite maps
	
	public Institution()
	{
		
	}

	public Institution(String name, String domain, City city)
	{
		this.name = name;
		this.domain = domain;
		this.city = city;
	}
}
