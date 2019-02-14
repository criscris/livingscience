package ch.ethz.livingscience.pages;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import nu.xom.Attribute;
import nu.xom.Document;
import nu.xom.Element;
import ch.ethz.livingscience.Page;
import ch.ethz.livingscience.data.Profile;
import ch.ethz.livingscience.data.ProfilesDB;
import ch.ethz.livingscience.data.ProfilesSearchIndex;

public class SearchProfilesPage extends Page
{
	ProfilesDB db;
	ProfilesSearchIndex searchIndex;
	String query;
	
	public SearchProfilesPage(Document doc, ProfilesDB db, ProfilesSearchIndex searchIndex, String query) throws IOException
	{
		super(doc);
		this.db = db;
		this.searchIndex = searchIndex;
		this.query = query;
	}
	
	public void exec()
	{
		Map<String, Float> profileScores = searchIndex.queryForProfiles(query);
		
		if(profileScores.size()!=0) {
			List<Profile> profiles = new ArrayList<>();
			for (Entry<String, Float> entry : profileScores.entrySet())
			{
				Profile profile = db.getProfile(entry.getKey());
				if (profile != null) 
				{
					profile.score = entry.getValue();
					profiles.add(profile);
				}
			}
			Collections.sort(profiles, new Comparator<Profile>() 
			{
				public int compare(Profile o1, Profile o2) 
				{
					if (o1.score == o2.score)
					{
						return o2.pubIDs.size() - o1.pubIDs.size();
					}
					return (int) ((o2.score - o1.score)*1000f);
				}
			});
			
			String result = profiles.size() + " " + (profiles.size() == 1 ? "profile" : "profiles") + " found for the query: " + query + ".";
			Element resultDiv = new Element("div", ns);
			resultDiv.appendChild(result);
			resultDiv.addAttribute(new Attribute("class", "mainHeading"));
			content.appendChild(resultDiv);
			
			for (Profile profile : profiles)
			{
				Element profileDiv = new Element("div", ns);
				profileDiv.addAttribute(new Attribute("class", "pubContainer"));
				
				Element a = new Element("a", ns);
				a.addAttribute(new Attribute("class", "pubLink"));
				a.addAttribute(new Attribute("href", "/profiles/" + profile.id));
				a.appendChild(profile.name);
				profileDiv.appendChild(a);
				
				if (profile.affiliation != null && profile.affiliation.length() > 0)
				{
					Element info = new Element("div", ns);
					info.addAttribute(new Attribute("class", "pubMeta"));
					info.appendChild(profile.affiliation);
					profileDiv.appendChild(info);
				}
				
				Element info = new Element("div", ns);
				info.addAttribute(new Attribute("class", "pubMeta"));
				info.appendChild(profile.pubIDs.size() + " publications.");
				profileDiv.appendChild(info);
				
				content.appendChild(profileDiv);
			}
		}
		else {
			String result = "No author names could be found matching the query.";
			Element resultDiv = new Element("div", ns);
			resultDiv.appendChild(result);
			resultDiv.addAttribute(new Attribute("class", "mainHeading"));
			content.appendChild(resultDiv);
			
		}
		
		
	}
}
