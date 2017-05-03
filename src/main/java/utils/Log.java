package utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

public class Log {

	private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
	
	private static File logFile;
	public static void setLogFile(File logFile)
	{
		Log.logFile = logFile;
	}
	
	public static final void log(String message)
	{
		String s = sdf.format(Calendar.getInstance().getTime()) + " " + message;
		if (logFile != null)
		{
			try
			{
				BufferedWriter writer = new BufferedWriter(new FileWriter(logFile, true));
				writer.write(s);
				writer.write("\n");
				writer.close();
			}
			catch (Exception ex)
			{
				
			}
		}
		else
		{
			System.out.println(s);
		}
	}	
	
	public static final void logRequest(HttpServletRequest request)
	{
		logRequest(request, null);
	}
	
	private static final String proxyHeader = "X-Forwarded-For";
	private static final String ignoreFavicon = "/favicon.ico";
	public static final void logRequest(HttpServletRequest request, HttpSession session)
	{
		String requestURI = request.getRequestURI();
		if (ignoreFavicon.equals(requestURI)) return;
				
		String hiddenIP = request.getHeader(proxyHeader);
		String remoteAddress = hiddenIP != null ? hiddenIP : request.getRemoteAddr();
		
		String query = request.getQueryString();
		
		String sessionID = session != null ? session.getId() + " " : "";
		log(sessionID + remoteAddress + " -> " + request.getMethod() + " " + requestURI + (query != null ? "?" + query : ""));
	}

}
