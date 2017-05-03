package ch.ethz.livingscience.data;

public class TextOutputPrep
{
	// XML 1.0
	// #x9 | #xA | #xD | [#x20-#xD7FF] | [#xE000-#xFFFD] | [#x10000-#x10FFFF]
	static final String xml10pattern = "[^"
				                    + "\u0009\r\n"
				                    + "\u0020-\uD7FF"
				                    + "\uE000-\uFFFD"
				                    + "\ud800\udc00-\udbff\udfff"
				                    + "]";
	
	public static String prepare(String text)
	{
//		text = StringEscapeUtils.escapeXml(text);
//		text = StringEscapeUtils.escapeHtml4(text);
		
		return text.replaceAll(xml10pattern, "");
	}
}
