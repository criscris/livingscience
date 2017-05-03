package ch.ethz.livingscience.pages;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import nu.xom.Document;

import org.json.JSONException;

import ch.ethz.livingscience.data.ProfilesDB;

public class JsonPage extends ProfilePubListPage
{
	ProfilesDB db;
	String profileID;
	
	public JsonPage(Document doc, ProfilesDB db, String profileID) throws IOException
	{
		super(doc, db, profileID);
	}
	
	public void exec() throws IOException
	{
		loadPubs();
	}
	
	public void writeResponse(HttpServletResponse resp) throws IOException
	{
		resp.setContentType(jsonContent);
		resp.setStatus(HttpServletResponse.SC_OK);
		try 
		{
			resp.getWriter().write(json.toString(2));
		} 
		catch (JSONException e) 
		{
			throw new IOException(e.getCause());
		}
	}
}
