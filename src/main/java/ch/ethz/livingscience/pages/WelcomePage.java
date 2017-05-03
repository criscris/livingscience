package ch.ethz.livingscience.pages;

import java.io.IOException;

import nu.xom.Attribute;
import nu.xom.Comment;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Nodes;
import ch.ethz.livingscience.Page;
import ch.ethz.livingscience.data.ProfilesSearchIndex;

public class WelcomePage extends Page
{
	ProfilesSearchIndex searchIndex;
	
	public WelcomePage(Document doc, ProfilesSearchIndex searchIndex) throws IOException
	{
		super(doc);
		this.searchIndex = searchIndex;
	}

	public void exec() 
	{
		Nodes nodes = doc.query("//*/html:span[@id='welcomecontent'] ", ctx);
		if (nodes.size() == 0) return;
		Element welcome = (Element) nodes.get(0);
		welcome.addAttribute(new Attribute("style", "visibility:visible"));
		
		nodes = welcome.query("//*/html:span[@id='noOfProfilesInDatabase'] ", ctx);
		if (nodes.size() != 0)
		{
			Element noOfProfiles = (Element) nodes.get(0);
			noOfProfiles.removeChildren();
			noOfProfiles.appendChild(" " + (searchIndex.getNoOfAutomaticProfiles() + searchIndex.getNoOfManualProfiles()) + " ");	
		}

		nodes = welcome.query("//*/html:div[@id='introVideo'] ", ctx);
		if (nodes.size() != 0)
		{
			Element introVideo = (Element) nodes.get(0);
			Element iframe = new Element("iframe", ns);
			introVideo.appendChild(iframe);
			iframe.addAttribute(new Attribute("src", "//player.vimeo.com/video/77449655?title=0&amp;byline=0&amp;portrait=0"));
			iframe.addAttribute(new Attribute("frameborder", "0"));
			iframe.addAttribute(new Attribute("width", "720"));
			iframe.addAttribute(new Attribute("height", "405"));
			iframe.addAttribute(new Attribute("webkitallowfullscreen", "true"));
			iframe.addAttribute(new Attribute("mozallowfullscreen", "true"));
			iframe.addAttribute(new Attribute("allowfullscreen", "true"));
			iframe.appendChild(new Comment(" _ "));	
		}
	}
}
