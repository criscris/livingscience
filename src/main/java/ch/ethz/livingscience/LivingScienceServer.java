package ch.ethz.livingscience;

import java.io.File;

import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import ch.ethz.livingscience.data.ProfilesDB;
import ch.ethz.livingscience.data.ProfilesSearchIndex;
import ch.ethz.livingscience.ngrams.NGramStore2_inMemory;
import utils.Log;
import utils.server.JettyServer;

public class LivingScienceServer
{
	public static void main(String[] args) throws Exception
	{	
		int port = 0;
		int mongoPort = 0;
		File staticContentDir = null;
		File ngramsFile = null;
		File ngramsGlobalFile = null;
		File searchIndexDir = null;
		int fromYear = 2005;
		int toYear = 2018;
		try
		{
			port = new Integer(args[0]);
			mongoPort = new Integer(args[1]);
			staticContentDir = new File(args[2]);
			ngramsFile = new File(args[3]);
			ngramsGlobalFile = new File(args[4]);
			searchIndexDir = new File(args[5]);
			fromYear = new Integer(args[6]);
			toYear = new Integer(args[7]);
		}
		catch (Exception ex)
		{
			System.out.println("USAGE: port portMongoDB pathToStaticContent ngramsFile searchIndexDir"); // pathToLivingScienceDB");
			return;
		}
		File logFile = new File("log.txt");
		Log.setLogFile(logFile);
		
		ProfilesDB db = new ProfilesDB(mongoPort);
		System.out.println("DB started.");
		
		ProfilesSearchIndex searchIndex = new ProfilesSearchIndex(db, searchIndexDir);
		System.out.println("Search index built.");
		
//		System.out.println(ngramsFile.getAbsolutePath());
//		File parent = ngramsFile.getParentFile();
//		String name = ngramsFile.getName();
//		int i1 = name.lastIndexOf(".");
//		File indexFile = new File(parent, name.substring(0, i1) + "_index" + name.substring(i1));
//		System.out.println(indexFile.getAbsolutePath());
//		RandomAccessTextFile rat = new RandomAccessTextFile(ngramsFile, indexFile);
		
		//MANUAL: range of years for ngrams plot
		NGramStore2_inMemory ngramsStore = new NGramStore2_inMemory(ngramsFile, 2005, 2018);
		
		final LivingScienceServlet profilesServlet = new LivingScienceServlet(staticContentDir, db, searchIndex, ngramsStore, ngramsGlobalFile, fromYear, toYear);
		System.out.println("Servlet initialized.");
		
		JettyServer coral = new JettyServer(port) 
		{
			public void init(ServletContextHandler context) 
			{
				context.addServlet(new ServletHolder(profilesServlet), "/*");
			}
		};
		coral.exec();
	}
}