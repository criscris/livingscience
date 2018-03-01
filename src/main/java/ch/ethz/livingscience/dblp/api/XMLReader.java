package ch.ethz.livingscience.dblp.api;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/*
 * Note: with special characters the names of authors/titles appear splitted in different contents
 */
public class XMLReader {
	
   static int count;	
   static int countBooks;
   static String entryType;
   //among "book, article, inproceedings, proceedings, incollection, phdthesis, mastersthesis, www"
   //at the moment you set manually the entryCase to one in the list above
   static String entryCase = "www"; 

   public static void main(String argv[]) throws IOException {
	   
	final BufferedWriter writer = new BufferedWriter(new FileWriter(new File("C:/Users/almud/Documents/LivingScience/files/dblp/dump/sorted/" + entryCase +"2.txt")));   
	count = 0;   

    try {

	SAXParserFactory factory = SAXParserFactory.newInstance();
	SAXParser saxParser = factory.newSAXParser();

	DefaultHandler handler = new DefaultHandler() {
		
	boolean entry = false;

	public void startElement(String uri, String localName,String qName,
                Attributes attributes) throws SAXException {

		if(!entry){
			if(!qName.equalsIgnoreCase("dblp")){
				entryType = qName;
				entry = true;
				switch (qName.toLowerCase()){
				case "book":
//					count++;
//					try {
//						writer.write("__qName:" + entryCase);
//						for(int i=0; i<attributes.getLength(); i++){
//							writer.write(" " + attributes.getQName(i) + "=\"" + attributes.getValue(i) + "\"");
//						}
//						writer.write(" ");
//					} catch (IOException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}
					break;
				case "article":
//					count++;
//					try {
//						writer.write("__qName:" + entryCase);
//						for(int i=0; i<attributes.getLength(); i++){
//							writer.write(" " + attributes.getQName(i) + "=\"" + attributes.getValue(i) + "\"");
//						}
//						writer.write(" ");
//					} catch (IOException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}
					break;
//					break;
				case "inproceedings":
//					count++;
//					try {
//						writer.write("__qName:" + entryCase);
//						for(int i=0; i<attributes.getLength(); i++){
//							writer.write(" " + attributes.getQName(i) + "=\"" + attributes.getValue(i) + "\"");
//						}
//						writer.write(" ");
//					} catch (IOException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}
					break;
				case "proceedings":
//					count++;
//					try {
//						writer.write("__qName:" + entryCase);
//						for(int i=0; i<attributes.getLength(); i++){
//							writer.write(" " + attributes.getQName(i) + "=\"" + attributes.getValue(i) + "\"");
//						}
//						writer.write(" ");
//					} catch (IOException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}
					break;
				case "incollection":
//					count++;
//					try {
//						writer.write("__qName:" + entryCase);
//						for(int i=0; i<attributes.getLength(); i++){
//							writer.write(" " + attributes.getQName(i) + "=\"" + attributes.getValue(i) + "\"");
//						}
//						writer.write(" ");
//					} catch (IOException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}
					break;
				case "phdthesis":
//					count++;
//					try {
//						writer.write("__qName:" + entryCase);
//						for(int i=0; i<attributes.getLength(); i++){
//							writer.write(" " + attributes.getQName(i) + "=\"" + attributes.getValue(i) + "\"");
//						}
//						writer.write(" ");
//					} catch (IOException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}
					break;
				case "mastersthesis":
//					count++;
//					try {
//						writer.write("__qName:" + entryCase);
//						for(int i=0; i<attributes.getLength(); i++){
//							writer.write(" " + attributes.getQName(i) + "=\"" + attributes.getValue(i) + "\"");
//						}
//						writer.write(" ");
//					} catch (IOException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}
					break;
				case "www":
					count++;
					try {
						writer.write("__qName:" + entryCase);
						for(int i=0; i<attributes.getLength(); i++){
							writer.write(" " + attributes.getQName(i) + "=\"" + attributes.getValue(i) + "\"");
						}
						writer.write(" ");
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					break;
				default:
					System.out.println("tag: " + qName);
					break;
				}
			}
		}
		else{
			if(entryType.equalsIgnoreCase(entryCase)){
				try {
					writer.write("__qName:" + qName);
//					writer.write("<" + qName);
					for(int i=0; i<attributes.getLength(); i++){
						writer.write(" " + attributes.getQName(i) + "=\"" + attributes.getValue(i) + "\"");
					}
//					writer.write(">");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

//		}
	}

	public void endElement(String uri, String localName,
		String qName) throws SAXException {

		if(entryType.equalsIgnoreCase(entryCase)){
			try {
//				writer.write("</" + qName + ">");
				if(qName.equalsIgnoreCase(entryCase)){
					writer.write("\n");
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}		
		if(entryType.equalsIgnoreCase(qName)){	
			entry = false;
		}	

	}

	public void characters(char ch[], int start, int length) throws SAXException {
		
		if(entryType.equalsIgnoreCase(entryCase)){
			try {
				writer.write("__qValue:");
				writer.write(new String(ch, start, length));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

     };

       saxParser.parse("C:/Users/almud/Documents/LivingScience/files/dblp/dump/dblp.xml", handler);
       writer.close();
       System.out.println("written: " + count + " " + entryCase);

     } catch (Exception e) {
       e.printStackTrace();
     }

   }

}