package utils.plot;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import nu.xom.Attribute;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Serializer;
import utils.ToolExecuter;
import utils.image.RgbColor;
import utils.io.BinaryFileUtil;
import utils.math.Vector2f;
import utils.plot.Axis.AxisType;
import utils.plot.shape.CircleShape;
import utils.plot.shape.ErrorBarsShape;
import utils.plot.shape.FreeShape;
import utils.plot.shape.RectShape;
import utils.plot.shape.VerticalBarShape;

import org.apache.commons.codec.binary.Base64;

public class PlotCanvasSVG 
{
	public static final String ns = "http://www.w3.org/2000/svg";
	public static final String nsxlink = "http://www.w3.org/1999/xlink";
	static final String plotClip = "plotClip";
	
	public static boolean createOuterBorderRect = true;
	
	public Element svg;
	public Element svg_data;
	Element svg_info;
	
	final int left = 120; // was 110
	final int right;
	final int top = 28;
	final int bottom = 90;
	final int plotAreaWidth;
	final int plotAreaHeight;
	
	final int tickHalfLength = 4;
	final int plotAreaMargin = 16;
	
	Axis xAxis;
	Axis yAxis;
	
	boolean usePlotLabels;
	int plotLabelCount = 0;
	
	int pixelWidth;
	int pixelHeight;
	
	public int getWidth()
	{
		return pixelWidth;
	}
	
	public int getHeight()
	{
		return pixelHeight;
	}
	
	public Element getSVG()
	{
		return svg;
	}
	
	public PlotCanvasSVG(int plotAreaWidth, int plotAreaHeight, Axis xAxis, Axis yAxis) throws Exception
	{
		this(plotAreaWidth, plotAreaHeight, xAxis, yAxis, 0);
	}
	
	public PlotCanvasSVG(int plotAreaWidth, int plotAreaHeight, Axis xAxis, Axis yAxis, int legendWidth) throws Exception
	{
		if (legendWidth > 0)
		{
			right = legendWidth;
			usePlotLabels = true;
		}
		else
		{
			right = 30; // 30 since may need to some space for the x tick labels
		}
		
		
		this.plotAreaWidth = plotAreaWidth;
		this.plotAreaHeight = plotAreaHeight;
	
		pixelWidth = left + plotAreaWidth + right;
		pixelHeight = top + plotAreaHeight + bottom;
		
		this.xAxis = xAxis;
		this.yAxis = yAxis;
		
		svg = new Element("svg", ns);
		svg.addNamespaceDeclaration("xlink", nsxlink);
		svg.addAttribute(new Attribute("width", pixelWidth + "px"));
		svg.addAttribute(new Attribute("height", pixelHeight + "px"));
		svg.addAttribute(new Attribute("viewBox", "0 0 " + pixelWidth + " " + pixelHeight));
		
		Element defs = new Element("defs", ns);
		svg.appendChild(defs);
		
		svg_data = new Element("g", ns);
		svg.appendChild(svg_data);
		
		svg_info = new Element("g", ns);
		svg.appendChild(svg_info);
		

		Element style = new Element("style", ns);
		defs.appendChild(style);
		style.addAttribute(new Attribute("type", "text/css"));
		style.appendChild(
			".text { font-weight:normal; font-family:'Arial'; fill:#000000 }\n" +
			".textaxislabels { font-size:32px }\n" +
			".textticklabels { font-size:18px }\n" +
			".textticklabelsLog { font-size:20px }\n" +
			".textlegend { font-size:20px }\n" +
			".plotAxisTicks { stroke:#000000; stroke-width:1px }");
		
		// plot area clipping
		Element clip = new Element("clipPath", ns);
		clip.addAttribute(new Attribute("id", "" + plotClip));
		Element clipRect = new Element("rect", ns);
		clip.appendChild(clipRect);
		clipRect.addAttribute(new Attribute("x", "" + left));
		clipRect.addAttribute(new Attribute("y", "" + top));
		clipRect.addAttribute(new Attribute("width", "" + plotAreaWidth));
		clipRect.addAttribute(new Attribute("height", "" + plotAreaHeight));
		defs.appendChild(clip);		

		
		Element rect = new Element("rect", ns);
		svg_info.appendChild(rect);
		rect.addAttribute(new Attribute("x", "" + (left - plotAreaMargin)));
		rect.addAttribute(new Attribute("y", "" + (top - plotAreaMargin)));
		rect.addAttribute(new Attribute("width", "" + (plotAreaWidth + 2*plotAreaMargin)));
		rect.addAttribute(new Attribute("height", "" + (plotAreaHeight + 2*plotAreaMargin)));
		rect.addAttribute(new Attribute("style", "fill:none; stroke:#000000; stroke-width:1px"));
		
		// do not clip bounds
		if (createOuterBorderRect)
		{
			rect = new Element("rect", ns);
			svg_info.appendChild(rect);
			rect.addAttribute(new Attribute("x", "0"));
			rect.addAttribute(new Attribute("y", "0"));
			rect.addAttribute(new Attribute("width", "" + pixelWidth));
			rect.addAttribute(new Attribute("height", "" + pixelHeight));
			rect.addAttribute(new Attribute("style", "fill:none; stroke:#FFFFFF; stroke-width:1px"));
		}
			    
		
		// xLabel
	    Element text = new Element("text", ns);
	    svg_info.appendChild(text);
	    text.addAttribute(new Attribute("x", "" + (left + plotAreaWidth / 2f)));
	    text.addAttribute(new Attribute("y", "" + (pixelHeight - 10)));
	    text.addAttribute(new Attribute("text-anchor", "middle"));
	    text.addAttribute(new Attribute("class", "text textaxislabels"));
	    text.appendChild(xAxis.label);
	    
	    // yLabel
	    text = new Element("text", ns);
	    svg_info.appendChild(text);
	    text.addAttribute(new Attribute("x", "0"));
	    text.addAttribute(new Attribute("y", "0"));
	    text.addAttribute(new Attribute("transform", "translate(30," + (top + plotAreaHeight / 2f) + ") rotate(-90)"));
	    text.addAttribute(new Attribute("text-anchor", "middle"));
	    text.addAttribute(new Attribute("class", "text textaxislabels"));
	    text.appendChild(yAxis.label);
	    
	    
	    // x ticks
	    for (int i=0; i<xAxis.ticks.length; i++)
	    {
	    	float x = getPlotPosition(left, plotAreaWidth, xAxis, xAxis.ticks[i]);
	    	String label = xAxis.getLabel(i);
	    	
	    	addLine(svg_info, x, getYPlotBottom() + plotAreaMargin + tickHalfLength, x, getYPlotBottom() + plotAreaMargin - tickHalfLength, "plotAxisTicks");
		    
	    	if (label != null)
	    	{
			    text = new Element("text", ns);
			    svg_info.appendChild(text);
			    text.addAttribute(new Attribute("x", "" + x));
			    text.addAttribute(new Attribute("y", "" + (getYPlotBottom() + plotAreaMargin + 24)));
			    text.addAttribute(new Attribute("text-anchor", "middle"));
			    text.addAttribute(new Attribute("class", "text " + (xAxis.type == AxisType.Log ? "textticklabelsLog" : "textticklabels")));
			    text.appendChild(label);
	    	}
	    }
	    
	    // y ticks
	    for (int i=0; i<yAxis.ticks.length; i++)
	    {
	    	float y = getPlotPosition(getYPlotBottom(), -plotAreaHeight, yAxis, yAxis.ticks[i]);
	    	String label = yAxis.getLabel(i);
	    	
	    	addLine(svg_info, left - plotAreaMargin - tickHalfLength, y, left - plotAreaMargin + tickHalfLength, y, "plotAxisTicks");
	
	    	if (label != null)
	    	{
			    text = new Element("text", ns);
			    svg_info.appendChild(text);
			    text.addAttribute(new Attribute("x", "" + (left - plotAreaMargin - 12)));
			    text.addAttribute(new Attribute("y", "" + (y + 5)));
			    text.addAttribute(new Attribute("text-anchor", "end"));
			    text.addAttribute(new Attribute("class", "text " + (yAxis.type == AxisType.Log ? "textticklabelsLog" : "textticklabels")));
			    text.appendChild(label);
	    	}

	    }
	}
	
	private int getYPlotBottom()
	{
		return pixelHeight - bottom;
	}
	
	private static final float getPlotPosition(float offset, float width, Axis axis, float value)
	{
		float norm = axis.getNorm(value);
		return offset + width * norm;
	}
	
	private static final float getPlotPositionClipped(float offset, float width, Axis axis, float value)
	{
		float norm = axis.getNorm(value);
		norm = Math.max(0f, Math.min(1f, norm));
		return offset + width * norm;
	}
	
	public static final void addLine(Element svg, float xStart, float yStart, float xEnd, float yEnd, String className)
	{
		Element tick = new Element("path", ns);
		svg.appendChild(tick);
		tick.addAttribute(new Attribute("d", "M" + xStart + " " + yStart + " L" + xEnd + " " + yEnd));
		tick.addAttribute(new Attribute("class", className));
	}
	
	public static final void addRect(Element svg, float x, float y, float width, float height, String className)
	{
		Element rect = new Element("rect", ns);
		svg.appendChild(rect);
		rect.addAttribute(new Attribute("x", "" + x));
		rect.addAttribute(new Attribute("y", "" + y));
		rect.addAttribute(new Attribute("width", "" + width));
		rect.addAttribute(new Attribute("height", "" + height));
		if (className != null) rect.addAttribute(new Attribute("class", className));
	}
	
	public Vector2f getScreenCoord(Vector2f realCoords)
	{
		float xn = xAxis.getNorm(realCoords.x);  //(plot.xData[i]- xAxis.range.min) / (xAxis.range.max - xAxis.range.min);
		float x = left + xn * plotAreaWidth;
		
		float yn = yAxis.getNorm(realCoords.y); //(plot.yData[i] - yAxis.range.min) / (yAxis.range.max - yAxis.range.min);
		float y = top + (1f - yn) * plotAreaHeight;
		return new Vector2f(x, y);
	}
	
	public List<Vector2f> getScreenCoords(Plot plot)
	{
		List<Vector2f> screenCoords = new ArrayList<>();
		for (int i=0; i<plot.xData.length; i++)
		{
			
			float xn = xAxis.getNorm(plot.xData[i]);  //(plot.xData[i]- xAxis.range.min) / (xAxis.range.max - xAxis.range.min);
			float x = left + xn * plotAreaWidth;
			
			float yn = yAxis.getNorm(plot.yData[i]); //(plot.yData[i] - yAxis.range.min) / (yAxis.range.max - yAxis.range.min);
			float y = top + (1f - yn) * plotAreaHeight;
			screenCoords.add(new Vector2f(x, y));
		}
		return screenCoords;
	}
	
//	public float getScreenLengthY(float start, float end)
//	{
//		float yn = yAxis.getNorm(value); //(value - yAxis.range.min) / (yAxis.range.max - yAxis.range.min);
//		return yn * plotAreaHeight;
//	}
	
	public boolean isWithinRanges(float x, float y)
	{
		return xAxis.range.isWithin(x) && yAxis.range.isWithin(y);
	}
	
	int noOfPlots = 0;
	public int noOfPlots()
	{
		return noOfPlots;
	}
	
	public void draw(Plot plot)
	{
		noOfPlots++;
		List<Vector2f> screenCoords = getScreenCoords(plot);
		
		if (plot.props instanceof PlotLineProps)
		{
			PlotLineProps props = (PlotLineProps) plot.props;
			
			int startIndex = 0;
			while (startIndex < screenCoords.size())
			{
				Element path = new Element("path", ns);
				path.addAttribute(new Attribute("clip-path", "url(#" + plotClip + ")"));
				svg_data.appendChild(path);
				if (props.alpha < 1f) path.addAttribute(new Attribute("stroke-opacity", "" + props.alpha));
				
				path.addAttribute(new Attribute("style", "fill:none; stroke:" + props.color.css() + "; stroke-width:" + props.strokeWidth + "px"));
				
				StringBuilder sb = new StringBuilder();
				int index = 0;
				for (; startIndex<screenCoords.size() ; startIndex++)
				{
					Vector2f coords = screenCoords.get(startIndex);
					if (Float.isNaN(coords.y) || Float.isInfinite(coords.y)) 
					{
						startIndex++;
						break; // start new line
					}
					
					sb.append(index == 0 ? "M" : " L");
					sb.append(coords.x + " " + coords.y);
					index++;
				}
				
				path.addAttribute(new Attribute("d", sb.toString()));
			}
		}
		else if (plot.props instanceof PlotDotProps)
		{
			PlotDotProps props = (PlotDotProps) plot.props;
			RgbColor defaultColor = props.color;

			Element g = new Element("g", ns);
			svg_data.appendChild(g);
			g.addAttribute(new Attribute("fill", defaultColor.css()));
			if (props.alpha < 1f) 
			{
				g.addAttribute(new Attribute("fill-opacity", "" + props.alpha));
				g.addAttribute(new Attribute("stroke-opacity", "" + props.alpha));
			}
//			if (props.shape instanceof ErrorBarsShape) g.addAttribute(new Attribute("clip-path", "url(#" + plotClip + ")"));
			
			for (int i=0; i<screenCoords.size(); i++)
			{
				Vector2f s = screenCoords.get(i);
				if (plot.propData != null) 
				{
					props.setPropValue(plot.propData[i]);
				}
				if (!isWithinRanges(plot.xData[i], plot.yData[i])) continue;
						
				if (props.shape instanceof CircleShape)
				{
					Element circle = new Element("circle", ns);
					g.appendChild(circle);
					circle.addAttribute(new Attribute("cx", "" + s.x));
					circle.addAttribute(new Attribute("cy", "" + s.y));
					circle.addAttribute(new Attribute("r", "" + ((CircleShape) props.shape).radius));
					if (!defaultColor.isSame(props.color)) circle.addAttribute(new Attribute("fill", props.color.css()));
				}
				else if (props.shape instanceof RectShape)
				{
					RectShape rs = (RectShape) props.shape;
					
					Element rect = new Element("rect", ns);
					g.appendChild(rect);
					rect.addAttribute(new Attribute("x", "" + (s.x - rs.radiusX)));
					rect.addAttribute(new Attribute("y", "" + (s.y - rs.radiusY)));
					rect.addAttribute(new Attribute("width", "" + (rs.radiusX*2f)));
					rect.addAttribute(new Attribute("height", "" + (rs.radiusY*2f)));
					if (!defaultColor.isSame(props.color)) rect.addAttribute(new Attribute("fill", props.color.css()));
				}
				else if (props.shape instanceof ErrorBarsShape)
				{
					float usd = getPlotPositionClipped(getYPlotBottom(), -plotAreaHeight, yAxis, plot.yData[i] + plot.propData[i]);
					float lsd = getPlotPositionClipped(getYPlotBottom(), -plotAreaHeight, yAxis, plot.yData[i] - plot.propData[i]);
					
					Element line = new Element("path", ns);
					g.appendChild(line);
					line.addAttribute(new Attribute("d", "M" + s.x + " " + lsd + " L" + s.x + " " + usd));
					line.addAttribute(new Attribute("style", "fill:none; stroke:" + props.color.css() + "; stroke-width:1px"));
					
					float w = 12f;
					float h = 2f;
					float x = s.x - w/2f;
					float y = s.y - h/2f;
					if (y >= top && y <= getYPlotBottom()) addRect(g, x, y, w, h, null);
					addRect(g, x, lsd, w, h, null);
					addRect(g, x, usd, w, h, null);
				}
				else if (props.shape instanceof VerticalBarShape)
				{
					VerticalBarShape v = (VerticalBarShape) props.shape;
					
					Vector2f zero = getScreenCoord(new Vector2f(plot.xData[i], v.zero));
					float height = zero.y - s.y;
					
					Element rect = new Element("rect", ns);
					g.appendChild(rect);
					rect.addAttribute(new Attribute("x", "" + (s.x + v.offsetX - v.width/2)));
					rect.addAttribute(new Attribute("y", "" + s.y));
					rect.addAttribute(new Attribute("width", "" + v.width));
					rect.addAttribute(new Attribute("height", "" + height));
					if (!defaultColor.isSame(props.color)) rect.addAttribute(new Attribute("fill", props.color.css()));
				}
				else if (props.shape instanceof FreeShape)
				{
					FreeShape fshape = (FreeShape) props.shape;
					
					Element path = new Element("path", ns);
					svg_data.appendChild(path);

					path.addAttribute(new Attribute("style", "fill:none; stroke:" + props.color.css() + "; stroke-width:" + fshape.strokeWidth + "px"));
					path.addAttribute(new Attribute("d", fshape.path));
					path.addAttribute(new Attribute("transform", "translate(" + s.x + "," + s.y + ") scale(" + fshape.radius + ")"));
				}
			}
			
			props.color = defaultColor;
		}
		else if (plot.props instanceof PlotCustomProps)
		{
			PlotCustomProps props = (PlotCustomProps) plot.props;
			
			for (int i=0; i<screenCoords.size(); i++)
			{
				Vector2f s = screenCoords.get(i);
				
				props.dataPointPlotter.plot(i, s, svg_data, ns);
			}
		}
		
		addPlotLabel(plot.label, plot.props);
	}
	
	public void drawLine(Vector2f fromReal, Vector2f toReal, String style)
	{
		Vector2f fromScreen = getScreenCoord(fromReal);
		Vector2f toScreen = getScreenCoord(toReal);
		Element line = new Element("path", ns);
		svg_data.appendChild(line);
		line.addAttribute(new Attribute("d", "M" + fromScreen.x + " " + fromScreen.y + " L" + toScreen.x + " " + toScreen.y));
		line.addAttribute(new Attribute("style", style));
	}
	
	
	
	public void addPlotLabel(String label, PlotProps props_)
	{
		if (usePlotLabels && label != null)
		{
			plotLabelCount++;
			
			float x = pixelWidth - right + plotAreaMargin + 12;
			float y = top + plotLabelCount * 32 - 3;
			float cx = x + 10;
			
			float cy = y - 6;
			
			if (props_ instanceof PlotLineProps)
			{
				PlotLineProps props = (PlotLineProps) props_;
				
				Element path = new Element("path", ns);
				svg_info.appendChild(path);
				if (props.alpha < 1f) path.addAttribute(new Attribute("stroke-opacity", "" + props.alpha));
				path.addAttribute(new Attribute("style", "fill:none; stroke:" + props.color.css() + "; stroke-width:2px"));	
				path.addAttribute(new Attribute("d", "M" + x + " " + cy + " L" + (x+20) + " " + cy));
			}
			else if (props_ instanceof PlotDotProps)
			{
				PlotDotProps props = (PlotDotProps) props_;

				if (props.shape instanceof CircleShape)
				{
					Element circle = new Element("circle", ns);
					svg_info.appendChild(circle);
					circle.addAttribute(new Attribute("cx", "" + cx));
					circle.addAttribute(new Attribute("cy", "" + cy));
					circle.addAttribute(new Attribute("r", "" + ((CircleShape) props.shape).radius));
					circle.addAttribute(new Attribute("style", "stroke:none; fill:" + props.color.css()));	
					if (props.alpha < 1f) circle.addAttribute(new Attribute("fill-opacity", "" + props.alpha));
				}
				else if (props.shape instanceof RectShape)
				{
					RectShape rs = (RectShape) props.shape;
					
					float radiusX = Math.min(10, rs.radiusX);
					
					Element rect = new Element("rect", ns);
					svg_info.appendChild(rect);
					rect.addAttribute(new Attribute("x", "" + (cx - radiusX)));
					rect.addAttribute(new Attribute("y", "" + (cy - rs.radiusY)));
					rect.addAttribute(new Attribute("width", "" + (radiusX*2f)));
					rect.addAttribute(new Attribute("height", "" + (rs.radiusY*2f)));
					rect.addAttribute(new Attribute("style", "stroke:none; fill:" + props.color.css()));
					if (props.alpha < 1f) rect.addAttribute(new Attribute("fill-opacity", "" + props.alpha));
				}
				else if (props.shape instanceof VerticalBarShape)
				{
					VerticalBarShape v = (VerticalBarShape) props.shape;
					
					Element rect = new Element("rect", ns);
					svg_info.appendChild(rect);
					rect.addAttribute(new Attribute("x", "" + (cx - 10)));
					rect.addAttribute(new Attribute("y", "" + (cy - v.width/2)));
					rect.addAttribute(new Attribute("width", "" + 20));
					rect.addAttribute(new Attribute("height", "" + v.width));
					
					rect.addAttribute(new Attribute("style", "stroke:none; fill:" + props.color.css()));
					if (props.alpha < 1f) rect.addAttribute(new Attribute("fill-opacity", "" + props.alpha));
				}
				else if (props.shape instanceof FreeShape)
				{
					FreeShape fshape = (FreeShape) props.shape;
					
					Element path = new Element("path", ns);
					svg_info.appendChild(path);

					path.addAttribute(new Attribute("style", "fill:none; stroke:" + props.color.css() + "; stroke-width:" + fshape.strokeWidth + "px"));
					path.addAttribute(new Attribute("d", fshape.path));
					path.addAttribute(new Attribute("transform", "translate(" + cx + "," + cy + ") scale(" + fshape.radius + ")"));
				}
			}
		    
		    Element text = new Element("text", ns);
		    svg_info.appendChild(text);
		    text.addAttribute(new Attribute("x", "" + (x + 25)));
		    text.addAttribute(new Attribute("y", "" + (y)));
		    text.addAttribute(new Attribute("class", "text textlegend"));
		    text.appendChild(label);
		}
	}
	
	public void drawFill(Plot plotLower, Plot plotUpper, RgbColor fillColor, float alpha)
	{
		List<Vector2f> screenCoords = getScreenCoords(plotUpper);
		List<Vector2f> screenCoords2 = getScreenCoords(plotLower);
		Collections.reverse(screenCoords2);
		screenCoords.addAll(screenCoords2);
		
		Element path = new Element("path", ns);
		svg_data.appendChild(path);
		path.addAttribute(new Attribute("clip-path", "url(#" + plotClip + ")"));
		
		path.addAttribute(new Attribute("style", "fill:" + fillColor.css() + "; stroke:none"));
		if (alpha < 1f) path.addAttribute(new Attribute("fill-opacity", "" + alpha));
		
		StringBuilder sb = new StringBuilder();
		int index = 0;
		for (int i=0; i<screenCoords.size() ; i++)
		{
			Vector2f coords = screenCoords.get(i);
			if (Float.isNaN(coords.y)) continue;
			
			sb.append(index == 0 ? "M" : " L");
			sb.append(coords.x + " " + coords.y);
			index++;
		}
		sb.append(" Z");
		
		path.addAttribute(new Attribute("d", sb.toString()));
	}
	
	public static void saveSVG(Element svg, File svgFile) throws IOException
	{
		saveSVG(svg, svgFile, true, false);
	}
	
	
	static ToolExecuter inkscape = new ToolExecuter(new File(ToolExecuter.Inkscape_MacOS));
	public static void saveSVG(Element svg, File svgFile, boolean pdf, boolean png) throws IOException
	{
	    FileOutputStream fos = new FileOutputStream(svgFile);
        Serializer serializer = new Serializer(fos, "UTF-8");
        serializer.setIndent(4);
        
        Document doc = svg.getDocument();
        if (doc == null) doc = new Document(svg);
        serializer.write(doc);
        serializer.flush();
        fos.close();
        
        long time = System.currentTimeMillis();
        String svgFilePath = svgFile.getAbsolutePath();
        
        if (pdf)
        {
            String pdfFilePath = new File(svgFile.getParentFile(), svgFile.getName().substring(0, svgFile.getName().lastIndexOf(".")) + ".pdf").getAbsolutePath();
            inkscape.exec("z -D --file=" + svgFilePath + " --export-pdf=" + pdfFilePath);
            System.out.println("Exporting to " + pdfFilePath + " took " + (System.currentTimeMillis() - time) + " ms.");
        }
        if (png)
        {
            String pngFilePath = new File(svgFile.getParentFile(), svgFile.getName().substring(0, svgFile.getName().lastIndexOf(".")) + ".png").getAbsolutePath();
            inkscape.exec("z -D --file=" + svgFilePath + " --export-png=" + pngFilePath);
        }
	}
	
	
	public void save(File svgFile) throws IOException
	{
		saveSVG(svg, svgFile);
	}
	
	public void save_RasterPlotData(File svgFile) throws Exception
	{
		//Element image = new Element("image", ns);
		//svg.appendChild(image);
		//<image id="image1PNG" x="240" y="150" width="240" height="150" xlink:href="data:image/png;base64,__here__"/>
		
		// implement here
		
		// raster plot data only
		svg.removeChild(svg_info);
		saveSVG(svg, svgFile, false, false);
		String svgFilePath = svgFile.getAbsolutePath();
		File pngFile = new File(svgFile.getParentFile(), svgFile.getName().substring(0, svgFile.getName().lastIndexOf(".")) + ".png");
        inkscape.exec("z -D --file=" + svgFilePath + " --export-area-page --export-png=" + pngFile.getAbsolutePath());
        String stringData = new String(Base64.encodeBase64(  BinaryFileUtil.getFileData(pngFile)), "UTF-8");
        
        pngFile.delete();
    
        // create embbedded image
        Element img = new Element("image", ns);
        img.addAttribute(new Attribute("x", "0"));
        img.addAttribute(new Attribute("y", "0"));
        img.addAttribute(new Attribute("width", pixelWidth + "px"));
        img.addAttribute(new Attribute("height", pixelHeight + "px"));
        img.addAttribute(new Attribute("xlink:href", nsxlink, "data:image/png;base64," + stringData));
        
        svg.replaceChild(svg_data, img); // replace vector with image
        svg.appendChild(svg_info); // add other elements again
		
		save(svgFile);
	}
}


