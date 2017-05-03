package ch.ethz.livingscience.pages;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
	int noOfYears = 12;

	public NGramsPage(Document doc, ProfilesDB db, String profileID, NGramStore2_inMemory ngramsStore) throws IOException
	{
		super(doc, db, profileID);
		this.ngramsStore = ngramsStore;
		
		float[] allYears =  ngramsStore.getYears();
		years = Arrays.copyOfRange(allYears, allYears.length - noOfYears, allYears.length);
	}
	

	static final Range yearsRange = new Range(2000, 2013);
	
	public void exec() throws IOException
	{
		loadPubs();
		
		NGrams ngramsExtractor = NGrams.getInstance();
		Set<String> ngramsSet = new HashSet<>();
		for (Publication pub : pubs)
		{
			if (pub.title != null) ngramsSet.addAll(ngramsExtractor.getNGrams(pub.title, 2, 3));
			if (pub.summary != null) ngramsSet.addAll(ngramsExtractor.getNGrams(pub.summary, 2, 3));
		}
		

		List<NGramInfo> ngrams = new ArrayList<>();
		for (String entry : ngramsSet)
		{
			ngrams.add(new NGramInfo(entry));
		}
		
		for (NGramInfo ni : ngrams)
		{
			float[] result = ngramsStore.getNormalizedYearCounts(ni.name);
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
		
		
		

		
//		float maxYGrid = Range.getSmallest1Digitbound(maxY, true);
//		if (maxYGrid == 0f) maxYGrid = 1f;
//		Axis yAxis = new Axis("Fraction of all publications", new Range(0, maxYGrid), new float[] {0, maxYGrid / 2f, maxYGrid}, new String[] {"0", "" + (maxYGrid / 2), "" + maxYGrid});
//		
		

		int noOfPlots = 0;
		try
		{	
			PlotCanvasSVG canvas = getSVGPlot(ngrams, years);
			noOfPlots = canvas.noOfPlots();
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
		p.appendChild(ngrams.size() + " keywords could be extracted from the publication list of " + profile.name + ". Displayed are the " + noOfPlots + " keywords that have the best upward trend during " + (int) years[0] + " and " + (int) years[years.length - 1] + "."
				+ "The plot shows the fraction of all publications for a specific year that contains the keyword.");
		stats.appendChild(p);
		
//		p = new Element("p", ns);
//		stats.appendChild("Web of Science lists about 47 million publications. We extracted 6.934.872 (1,2,3)-grams from publication titles, ignoring common stop words at beginning and end of n-grams and not considering n-grams that occur less than 5 times.");
//		stats.appendChild(p);
	}
	

	
	static PlotCanvasSVG getSVGPlot(List<NGramInfo> ngrams, float[] years) throws Exception
	{
		List<Plot> plots = new ArrayList<>();
		float minY = Float.MAX_VALUE;
		float maxY = 0;
		int addedCount = 0;
		int i = 0;
		String ngramsAdded = "";
		while (i < ngrams.size() && addedCount < 8)
		{
			NGramInfo ngram = ngrams.get(i);
			i++;
			if (ngram.score == 0f) continue;
			if (ngramsAdded.indexOf(ngram.name) != -1) continue; // is a subset of an existing ngram
			
			RgbColor color = addedCount < colors.length ? colors[addedCount] : new RgbColor((float) Math.random(), (float) Math.random(), (float) Math.random());
			
			Plot p = new Plot(years, ngram.result);
			
			PlotLineProps props = new PlotLineProps(color);
			p.props = props;
			plots.add(p);

			p = new Plot(years, ngram.result);
			PlotDotProps dprops = new PlotDotProps(color);
			dprops.shape = shapes[addedCount % shapes.length];
			p.props = dprops;
			p.label = ngram.name;
			plots.add(p);
			
			
			addedCount++;
			ngramsAdded += ngram.name + ";";
			
			maxY = Math.max(maxY, p.yRange.max);
			minY = Math.min(minY, p.yRange.min);
			
			// prevent 0 values which leads to missing line plots (screen coords in infinity)
			for (int j=0; j<ngram.result.length; j++) ngram.result[j] = Math.max(1e-10f, ngram.result[j]);
		}
		
		int min = -1;
		for (; min>=-6; min--) if (Math.pow(10, min) < minY) break;
		
		int max = -7;
		for (; max<=-1; max++) if (Math.pow(10, max) > maxY) break;
		Axis yAxis = new Axis("Fraction", 1, min, 1, max);
		
		
		PlotCanvasSVG.createOuterBorderRect = false;
		PlotCanvasSVG canvas = new PlotCanvasSVG(800, 450,
				new Axis("Year", yearsRange, yearsRange.generateTicks(14)), 
				yAxis, 300);
		PlotCanvasSVG.createOuterBorderRect = true;
		
		for (Plot p : plots)
		{
			canvas.draw(p);
		}
		
		return canvas;
	}
	
	public static void main(String[] args) throws Exception
	{
		File outDir = new File("/Users/cschulz/Documents/projects/2013_ia/researchplans/");
		
		OfflineNGramsPlot.create(new File(outDir, "websites.svg"), 
				"facebook", "youtube", "wikipedia", "linkedin", "twitter", "ebay"
				);
		
		OfflineNGramsPlot.create(new File(outDir, "keywords.svg"), 
				"online communities", "online community", "social networks", "social network", "online social networks"
				);
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
		if (result == null) return;

		
		float maxRise = 0f;
		int last = result.length - dt;
		for (int i=0; i<last; i++)
		{
			float begin = result[i];
			float end = result[i+dt];
			if (begin < minFraction || end < minFraction) continue;
			
			float rise = end / begin;
			maxRise = Math.max(rise, maxRise);
		}
		score = maxRise;
		
//		// sum
//		for (int i=0; i<result.length; i++)
//		{
//			score += result[i];
//		}
	}

	public int compareTo(NGramInfo o) 
	{
		return (int) ((o.score - score) * 1000000f);
	}
}

class OfflineNGramsPlot
{
	public static void create(File outsvgFile, String... keywords) throws Exception 
	{
		File ngramsFile = new File("/Users/cschulz/Documents/data/wos/ngrams/from2000/ngramsYears_2000_2011.txt");
		NGramStore2_inMemory ngramsStore = new NGramStore2_inMemory(ngramsFile, 2000, 2011);
		float[] allYears =  ngramsStore.getYears();
		int noOfYears = 12;
		float[] years = Arrays.copyOfRange(allYears, allYears.length - noOfYears, allYears.length);
		
		List<NGramInfo> ngrams = new ArrayList<>();
		for (String entry : keywords)
		{
			ngrams.add(new NGramInfo(entry));
		}
		
		for (NGramInfo ni : ngrams)
		{
			float[] result = ngramsStore.getNormalizedYearCounts(ni.name);
			if (result != null)
			{
				ni.result = Arrays.copyOfRange(result, result.length - noOfYears, result.length);
			}
		}
		for (NGramInfo ni : ngrams)
		{
			ni.score = 1f;
		}
		Collections.sort(ngrams);
		
		PlotCanvasSVG canvas = NGramsPage.getSVGPlot(ngrams, years);
		canvas.save(outsvgFile);
	}
}