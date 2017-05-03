package ch.ethz.livingscience.pubmatchingtest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import ch.ethz.livingscience.arxiv.ArxivCitation;
import ch.ethz.livingscience.arxiv.api.ArxivMetaFinder;
import utils.Log;
import utils.text.TextFileUtil;

public class PubMatchingServlet extends HttpServlet
{
	private static final long serialVersionUID = 1L;

	ArxivMetaFinder finder;
	
	public PubMatchingServlet(ArxivMetaFinder finder) throws Exception
	{
		this.finder = finder;
	}
	
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException 
	{
		Log.logRequest(req);
		
		String title = req.getParameter("title");
		String authors = req.getParameter("authors");
		String journal = req.getParameter("journal");
		String year = req.getParameter("year");
		
		if (title == null && authors == null && journal == null && year == null)
		{
			resp.getWriter().write("USAGE:\n\t?title=publication title\n\t"
					+ "&authors=J Smith, W Lee\n\t"
					+ "&journal=superjournal 433, 23-25 [syntax does not matter, can include volume / issue / pages etc.]\n\t"
					+ "&year=2013");
			return;
		}
		
		ArxivCitation searchedCitation = new ArxivCitation();
		if (title != null) 
		{
			if (title.length() > 150) title = title.substring(0, 150);
			searchedCitation.title = title;
		}
		if (authors != null) 
		{
			List<String> authorsList = new ArrayList<>();
			for (String part : TextFileUtil.split(authors, ","))
			{
				authorsList.add(part.trim());
				if (authorsList.size() >= 5) break;
			}
			searchedCitation.authors = authorsList;
		}
		if (journal != null)
		{
			if (journal.length() > 150) journal = journal.substring(0, 150);
			searchedCitation.journal = journal;
		}
		if (year != null)
		{
			try
			{
				int y = new Integer(year);
				searchedCitation.year = y;
			}
			catch (Exception ex)
			{
				
			}
		}
		

		
		long time = System.currentTimeMillis();
		ArxivCitation foundCitation = finder.findArxivIDMostSimilarTo(searchedCitation);
		time = System.currentTimeMillis() - time;
		
		JSONObject result = new JSONObject();
		try
		{		
			result.put("processingTimeInMs", time);
			
			
			if (foundCitation == null)
			{
				if (finder.lastBestScore() == 0f)
				{
					result.put("message", "Request overspecified or no matching publication in database.");
				}
				else
				{
					result.put("message", "Request underspecified. Need more information!");
				}
			}
			else
			{
				
				JSONObject request = new JSONObject();
				request.put("title", searchedCitation.title);
				request.put("authors", new JSONArray(searchedCitation.authors));
				request.put("journal", searchedCitation.journal);
				request.put("year", searchedCitation.year);
				result.put("request", request);
				
				JSONObject response = new JSONObject();
				response.put("arxivID", foundCitation.arxivID);
				if (foundCitation.doi.length() > 0) response.put("doi", foundCitation.doi);
				response.put("title", foundCitation.title);
				response.put("authors", new JSONArray(foundCitation.authors));
				response.put("journal", foundCitation.journal);
				response.put("year", foundCitation.year);
				
				result.put("response", response);
				float score = (float) Math.round(finder.lastBestScore() * 100) / 100f;
				result.put("matchingScore", "" + score);
			}
			
			resp.getWriter().write(result.toString(2));
		}
		catch (JSONException ex)
		{
			
		}
	}
}
