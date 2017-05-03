package ch.ethz.livingscience;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import nu.xom.Attribute;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Nodes;
import nu.xom.XPathContext;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Page 
{
	public static final String ns = "http://www.w3.org/1999/xhtml";
	public static XPathContext ctx = new XPathContext("html", ns);
	public static final String xhtmlContent = "text/html;charset=utf-8";
	public static final String textContent = "text/plain;charset=utf-8";
	public static final String jsonContent = "application/json;charset=utf-8";
	
	// html template
	protected Document doc;
	protected Element content;
	protected Element sidebar;
	protected Element dataParsing;
	
	public Page()
	{
		
	}
	
	public Page(Document doc) throws IOException
	{
		this.doc = doc;
		Nodes nodes = doc.query("//*/html:span[@id='content'] ", ctx);
		content = (Element) nodes.get(0);
		
		nodes = doc.query("//*/html:div[@id='leftsidebar'] ", ctx);
		sidebar = (Element) nodes.get(0);
		

		nodes = doc.query("//*/html:script[@id='parsingDocumentData'] ", ctx);
		dataParsing = (Element) nodes.get(0);
		
	}
	
	public void addJSONData(JSONArray o, String varName) throws IOException
	{
		try 
		{
			addJSONData(o.toString(2), varName);
		} 
		catch (JSONException e) 
		{
			throw new IOException(e.getCause());
		}
	}
	
	public void addJSONData(JSONObject o, String varName) throws IOException
	{
		try 
		{
			addJSONData(o.toString(2), varName);
		} 
		catch (JSONException e) 
		{
			throw new IOException(e.getCause());
		}
	}
	
	public void addJSONData(String json, String varName) throws IOException
	{
		Element head = (Element) doc.query("/html:html/html:head", ctx).get(0);
		Element script = new Element("script", ns);
		script.addAttribute(new Attribute("type", "application/json"));
		script.addAttribute(new Attribute("id", varName));
		head.insertChild(script, head.indexOf(dataParsing)); // want to have the data script before the dataParsing script
		script.appendChild(json);
		dataParsing.appendChild("var " + varName + " = JSON.parse(document.getElementById('" + varName + "').innerHTML);");
	}
	
	public void exec() throws IOException
	{
		
	}
	
	public void writeResponse(HttpServletResponse resp) throws IOException
	{
		resp.setContentType(xhtmlContent);
		resp.setStatus(HttpServletResponse.SC_OK);
		resp.getWriter().write(doc.toXML());
	}
}
