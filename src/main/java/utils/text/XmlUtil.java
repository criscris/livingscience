package utils.text;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Element;
import org.w3c.dom.Node;


public class XmlUtil
{
	
	public static Element getChild(Element element, String tagName)
	{
		Node currentChild = element.getFirstChild();
		
		while (currentChild != null)
		{
			if (tagName.equals(currentChild.getNodeName())) return (Element) currentChild;
			currentChild = currentChild.getNextSibling();
		}
		return null;
	}
	
	public static Element getChild(Element element, String tagName, String attrib, String attribValue)
	{
		Node currentChild = element.getFirstChild();
		
		while (currentChild != null)
		{
			if (tagName.equals(currentChild.getNodeName()))
			{
				Element elem = (Element) currentChild;
				if (attribValue.equals(elem.getAttribute(attrib))) return (Element) currentChild;
			}
			currentChild = currentChild.getNextSibling();
		}
		return null;
	}
	
	public static List<Element> getChildren(Element element, String tagName)
	{
		List<Element> children = new ArrayList<Element>();
		
		Node currentChild = element.getFirstChild();
		
		while (currentChild != null)
		{
			if (tagName.equals(currentChild.getNodeName())) children.add((Element) currentChild);
			currentChild = currentChild.getNextSibling();
		}
		
		return children;
	}
	
	public static List<Element> getAllSubElements(Element element, String attrib, String attribValue)
	{
		List<Element> foundElements = new ArrayList<Element>();
		addSubElements(foundElements, element, attrib, attribValue);
		return foundElements;
	}
	
	private static void addSubElements(List<Element> foundElements, Element element, String attrib, String attribValue)
	{
		Node currentChild = element.getFirstChild();
		if (currentChild == null) return;
		
		while (currentChild != null)
		{
			if (currentChild instanceof Element)
			{
				Element elem = (Element) currentChild;
				if (attribValue.equals(elem.getAttribute(attrib))) foundElements.add((Element) currentChild);
				addSubElements(foundElements, elem, attrib, attribValue);
			}
			
			currentChild = currentChild.getNextSibling();
		}
	}
	
	public static String removeWhiteSpaceInbetween(String text)
	{
		String[] words = StringUtils.split(text);
		StringBuilder sb = new StringBuilder();
		for (int i=0; i<words.length; i++)
		{
			String newWord = StringUtils.trimToNull(words[i]);
			if (newWord != null)
			{
				sb.append(newWord);
				if (i< words.length-1) sb.append(" ");
			}
		}
		return sb.toString().trim();
	}
}
