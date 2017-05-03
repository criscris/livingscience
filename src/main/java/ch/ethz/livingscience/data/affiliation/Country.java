package ch.ethz.livingscience.data.affiliation;

import java.io.Serializable;

public class Country implements Serializable
{
	private static final long serialVersionUID = -8747964552603932761L;
	
	public String name;
	
	public Country(String name)
	{
		this.name = name;
	}
	
	public Country()
	{

	}
}
