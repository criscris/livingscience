package ch.ethz.livingscience.datacreation;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.mongodb.BasicDBObject;

import ch.ethz.livingscience.arxiv.ArxivCitation;
import ch.ethz.livingscience.arxiv.api.ArxivXMLAnalyzer;
import ch.ethz.livingscience.data.Profile;
import ch.ethz.livingscience.data.ProfilesDB;
import ch.ethz.livingscience.data.Publication;
import ch.ethz.livingscience.data.affiliation.Institution;
import ch.ethz.livingscience.data.affiliation.UniList;
import utils.text.CountableSet;
import utils.text.LineListener;
import utils.text.TextFileUtil;

public class ProfilesDBCreator implements LineListener
{
	public static void main(String[] args) throws Exception
	{
		ProfilesDBCreator c = new ProfilesDBCreator();
		c.exec();
	}
	
	ProfilesDB db;
	Map<String, List<String>> arxivIDtoEmailDomains;
	UniList uniList;
	
	Map<String, List<Profile>> arxivIDtoProfile = new HashMap<>();
	List<Profile> profiles = new ArrayList<>();
	
	public ProfilesDBCreator() throws Exception
	{
		db = new ProfilesDB(27013);
		arxivIDtoEmailDomains = createArxivIDtoEmailDomains();
		uniList = new UniList();
		
		for (String line : TextFileUtil.loadList(new File("/Users/cschulz/Documents/data/arxiv/disambiguation/out/clusters.txt")))
		{
			List<String> parts = TextFileUtil.split(line, ";");
			
			Profile p = new Profile();
			p.name = parts.get(0);
			
			int i1 = p.name.indexOf(", ");
			p.name = p.name.substring(p.name.length() - 1) + " " + p.name.substring(0, i1);
			
			p.pubIDs = new ArrayList<>();
			profiles.add(p);
			
			for (int i=1; i<parts.size(); i++)
			{
				String arxivID = parts.get(i);
				
				List<Profile> profiles = arxivIDtoProfile.get(arxivID);
				if (profiles == null)
				{
					profiles = new ArrayList<>();
					arxivIDtoProfile.put(arxivID, profiles);
				}
				profiles.add(p);
			}
		}
		System.out.println(arxivIDtoProfile.size() + " pub are associated to a profile.");
	}
	
	static final String[] emailDomainFilters = new String[] { "gmail", "googlemail", "outlook", "hotmail", "yahoo" };
	static Map<String, List<String>> createArxivIDtoEmailDomains() throws Exception
	{
		Map<String, List<String>> arxivIDtoEmailDomains = new HashMap<>();
		
		for (String line : TextFileUtil.loadList(new File("/Users/cschulz/Documents/data/arxiv/arxivemails.txt")))
		{
			List<String> parts = TextFileUtil.split(line, ";");
			
			String arxivID = parts.get(0);
			
			List<String> domains = new ArrayList<>();
			for (int i=1; i<parts.size(); i++)
			{
				String email = parts.get(i);
				int i1 = email.indexOf("@");
				
				String domain = email.substring(i1 + 1).toLowerCase();
				
				boolean use = true;
				for (String filter : emailDomainFilters)
				{
					if (domain.contains(filter))
					{
						use = false;
						break;
					}
				}
				if (!use) continue;
				
				domains.add(domain);
			}
			
			if (domains.size() > 0) arxivIDtoEmailDomains.put(arxivID, domains);
		}
		
		System.out.println(arxivIDtoEmailDomains.size() + " pubs with emails.");
		return arxivIDtoEmailDomains;
	}
	
	public void exec() throws Exception
	{
		long time = System.currentTimeMillis();
		TextFileUtil.loadList(new File("/Users/cschulz/Documents/data/arxiv/arxivmeta_summaries.txt"), this);
		System.out.println("Pubs loaded in " + (System.currentTimeMillis() - time) + " ms.");
		
		// now, add all profiles
		int debugAffiliationCount = 0;
		for (int i=0; i<profiles.size(); i++)
		{
			Profile profile = profiles.get(i);
			
			boolean useClustername = true;
			int i0 = profile.name.lastIndexOf(" ");
			if (i0 != -1)
			{
				String lastName = profile.name.substring(i0 + 1).toLowerCase();
				
				List<Publication> pubs = new ArrayList<>();
				for (String pubID : profile.pubIDs)
				{
					pubs.add(db.getPub(pubID));
				}
				
				String fullName = guessFullName(lastName, pubs);
				if (fullName != null)
				{
					profile.name = fullName;
					useClustername = false;
				}
				
				profile.affiliation = guessAffiliation(pubs);
				if (profile.affiliation != null) debugAffiliationCount++;
			}
			
			if (useClustername)
			{
				// make name uppercase
				profile.name = profile.name.substring(0, 1).toUpperCase() + profile.name.substring(1);
				int i1 = profile.name.indexOf(" ");
				if (i1 > 0 && i1 < profile.name.length() - 2)
				{
					profile.name = profile.name.substring(0, i1 + 1) + profile.name.substring(i1 + 1, i1 + 2).toUpperCase() + profile.name.substring(i1 + 2);
				}
			}
			
			
			BasicDBObject doc = new BasicDBObject("name", profile.name).append("pubs", profile.pubIDs);
			if (profile.affiliation != null) doc.append("affiliation", profile.affiliation);
			db.collProfilesAuto.insert(doc);
			
			if ((i+1) % 2000 == 0) System.out.println("profile" + (i+1));
		}
		
		System.out.println(debugAffiliationCount + " of " + profiles.size() + " have an affiliation.");
		System.out.println((System.currentTimeMillis() - time) + " ms.");
	}
	
	int pubCount = 0;
	public void newLine(int index, String line) 
	{
		ArxivCitation citation = new ArxivCitation(line);
		
		String url = "http://arxiv.org/abs/" + ArxivXMLAnalyzer.addSlashToIDifOldID(citation.arxivID);
		
		BasicDBObject doc = new BasicDBObject();
		if (citation.authors != null && citation.authors.size() > 0) doc.append("authors", citation.authors);
		if (citation.journal != null && citation.journal.length() > 0) doc.append("journal", citation.journal);
		if (citation.title != null && citation.title.length() > 0) doc.append("title", citation.title);
		if (citation.year != 0) doc.append("year", citation.year);
		if (citation.summary != null && citation.summary.length() > 0) doc.append("summary", citation.summary);
		
		List<String> affiliations = arxivIDtoEmailDomains.get(citation.arxivID);
		if (affiliations != null && affiliations.size() > 0) doc.append("affiliations", affiliations);
		
		doc.append("url", url);
		
		if (citation.doi != null && citation.doi.length() > 0) doc.append("doi", citation.doi);
		
		db.collPubs.insert(doc);
		String id = doc.get("_id").toString();

		// arxiv id without slash
		List<Profile> profiles = arxivIDtoProfile.get(citation.arxivID);
		if (profiles != null)
		{
			for (Profile profile : profiles)
			{
				profile.pubIDs.add(id);
			}
		}
		
		pubCount++;
		if (pubCount % 2000 == 0) System.out.println("pub" + pubCount);
	}
	
	static String guessFullName(String lastname, List<Publication> pubs)
	{
		CountableSet<String> names = new CountableSet<>();
		
		for (Publication pub : pubs)
		{
			for (String author : pub.authors)
			{
				if (author.toLowerCase().endsWith(lastname))
				{
					if (author.length() < 3) continue;
					if (author.charAt(1) == ' ' || author.charAt(1) == '.' || author.charAt(2) == ' ') continue;
					
					names.add(author);
				}
			}
		}
		
		List<Entry<String, Integer>> entries = names.sortedEntriesDecending();
		if (entries.size() == 0) return null;
		
		return entries.get(0).getKey();
	}
	
	String guessAffiliation(List<Publication> pubs)
	{
		CountableSet<String> emailDomains = new CountableSet<>();
		
		for (Publication pub : pubs)
		{
			for (String affil : pub.affiliations)
			{
				emailDomains.add(affil);
			}
		}
		
		List<Entry<String, Integer>> entries = emailDomains.sortedEntriesDecending();
		if (entries.size() == 0) return null;
		
		for (int i=0; i<Math.min(entries.size(), 3); i++)
		{
			String emailDomain = entries.get(0).getKey();
			Institution inst = uniList.getInstitution(emailDomain);
			if (inst != null)
			{
				return inst.name;
			}
		}
		
		return null;
	}
}
