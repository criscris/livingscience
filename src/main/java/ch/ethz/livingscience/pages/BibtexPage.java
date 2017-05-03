package ch.ethz.livingscience.pages;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import ch.ethz.livingscience.Page;
import ch.ethz.livingscience.data.Profile;
import ch.ethz.livingscience.data.ProfilesDB;
import ch.ethz.livingscience.data.Publication;
import utils.text.CountableSet;
import utils.text.TextFileUtil;

/**
 * @Article{unqiueid,
	 author    = "Milton {Abramowitz} and Irene A. {Stegun}",
	 title     = "Handbook of Mathematical Functions with Formulas, Graphs, and Mathematical Tables",
	 journal   = "Whatever",
	 year      =  1964
	}
 * @author cschulz
 *
 */
public class BibtexPage extends Page
{
	ProfilesDB db;
	String profileID;
	
	StringBuilder result;
	
	public BibtexPage(ProfilesDB db, String profileID)
	{
		this.db = db;
		this.profileID = profileID;
	}
	
	public void exec() throws IOException
	{
		result = new StringBuilder();
		
		Profile profile = db.getProfile(profileID);
		if (profile == null)
		{
			return;
		}
		
		List<Publication> pubs = new ArrayList<>();
		for (String pubID : profile.pubIDs)
		{
			Publication pub = db.getPub(pubID);
			if (pub != null) pubs.add(pub);
		}
		
		CountableSet<String> articleIDs = new CountableSet<>();
		
		for (int i=0; i<pubs.size(); i++)
		{
			Publication pub = pubs.get(i);
			result.append("@Article{");
			
			String id = "";
			for (int j=0; j<pub.authors.size(); j++)
			{
				
				List<String> parts = TextFileUtil.splitByNonLetterAndDigit(pub.authors.get(j));
				String lastName = parts.get(parts.size() - 1);
				
				id += lastName.toLowerCase();
				if (j < pub.authors.size() - 1) id += "_";
			}
			id += pub.year;
			articleIDs.add(id);
			int count = articleIDs.counts.get(id);
			if (count > 1) id += "_" + count;
			result.append(id);
			
			result.append(",\n");
			
			String authors = "";
			for (int j=0; j<pub.authors.size(); j++)
			{
				String author = pub.authors.get(j);
				List<String> parts = TextFileUtil.split(author, " ");
				
				for (int k=0; k<parts.size() - 1; k++)
				{
					authors += parts.get(k) + " ";
				}
				authors += "{" + parts.get(parts.size() - 1) + "}";
				
				if (j < pub.authors.size() - 1) authors += " and "; 
			}
			
			result.append(" author = \"" + authors.replace("\"", "") + "\",\n");
			
			if (pub.title == null) pub.title = "";
			result.append(" title = \"" + pub.title.replace("\"", "") + "\",\n");
			
			if (pub.journal == null) pub.journal = "";
			result.append(" journal = \"" + pub.journal.replace("\"", "") + "\",\n");
			
			result.append(" year = " + pub.year + "\n");
			result.append("}\n\n");
		}
	}
	
	public void writeResponse(HttpServletResponse resp) throws IOException
	{
		resp.setContentType(textContent);
		resp.setStatus(HttpServletResponse.SC_OK);
		resp.getWriter().write(result.toString());
	}
}
