package ch.ethz.livingscience;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilePermission;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.compress.utils.Charsets;

import static java.nio.file.StandardOpenOption.*;
import static java.util.stream.Collectors.joining;

public class RemoveDuplicateWords {

@SuppressWarnings("resource")
public static void main(String[] args) throws IOException {        
    Path sourcePath = Paths.get("C:/Users/user/Desktop/Wikiwords/testOutput.txt");
    Path changedPath = Paths.get("C:/Users/user/Desktop/Wikiwords/testOutput.txt");
      
    BufferedReader br = new BufferedReader(new FileReader("C:/Users/user/Desktop/Wikiwords/wikiScienceTopics.txt"));
    BufferedReader br2 = new BufferedReader(new FileReader("C:/Users/user/Desktop/Wikiwords/stop_words.txt"));
	Writer writer = new BufferedWriter(new FileWriter("C:/Users/user/Desktop/Wikiwords/testOut.txt"));

//Remove the stop words	
    try {			  
        String line, stop_line;
        List<String> lineList2 = new ArrayList<String>();
        
        while ((line = br.readLine()) != null){
       		lineList2.add(line);
       		System.out.println("First: "+ line);
        }
        br.close();
        Set<String> set = new HashSet<String>();
        Iterator<String> i = lineList2.iterator();
        while (i.hasNext()) {
            String s = i.next();
            if (set.contains(s)) {
                i.remove();
                System.out.println("Second: "+ i);
            }
            else {
                set.add(s);
                System.out.println("Second: "+ s);
            }
        }
        while ((stop_line = br2.readLine()) != null){        	
        	if (lineList2.contains(stop_line)){
           		lineList2.remove(stop_line);           		
           	}
        }
        br2.close();
        for (String outputLine : lineList2){
	        writer.write(outputLine);
	        writer.write("\n");
	        System.out.println(outputLine);
        }
        writer.close();
    } catch(IOException e) {
        System.out.println(e);
    } finally {
  	  if (writer != null) try { writer.close(); } catch (IOException ignore) {}
	}
    
}


}
