package ch.ethz.livingscience.data.affiliation;

import java.io.Serializable;

public class Coords implements Serializable
{
	private static final long serialVersionUID = 5073968636108767428L;
	
	public double lat; // y in degs
	public double lon; // x in degs;
	
	public Coords()
	{
		
	}
	
	public Coords(double lon, double lat)
	{
		this.lon = lon;
		this.lat = lat;
	}
}
