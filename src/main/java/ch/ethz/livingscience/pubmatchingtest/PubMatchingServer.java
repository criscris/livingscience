package ch.ethz.livingscience.pubmatchingtest;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import ch.ethz.livingscience.arxiv.api.ArxivMetaFinder;
import utils.Log;
import utils.server.JettyServer;

public class PubMatchingServer
{
	public static void main(String[] args) throws Exception
	{	
		int port = 0;
//		String pathToLivingScienceDB = null;
		File arxivMetaFile = null;
		try
		{
			port = new Integer(args[0]);
			arxivMetaFile = new File(args[1]);
//			pathToLivingScienceDB = args[1]; 
//			if (pathToLivingScienceDB == null) throw new Exception();
		}
		catch (Exception ex)
		{
			System.out.println("Specify parameters: port arxivMetaFile"); // pathToLivingScienceDB");
			return;
		} 
		
		File logFile = new File("log.txt");
		Log.setLogFile(logFile);
		
//		File livingScienceDBDir = new File(pathToLivingScienceDB);
//		if (!livingScienceDBDir.exists())
//		{
//			System.out.println(livingScienceDBDir.getAbsolutePath() + " does not exist.");
//			return;
//		}
//		final LivingSciencePerformanceTestServlet dbTestServlet = new LivingSciencePerformanceTestServlet(livingScienceDBDir);
		
		ArxivMetaFinder finder = new ArxivMetaFinder(arxivMetaFile);
		final PubMatchingServlet pubMatchingServlet = new PubMatchingServlet(finder);
		System.out.println("start server.");
		
		JettyServer coral = new JettyServer(port) 
		{
			public void init(ServletContextHandler context) 
			{
//				context.addServlet(new ServletHolder(dbTestServlet), "/saml/test");
//				context.addServlet(new ServletHolder(new SamlServlet()), "/saml/*");
				
				context.addServlet(new ServletHolder(new MaintenanceServlet()), "/*");
				context.addServlet(new ServletHolder(pubMatchingServlet), "/pubmatching");
			}
		};
		coral.exec();
	}
}

class MaintenanceServlet extends HttpServlet 
{
	private static final long serialVersionUID = 1L;
	
	
	Date startDate;
	long startTime;
	public MaintenanceServlet()
	{
		startDate = Calendar.getInstance().getTime();
		startTime = System.currentTimeMillis();
	}

	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException 
	{
		Log.logRequest(req);
		resp.getWriter().write("LIVING SCIENCE server runs since " + new SimpleDateFormat("yyyy-MM-dd HH:mm:SSZ").format(startDate) + " (" + 
		((System.currentTimeMillis() - startTime)/1000) + " seconds ago).");
	}
}


