package ch.ethz.livingscience;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

public class wikiTextCleaner
{
	@SuppressWarnings("resource")
	public static void main(String[] args) throws IOException
	{
		BufferedReader br = new BufferedReader(new FileReader("C:/Users/user/Desktop/Wikiwords/wikiWords.txt"));
		Writer writer = new BufferedWriter(new FileWriter("C:/Users/user/Desktop/Wikiwords/testOutput.txt"));
		try {			  
	          String line;	          
	          while ((line = br.readLine()) != null) {
	        	 	        	
	        	 String[] parts = line.split(":");
	        	 String wikiWord = parts[2];
	        	 //System.out.println(wikiWord);
	        	 writer.write(wikiWord);
	        	 writer.write("\n");
	        	 
	          }
	      } catch(IOException e) {
	          System.out.println(e);
	      } finally {
	    	  if (writer != null) try { writer.close(); } catch (IOException ignore) {}
		}
	}
}