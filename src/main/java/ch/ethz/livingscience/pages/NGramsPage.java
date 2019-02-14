package ch.ethz.livingscience.pages;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.mongodb.DBCursor;
import com.mongodb.DBObject;

import nu.xom.Attribute;
import nu.xom.Document;
import nu.xom.Element;
import utils.image.RgbColor;
import utils.plot.Axis;
import utils.plot.Plot;
import utils.plot.PlotCanvasSVG;
import utils.plot.PlotDotProps;
import utils.plot.PlotLineProps;
import utils.plot.Range;
import utils.plot.shape.DotShape;
import utils.plot.shape.FreeShape;
import utils.plot.shape.FreeShape.FreeShapeType;
import ch.ethz.livingscience.data.ProfilesDB;
import ch.ethz.livingscience.data.Publication;
import ch.ethz.livingscience.ngrams.NGramStore2_inMemory;
import ch.ethz.livingscience.ngrams.NGrams;
/*
 * The "Fraction" represents (number of occurences of the ngram that year)/(total number of occurrences of that ngram).
 * It uses the ngramYears text file to get the number of ocurrences. The reason the plot goes down is that the graph takes
 * the minimum between a score or 1e-10f, so for years in which a term was still not popular the result goes down.
 * The plot gets cut when the result of the log is -infinity (i.e. the fraction is too close to zero)
 */
public class NGramsPage extends ProfilePubListPage
{
	static RgbColor[] colors = new RgbColor[]
	{
		RgbColor.fromInt(49, 130, 189),
		RgbColor.fromInt(32, 32, 32),
		RgbColor.fromInt(230, 85, 13),
		RgbColor.fromInt(253, 141, 60),
		
		RgbColor.fromInt(161, 217, 155),
		RgbColor.fromInt(49, 163, 84),
		RgbColor.fromInt(158, 154, 200),
		RgbColor.fromInt(99, 99, 99),
	};
	
	static DotShape[] shapes = new DotShape[] 
	{
			new FreeShape(FreeShapeType.Circle, 6f),
			new FreeShape(FreeShapeType.Cross, 6f),
			new FreeShape(FreeShapeType.Triangle, 6f),
			new FreeShape(FreeShapeType.Quad, 6f),
	};
	
	NGramStore2_inMemory ngramsStore;
	float[] years;
	int noOfYears;
	//number of keywords to plot for an author
    static int noOfKeywords = 5;
	public NGramsPage(Document doc, ProfilesDB db, String profileID, NGramStore2_inMemory ngramsStore) throws IOException
	{
		super(doc, db, profileID);
		this.ngramsStore = ngramsStore;
		this.noOfYears = ngramsStore.getNoOfYears();
		this.years = ngramsStore.getYears();
//		float[] allYears =  ngramsStore.getYears();
//		years = Arrays.copyOfRange(allYears, allYears.length - noOfYears, allYears.length);
	}
	

//	static final Range yearsRange = new Range(2005, 2018);
	
	public void exec() throws IOException
	{
		Set<String> shortnames = new HashSet<>();
		DBCursor cursor2 = db.collAcm.find();
		while(cursor2.hasNext()) {
			DBObject dbo2 = cursor2.next();
			String sname = dbo2.get("shortname").toString();
			shortnames.add(sname);
		}
		loadPubs();
		int fromYear = (int)years[0];
		int toYear = (int)years[years.length-1];
		NGrams ngramsExtractor = NGrams.getInstance();
//		Set<String> ngramsSet = new HashSet<>();
		//total pubs of an author per year
		int[] noOfPubs = new int[noOfYears];
//		for (Publication pub : pubs)
//		{
//			if (pub.title != null) ngramsSet.addAll(ngramsExtractor.getNGrams(pub.title, 2, 3));
//			if (pub.summary != null) ngramsSet.addAll(ngramsExtractor.getNGrams(pub.summary, 2, 3));
//		}
		
		/*
		 * change plot to all per author. 2005 shoudlnt be manual. 14 should be years
		 */
		Map<String, int[]> ngramsPSet = new HashMap<>();
		for (Publication pub : pubs)
		{
			if(pub.year>=fromYear & pub.year<=toYear) {
			int index = pub.year - fromYear;
		    noOfPubs[index]+=1;
			List<String> pubNgrams = new ArrayList<>();
//			if (pub.title != null) pubNgrams.addAll(ngramsExtractor.getNGrams(pub.title, 2, 3));
			if (pub.title != null) pubNgrams.addAll(ngramsExtractor.getACMNGrams(pub.title, 2, 3, shortnames));
//			if (pub.summary != null) pubNgrams.addAll(ngramsExtractor.getNGrams(pub.summary, 2, 3));
			if (pub.summary != null) pubNgrams.addAll(ngramsExtractor.getACMNGrams(pub.summary, 2, 3, shortnames));
			Set<String> hs = new HashSet<>();
			hs.addAll(pubNgrams);
			pubNgrams.clear();
			pubNgrams.addAll(hs);
			
			for (String ngram: pubNgrams)
			{	
				int[] countsNGram = ngramsPSet.get(ngram);
				
				//check if ngram is already in list
				if (ngramsPSet.get(ngram) != null)
				{
					//increase count for the nGram
					countsNGram[index] +=1;
					countsNGram[noOfYears] +=1;
					ngramsPSet.put(ngram, countsNGram);
				}
				else
				{
					//add new entry
					countsNGram = new int[noOfYears+1];
					Arrays.fill(countsNGram, 0);
					countsNGram[index] = 1;
					countsNGram[noOfYears] = 1;
					ngramsPSet.put(ngram, countsNGram);
				}
			}
			}	
		}
		
		/*
		 * end
		 */
		List<NGramInfo> ngrams = new ArrayList<>();
//		for (String entry : ngramsSet)
//		{
//			ngrams.add(new NGramInfo(entry));
//		}
		
		for (String entry : ngramsPSet.keySet())
		{
			ngrams.add(new NGramInfo(entry));
		}
		
		for (NGramInfo ni : ngrams)
		{
			//change fraction
			//float[] result = ngramsStore.getNormalizedYearCounts(ni.name);
			/*
			 * 
			 */
			float[] result = new float[noOfYears];
			for (int i=0; i<noOfYears; i++)
			{
				result[i] = noOfPubs[i] == 0 ? 0f : ((float) ngramsPSet.get(ni.name)[i] / noOfPubs[i]);
			}
			/*
			 * 
			 */
			if (result != null)
			{
				ni.result = Arrays.copyOfRange(result, result.length - noOfYears, result.length);
			}
		}
		for (NGramInfo ni : ngrams)
		{
			ni.computeScore();
		}
		Collections.sort(ngrams);
		
		
		Element heading = new Element("div", ns);
		heading.addAttribute(new Attribute("class", "mainHeading"));
		heading.appendChild("Keyword Trends for: " + profile.name);
		content.appendChild(heading);	

		try
		{	
			PlotCanvasSVG canvas = getSVGPlot(ngrams, years);
			Element svg = canvas.getSVG();
			
			svg.addAttribute(new Attribute("width", (int) (0.65f * canvas.getWidth()) + "px"));
			svg.addAttribute(new Attribute("height", (int) (0.65f * canvas.getHeight()) + "px"));
			
			Element stats = new Element("div", ns);
			stats.addAttribute(new Attribute("class", "contentEntry"));
			stats.appendChild(svg);
			
			content.appendChild(stats);
		}
		catch (Exception e)
		{
			throw new IOException(e.getCause());
		}
		
		
		Element stats = new Element("div", ns);
		stats.addAttribute(new Attribute("class", "contentEntry"));
		content.appendChild(stats);
		
		
		Element p = new Element("p", ns);
		p.appendChild(ngrams.size() + " keywords could be extracted from the publication list of " + profile.name + ". Displayed are the " + noOfKeywords + " keywords that have the best upward trend during " + (int) years[0] + " and " + (int) years[years.length - 1] + "."
				+ "The plot shows the percentage of all publications of the author for a specific year that contain the keyword.");
		stats.appendChild(p);
	}
	

	
	static PlotCanvasSVG getSVGPlot(List<NGramInfo> ngrams, float[] years) throws Exception
	{
		List<Plot> plots = new ArrayList<>();
		float minY = Float.MAX_VALUE;
		float maxY = 0;
		int addedCount = 0;
		int i = 0;
		String ngramsAdded = "";
		while (i < ngrams.size() && addedCount < noOfKeywords)
		{
			NGramInfo ngram = ngrams.get(i);
			System.out.println(ngram.name + " and score: " + ngram.score);
			//get the log of the result array
			float[] presult = ngram.result;
//			for(int j=0;j<presult.length;j++)
//			{
//				presult[j] = (float) Math.log10(presult[j]);
//			}
			i++;
			if (ngram.score == 0f) continue;
			if (ngramsAdded.indexOf(ngram.name) != -1) continue; // is a subset of an existing ngram
			
			RgbColor color = addedCount < colors.length ? colors[addedCount] : new RgbColor((float) Math.random(), (float) Math.random(), (float) Math.random());
			
			//Plot p = new Plot(years, ngram.result);
			//Try plotting the logarithm instead
			Plot p = new Plot(years, presult);
			
			PlotLineProps props = new PlotLineProps(color);
			p.props = props;
			plots.add(p);

			//p = new Plot(years, ngram.result);
			p = new Plot(years, presult);
			PlotDotProps dprops = new PlotDotProps(color);
			dprops.shape = shapes[addedCount % shapes.length];
			p.props = dprops;
			p.label = ngram.name;
			plots.add(p);
			
			
			addedCount++;
			ngramsAdded += ngram.name + ";";
			
			maxY = Math.max(maxY, p.yRange.max);
			minY = Math.min(minY, p.yRange.min);
		}
		
		int min = 0;
		for (; min>=-6; min--) if (Math.pow(10, min) < minY) break;
		
		int max = 1;
		for (; max<=-1; max++) if (Math.pow(10, max) > maxY) break;
		//Axis yAxis = new Axis("Fraction", 1, min, 1, max);
		Range yrange = new Range(0,1);
		Axis yAxis = new Axis("Percentage", yrange,yrange.generateTicks(6));
		
		Range yearsRange = new Range(years[0], years[years.length-1]);
		//for helbing dirk min=-7,max=-1
		PlotCanvasSVG.createOuterBorderRect = false;
		PlotCanvasSVG canvas = new PlotCanvasSVG(800, 450,
				new Axis("Year", yearsRange, yearsRange.generateTicks(years.length)), 
				yAxis, 300);
		PlotCanvasSVG.createOuterBorderRect = true;
		
		for (Plot p : plots)
		{
			canvas.draw(p);
		}

		return canvas;
	}
	
}

class NGramInfo implements Comparable<NGramInfo>
{
	static final int dt = 4;
	static final float minFraction = 1e-6f;
	
	String name;
	float[] result;
	float score;
	
	public NGramInfo(String name)
	{
		this.name = name;
	}
	
	public void computeScore()
	{
		score = 0f;
//		if (result == null) return;
//
//		
//		float maxRise = 0f;
//		int last = result.length - dt;
//		for (int i=0; i<last; i++)
//		{
//			float begin = result[i];
//			float end = result[i+dt];
//			if (begin < minFraction || end < minFraction) continue;
//			
//			float rise = end / begin;
//			maxRise = Math.max(rise, maxRise);
//		}
//		score = maxRise;
		//total sum of the slopes, for now calculated as ((counts yeari+1)-(counts yeari))^3 to give more value
		//to steep slopes and keep the sign (plain average would just be lastyear-firstyear)
//		double totalSum = 0;
		int autaverage = result.length;
		for(int i=0;i<result.length;i++)
		{
//			if(result[i]!=0) 
//			{
//				totalSum += 100*(result[i+1]-result[i])/result[i];
//			}
//			else if(result[i+1]!=0)
//			{
//				totalSum+=100;
//			}
			score += (float) 100*result[i]/autaverage;
//			score = (float) (totalSum/(result.length-1));
		}
	}

	public int compareTo(NGramInfo o) 
	{
		return (int) ((o.score - score) * 1000000f);
	}
}