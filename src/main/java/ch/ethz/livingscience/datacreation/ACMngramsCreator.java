package ch.ethz.livingscience.datacreation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import javax.sound.sampled.LineListener;


import com.mongodb.BasicDBObject;

import ch.ethz.livingscience.data.ProfilesDB;

public abstract class ACMngramsCreator implements LineListener{
	
public static void main(String[] args) throws Exception
{
    exec();
	
//	String[] ancestors = {"Document types", "General and reference"};
//	String[] children = {"Reliability", "Empirical studies", "Measurement", "Metrics", "Evaluation", "Experimentation", "Estimation", "Design", "Performance", "Validation", "Verification"};
//	for(String child:children) {
//		BasicDBObject doc = new BasicDBObject("name", child).append("ancestors", ancestors);
//	    db.collAcm.insert(doc);
//	}
}

static List<String> ancestors = new ArrayList<String>();
static List<String> indents = new ArrayList<String>();
static ProfilesDB db;
static int count = 0;
public static void exec() throws Exception {
	db = new ProfilesDB(27013);
	BufferedReader reader = new BufferedReader(new FileReader(new File("data/acmngrams/acmdata.txt")));
	String line = null;
	int index = 0;
	while ((line = reader.readLine()) != null)
	{
		newLine(index, line);
		index++;
	}
	reader.close();
	//TextFileUtil.loadList(new File("data/acmngrams/acmdata.txt"), this);
	String addchild = ancestors.remove(ancestors.size()-1);
	String addindent = indents.remove(indents.size()-1);
	count--;
	//remove siblings
	for(int i=ancestors.size()-1;i>=0;i--) {
		if(addindent.split("\\.").length<=indents.get(i).split("\\.").length) {
			ancestors.remove(i);
			indents.remove(i);
			count--;
		}
		else {
			i=-1;
		}
	}
	//add the last line
	String[] addancestors = ancestors.toArray(new String[0]);
	BasicDBObject doc = new BasicDBObject("name", addchild).append("ancestors", addancestors);
    db.collAcm.insert(doc);
}
public static void newLine(int index, String line) 
{
	String[] parts = line.split(";;;");
	String indentation = parts[0];
	String value = parts[1];
	if(count>0) {
		//is children
		if(indentation.split("\\.").length>indents.get(indents.size()-1).split("\\.").length) {
			ancestors.add(value);
			indents.add(indentation);
			count++;
		}
		//is sibling or parent of the next-->add the previous because it had no children
		else {
			String addchild = ancestors.remove(ancestors.size()-1);
			String addindent = indents.remove(indents.size()-1);
			count--;
			//remove siblings
			for(int i=ancestors.size()-1;i>=0;i--) {
				if(addindent.split("\\.").length<=indents.get(i).split("\\.").length) {
					ancestors.remove(i);
					indents.remove(i);
					count--;
				}
				else {
					i=-1;
				}
			}
			String[] addancestors = ancestors.toArray(new String[0]);
			BasicDBObject doc = new BasicDBObject("name", addchild).append("ancestors", addancestors);
		    db.collAcm.insert(doc);
			//remove siblings of the new
			for(int i=ancestors.size()-1;i>=0;i--) {
				if(indentation.split("\\.").length<=indents.get(i).split("\\.").length) {
					ancestors.remove(i);
					indents.remove(i);
					count--;
				}
				else {
					i=-1;
				}
			}
		    //now add the current element
		    ancestors.add(value);
			indents.add(indentation);
			count++;
		}
	}
	else {
		ancestors.add(value);
		indents.add(indentation);
		count++;
	}
}
}
