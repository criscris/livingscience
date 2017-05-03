package utils.plot;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.List;

import utils.image.BgrByteImage;
import utils.image.RgbColor;
import utils.math.Vector2f;

public class PlotCanvas
{
	public BgrByteImage image;
	Graphics2D g;
	
	final int left = 110;
	final int right;
	final int top = 10;
	final int bottom = 70;
	final int plotAreaWidth;
	final int plotAreaHeight;
	
	final int tickHalfLength = 4;
	
	Axis xAxis;
	Axis yAxis;
	
	boolean usePlotLabels;
	int plotLabelCount = 0;
	
	public PlotCanvas(int pixelWidth, int pixelHeight, Axis xAxis, Axis yAxis, boolean usePlotLabels)
	{
		this.usePlotLabels = usePlotLabels;
		right = usePlotLabels ? 200 : 30;
		
		image = new BgrByteImage(pixelWidth, pixelHeight);
		image.setAll(new RgbColor(1f, 1f, 1f));
		
		this.xAxis = xAxis;
		this.yAxis = yAxis;
		
		g = image.getBufferedImage().createGraphics();
		
		plotAreaWidth = image.getWidth() - left - right;
		plotAreaHeight = image.getHeight() - top - bottom;
		
		g.setColor(Color.BLACK);
		g.drawRect(left, top, plotAreaWidth, plotAreaHeight);
		
		Font font = new Font("Arial", Font.PLAIN, 18);
		FontRenderContext fontContext = g.getFontRenderContext();
	    g.setFont(font);
	    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
	    
	    // xlabel
	    float xLabelWidth = (float) font.getStringBounds(xAxis.label, fontContext).getWidth();
	    float xlabelStartX = left + (plotAreaWidth - xLabelWidth) / 2f;
	    
	    g.drawString(xAxis.label, xlabelStartX, image.getHeight() - 10);
	    
	    // ylabel
	    
	    AffineTransform orig = g.getTransform();
	   
	    float yLabelWidth = (float) font.getStringBounds(yAxis.label, fontContext).getWidth();
	    float ylabelStartY = top + (plotAreaHeight + yLabelWidth) / 2f;
	    g.translate(30, (int) ylabelStartY);
	    g.rotate(-Math.PI/2);
	    
	    g.drawString(yAxis.label, 0, 0);
	    
	    g.setTransform(orig);
	    
	    
	    // ticks
		font = new Font("Arial", Font.PLAIN, 14);
		fontContext = g.getFontRenderContext();
	    g.setFont(font);
	    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
	    
	    // x ticks
	    for (int i=0; i<xAxis.ticks.length; i++)
	    {
	    	float x = getPlotPosition(left, plotAreaWidth, xAxis.range, xAxis.ticks[i]);
	    	String label = xAxis.getLabel(i);
	    	
	    	int xi = Math.round(x);
	    	g.drawLine(xi, getYPlotBottom() + tickHalfLength, xi, getYPlotBottom() - tickHalfLength);
	    	
		    float tickLabelWidth = (float) font.getStringBounds(label, fontContext).getWidth();
		    float tickLabelStartX = x - (tickLabelWidth / 2f);
		    
		    g.drawString(label, tickLabelStartX, getYPlotBottom() + 24);
	    }
	    
	    // y ticks
	    for (int i=0; i<yAxis.ticks.length; i++)
	    {
	    	float y = getPlotPosition(getYPlotBottom(), -plotAreaHeight, yAxis.range, yAxis.ticks[i]);
	    	String label = yAxis.getLabel(i);
	    	
	    	int yi = Math.round(y);
	    	g.drawLine(left - tickHalfLength, yi, left + tickHalfLength, yi);
	    	
	    	float tickLabelWidth = (float) font.getStringBounds(label, fontContext).getWidth();
	    	float tickLabelStartX = left - 12 - tickLabelWidth;
	    	
	    	g.drawString(label, tickLabelStartX, yi + 6);
	    }
	}
	
	private int getYPlotBottom()
	{
		return image.getHeight() - bottom;
	}
	
	private static final float getPlotPosition(float offset, float width, Range range, float value)
	{
		float norm = (value - range.min) / (range.max - range.min);
		return offset + width * norm;
	}
	
	public List<Vector2f> getScreenCoords(Plot plot)
	{
		List<Vector2f> screenCoords = new ArrayList<>();
		for (int i=0; i<plot.xData.length; i++)
		{
			
			float xn = (plot.xData[i] - xAxis.range.min) / (xAxis.range.max - xAxis.range.min);
			float x = left + xn * plotAreaWidth;
			
			float yn = (plot.yData[i] - yAxis.range.min) / (yAxis.range.max - yAxis.range.min);
			float y = top + (1f - yn) * plotAreaHeight;
			screenCoords.add(new Vector2f(x, y));
		}
		return screenCoords;
	}
	
	
	public void draw(Plot plot)
	{
		List<Vector2f> screenCoords = getScreenCoords(plot);
		
		if (plot.props instanceof PlotLineProps)
		{
			PlotLineProps props = (PlotLineProps) plot.props;
			
			for (int i=0; i<screenCoords.size() - 1; i++)
			{
				Vector2f start = screenCoords.get(i);
				Vector2f end = screenCoords.get(i+1);
				
				image.drawLine(Math.round(start.x), Math.round(start.y), Math.round(end.x), Math.round(end.y), props.color);
			}
		}
		else if (plot.props instanceof PlotDotProps)
		{
			PlotDotProps props = (PlotDotProps) plot.props;
			RgbColor temp = new RgbColor();
			
			for (int i=0; i<screenCoords.size(); i++)
			{
				Vector2f s = screenCoords.get(i);
				if (plot.propData != null) 
				{
					props.setPropValue(plot.propData[i]);
//					System.out.println(plot.propData[i] + " " + props.color.getR() + " " + props.color.getG() + " " + props.color.getB());
				}
				
				int x = Math.round(s.x);
				int y = Math.round(s.y);
				for (Dot dot : props.dots)
				{
					float a = props.alpha * dot.alpha;
					if (a != 1f) image.set(x + dot.xd, y + dot.yd, props.color, a, temp); 
					else image.set(x + dot.xd, y + dot.yd, props.color);
				}
				

			}
		}
		
		addPlotLabel(plot);
	}
	
	public void drawFill(Plot plotLower, Plot plotUpper, RgbColor fillColor, float alpha)
	{
		// generate a white mask for the fill area
		List<Vector2f> ps0 = getScreenCoords(plotUpper);
		List<Vector2f> ps1 = getScreenCoords(plotLower);
		
		int xMin = Math.round(ps0.get(0).x);
		int xMax = Math.round(ps0.get(ps0.size() - 1).x);
		xMin = Math.max(left, Math.min(left + plotAreaWidth, xMin));
		xMax = Math.max(left, Math.min(left + plotAreaWidth, xMax));
		
		BgrByteImage fillArea = new BgrByteImage(image.getWidth(), image.getHeight());
		RgbColor white = new RgbColor(1f, 1f, 1f);
		
		int currentNode = 1;
		for (int x=xMin; x<=xMax; x++)
		{
			Vector2f v1 = ps0.get(currentNode);
			while (v1.x < x)
			{
				if (currentNode == ps0.size() - 1) break;
				currentNode++;
				v1 = ps0.get(currentNode);
			}
			Vector2f v0 = ps0.get(currentNode - 1);
			float blend = (x - v0.x) / (v1.x - v0.x);
			
			float y0 = v0.y * (1f - blend) + v1.y * blend;
			float y1 = ps1.get(currentNode - 1).y * (1f - blend) + ps1.get(currentNode).y * blend;
			
			int yLow = Math.round(Math.min(y0, y1));
			yLow = Math.max(top, Math.min(top + plotAreaHeight, yLow));
			
			int yHigh = Math.round(Math.max(y0, y1));
			yHigh = Math.max(top, Math.min(top + plotAreaHeight, yHigh));
			
			for (int y=yLow; y<=yHigh; y++)
			{
				fillArea.set(x, y, white);
			}
		}
		
		// draw fill area on plot canvas
		int size = image.getWidth() * image.getHeight();
		RgbColor temp = new RgbColor();
		for (int i=0; i<size; i++)
		{
			fillArea.getColor(i, temp);
			if (temp.getR() > 0.9f)
			{
				image.set(i, fillColor, alpha, temp);
			}
		}
	}
	
	public void addPlotLabel(Plot plot)
	{
		if (usePlotLabels && plot.label != null)
		{
			plotLabelCount++;
			
			float x = image.getWidth() - right + 12;
			float y = top + plotLabelCount * 20 - 3;
			
			float cy = y - 6;
			
			if (plot.props instanceof PlotLineProps)
			{
				PlotLineProps props = (PlotLineProps) plot.props;
				image.drawLine((int) x, (int) cy, (int) x + 20, (int) cy, props.color);
			}
			else if (plot.props instanceof PlotDotProps)
			{
				PlotDotProps props = (PlotDotProps) plot.props;
				RgbColor temp = new RgbColor();
				
				int xi = Math.round(x + 10);
				int yi = Math.round(cy);
				for (Dot dot : props.dots)
				{
					float a = props.alpha * dot.alpha;
					if (a != 1f) image.set(xi + dot.xd, yi + dot.yd, props.color, a, temp); 
					else image.set(xi + dot.xd, yi + dot.yd, props.color);
				}
			}
			
			
			Font font = new Font("Arial", Font.PLAIN, 14);
		    g.setFont(font);
		    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		    g.drawString(plot.label, x + 25, y);
		}
	}
}
