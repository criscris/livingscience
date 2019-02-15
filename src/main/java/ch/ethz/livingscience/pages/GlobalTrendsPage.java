package ch.ethz.livingscience.pages;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ch.ethz.livingscience.data.ProfilesDB;
import ch.ethz.livingscience.ngrams.NGramScore;
import ch.ethz.livingscience.ngrams.NGramStore2_inMemory;
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

public class GlobalTrendsPage extends ProfilePubListPage
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
	static HashSet<String> globalWords;
	//number of keywords to plot for a global trends graph
    static int noOfKeywords = 10;
	// The score will be the average slope over one year intervals
//	Map<String, Double> ngramToScore;
	float[] years;
	int noOfYears;
	int fromYear, toYear;

	public GlobalTrendsPage(Document doc, ProfilesDB db, String profileID, NGramStore2_inMemory ngramsStore, File ngramsGlobalFile, int fromYear, int toYear) throws IOException
	{
		super(doc, db, profileID);
		this.ngramsStore = ngramsStore;
		this.noOfYears = ngramsStore.getNoOfYears();
		this.globalWords = getGlobalWords(ngramsGlobalFile);
		this.fromYear = fromYear;
		this.toYear = toYear;

//		this.ngramToScore = ngramsStore.getNgramToScore();
		
		float[] allYears =  ngramsStore.getYears();
		years = Arrays.copyOfRange(allYears, allYears.length - noOfYears, allYears.length);
		
	}
	public HashSet<String> getGlobalWords(File wordsFile) throws IOException {
		HashSet<String> globalwords = new HashSet<>();
		BufferedReader reader = new BufferedReader(new FileReader(wordsFile));
		String line = null;
		while ((line = reader.readLine()) != null)
		{
			globalwords.add(line);
		}
		reader.close();
		return globalwords;
	}
	public void exec() throws IOException
	{	
		List<NGramScore> ngrams = ngramsStore.getLastYearList();
//		for (String entry : ngramToScore.keySet())
//		{
//			ngrams.add(new NGramScore(entry,ngramToScore.get(entry),ngramsStore.getPercentageYearCounts(entry)));
//		}

		//Plot1: most active last year
		Element heading = new Element("div", ns);
		heading.addAttribute(new Attribute("class", "mainHeading"));
		heading.appendChild("Global most active keywords in " + toYear +" : ");
		content.appendChild(heading);		

		int noOfPlots = 0;
		try
		{	
			PlotCanvasSVG canvas = getSVGPlot(ngrams, years, new Range(ngramsStore.getMin(),ngramsStore.getMax()));
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
		int ffyear = toYear - 1;
		p.appendChild("Global top " + noOfKeywords + " upward trends keywords.");
		stats.appendChild(p);

		//Plot2: most increase last year
		ngrams = ngramsStore.getLastYearIncreaseList();
		Element heading2 = new Element("div", ns);
		heading2.addAttribute(new Attribute("class", "mainHeading"));
		heading2.appendChild("Global upwards trends from " + ffyear + " to " + toYear + ":");
		content.appendChild(heading2);		

		noOfPlots = 0;
		try
		{	
			PlotCanvasSVG canvas2 = getSVGPlot(ngrams, years, new Range(ngramsStore.getMin(),ngramsStore.getMax()));
			noOfPlots = canvas2.noOfPlots();
			Element svg2 = canvas2.getSVG();
			
			svg2.addAttribute(new Attribute("width", (int) (0.65f * canvas2.getWidth()) + "px"));
			svg2.addAttribute(new Attribute("height", (int) (0.65f * canvas2.getHeight()) + "px"));
					
			Element stats2 = new Element("div", ns);
			stats2.addAttribute(new Attribute("class", "contentEntry"));
			stats2.appendChild(svg2);
					
			content.appendChild(stats2);
					
		}
		catch (Exception e)
		{
			throw new IOException(e.getCause());
		}
				
				
		Element stats2 = new Element("div", ns);
		stats2.addAttribute(new Attribute("class", "contentEntry"));
		content.appendChild(stats2);
				
				
		Element p2 = new Element("p", ns);
		p2.appendChild("Global top " + noOfKeywords + " upward increase trends keywords.");
		stats2.appendChild(p2);
				
				
		// Now the downwards trends
		ngrams = ngramsStore.getTotalIncreaseList();
		Element headingDown = new Element("div", ns);
		headingDown.addAttribute(new Attribute("class", "mainHeading"));
		headingDown.appendChild("Global downwards trends from " + fromYear + " to " + toYear + ": ");
		content.appendChild(headingDown);		

		noOfPlots = 0;
		try
		{	
			PlotCanvasSVG canvasDown = getSVGPlotDown(ngrams, years, new Range(ngramsStore.getMin(),ngramsStore.getMax()));
			noOfPlots = canvasDown.noOfPlots();
			Element svgDown = canvasDown.getSVG();
			
			svgDown.addAttribute(new Attribute("width", (int) (0.65f * canvasDown.getWidth()) + "px"));
			svgDown.addAttribute(new Attribute("height", (int) (0.65f * canvasDown.getHeight()) + "px"));
			
			Element statsDown = new Element("div", ns);
			statsDown.addAttribute(new Attribute("class", "contentEntry"));
			statsDown.appendChild(svgDown);
			
			content.appendChild(statsDown);
			
		}
		catch (Exception e)
		{
			throw new IOException(e.getCause());
		}
		
		
		Element statsDown = new Element("div", ns);
		statsDown.addAttribute(new Attribute("class", "contentEntry"));
		content.appendChild(statsDown);
		
		
		Element pDown = new Element("p", ns);
		pDown.appendChild("Global top " + noOfKeywords + " downward trends keywords.");
		statsDown.appendChild(pDown);
	}
	

	
	static PlotCanvasSVG getSVGPlot(List<NGramScore> ngrams, float[] years, Range yearsRange) throws Exception
	{
		List<Plot> plots = new ArrayList<>();
		float minY = Float.MAX_VALUE;
		float maxY = 0;
		int addedCount = 0;
		int i = 0;
		String ngramsAdded = "";
		while (i < ngrams.size() && addedCount < noOfKeywords)
		{
			NGramScore ngram = ngrams.get(i);
			i++;
//			System.out.println(ngram.name + ", " + ngram.score);
			if(globalWords.contains(ngram.name)) {
				//get the log of the result array
//				float[] presult = ngram.result;
//				for(int j=0;j<presult.length;j++)
//				{
//					presult[j] = (float) Math.log10(presult[j]);
//				}
				if (ngram.score == 0f) continue;
				if (ngramsAdded.indexOf(ngram.name) != -1) continue; // is a subset of an existing ngram
				
				RgbColor color = addedCount < colors.length ? colors[addedCount] : new RgbColor((float) Math.random(), (float) Math.random(), (float) Math.random());
				
				Plot p = new Plot(years, ngram.result);
				//Try plotting the logarithm instead
//				Plot p = new Plot(years, presult);
				
				PlotLineProps props = new PlotLineProps(color);
				p.props = props;
				plots.add(p);

				p = new Plot(years, ngram.result);
//				p = new Plot(years, presult);
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
			
		}
		int min = 0;
		for (; min>=-6; min--) if (Math.pow(10, min) < minY) break;
		
		int max = 1;
		for (; max<=-1; max++) if (Math.pow(10, max) > maxY) break;
//		Axis yAxis = new Axis("Fraction", 1, min, 1, max);
		Range yrange = new Range(minY, maxY);
		Axis yAxis = new Axis("Percentage", yrange,yrange.generateTicks(10));
		
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
	
	static PlotCanvasSVG getSVGPlotDown(List<NGramScore> ngrams, float[] years, Range yearsRange) throws Exception
	{
		List<Plot> plots = new ArrayList<>();
		float minY = Float.MAX_VALUE;
		float maxY = 0;
		int addedCount = 0;
		int i = 0;
		String ngramsAdded = "";
		while (i < ngrams.size() && addedCount < noOfKeywords)
		{
			NGramScore ngram = ngrams.get(ngrams.size()-i-1);
			//get the log of the result array
//			float[] presult = ngram.result;
//			for(int j=0;j<presult.length;j++)
//			{
//				presult[j] = (float) Math.log10(presult[j]);
//			}
			i++;
			if(globalWords.contains(ngram.name)) {
				if (ngram.score == 0f) continue;
				if (ngramsAdded.indexOf(ngram.name) != -1) continue; // is a subset of an existing ngram
				
				RgbColor color = addedCount < colors.length ? colors[addedCount] : new RgbColor((float) Math.random(), (float) Math.random(), (float) Math.random());
				
				Plot p = new Plot(years, ngram.result);
				//Try plotting the logarithm instead
//				Plot p = new Plot(years, presult);
				
				PlotLineProps props = new PlotLineProps(color);
				p.props = props;
				plots.add(p);

				p = new Plot(years, ngram.result);
//				p = new Plot(years, presult);
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
			
		}
		int min = 0;
		for (; min>=-6; min--) if (Math.pow(10, min) < minY) break;
		
		int max = 1;
		for (; max<=-1; max++) if (Math.pow(10, max) > maxY) break;
//		Axis yAxis = new Axis("Fraction", 1, min, 1, max);
		Range yrange = new Range(minY, maxY);
		Axis yAxis = new Axis("Percentage", yrange,yrange.generateTicks(10));
		
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


