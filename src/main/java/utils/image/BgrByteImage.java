package utils.image;

import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.imageio.ImageIO;

import utils.math.Matrix3f;
import utils.math.Vector2f;
import utils.math.Vector3f;

public class BgrByteImage implements Serializable
{
	private static final long serialVersionUID = 4501103306863434778L;
	
	private transient BufferedImage image;
	private int width;
	private int height;
	public byte data[];
	
	public BgrByteImage(int width, int height)
	{
		this.width = width;
		this.height = height;
		image = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
		Raster r = image.getRaster();
		DataBufferByte dbi = (DataBufferByte) r.getDataBuffer();
		data = dbi.getData();
	}
	
	public BgrByteImage(BgrByteImage source)
	{
		this.width = source.width;
		this.height = source.height;
		image = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
		Raster r = image.getRaster();
		DataBufferByte dbi = (DataBufferByte) r.getDataBuffer();
		data = dbi.getData();
		set(source);
	}
	
	public void setRGB(ByteBuffer buffer)
	{
		int length = width * height * 3;
		for (int i=0; i<length; i+=3)
		{	
			data[i+2] = buffer.get();
			data[i+1] = buffer.get();
            data[i] = buffer.get();       
		}
		buffer.rewind();
	}
	
	public BgrByteImage(File file) throws Exception
	{
		image = ImageIO.read(file);
		checkBGRImage(image, BufferedImage.TYPE_3BYTE_BGR, "TYPE_3BYTE_BGR");
		Raster r = image.getRaster();
		DataBufferByte dbi = (DataBufferByte) r.getDataBuffer();
		data = dbi.getData();
		
		width = image.getWidth();
		height = image.getHeight();
	}
	
	public static BgrByteImage load_4BYTE_ABGR(RgbColor background, File file) throws Exception
	{
		BufferedImage abgr = ImageIO.read(file);
		
		BgrByteImage bgr = new BgrByteImage(abgr.getWidth(), abgr.getHeight());
		
		RgbColor color = new RgbColor();
		RgbColor blend = new RgbColor();
		for (int y=0; y<bgr.height; y++)
		{
			for (int x=0; x<bgr.width; x++)
			{
				int c = abgr.getRGB(x, y);
				int a = (c>>24) & 0xff;
				int  r = (c & 0x00ff0000) >> 16;
				int  g = (c & 0x0000ff00) >> 8;
				int  b = c & 0x000000ff;
				color.set((float) r / 255f, (float) g / 255f, (float) b / 255f);
				
				blend.blend(color, background, (float) a / 255f);
				
				bgr.set(x, y, blend);
			}
		}
		
		return bgr;
	}
	
	
//	public BgrByteImage createScaled(int newWidth, int newHeight)
//	{
//		BgrByteImage destImage = new BgrByteImage(newWidth, newHeight);
//		
//		IplImage frame = IplImage.createFrom(getBufferedImage());
//		IplImage sframe = IplImage.createFrom(destImage.getBufferedImage());
//		opencv_imgproc.cvResize(frame, sframe, opencv_imgproc.CV_INTER_CUBIC);
//		sframe.copyTo(destImage.getBufferedImage());
//		
//		return destImage;
//	}
	
	public BgrByteImage createScaled(int newWidth, int newHeight) throws Exception
	{
		BufferedImage after = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_3BYTE_BGR);
		AffineTransform at = new AffineTransform();
		at.scale((float) newWidth / width, (float) newHeight / height);
		AffineTransformOp scaleOp = new AffineTransformOp(at, AffineTransformOp.TYPE_BILINEAR);
		return new BgrByteImage(scaleOp.filter(getBufferedImage(), after));
	}
	
	public BgrByteImage(BufferedImage image) throws Exception
	{
		this.image = image;
		checkBGRImage(image, BufferedImage.TYPE_3BYTE_BGR, "TYPE_3BYTE_BGR");
		Raster r = image.getRaster();
		DataBufferByte dbi = (DataBufferByte) r.getDataBuffer();
		data = dbi.getData();
		
		width = image.getWidth();
		height = image.getHeight();
	}
	
	public BgrByteImage(InputStream is) throws Exception
	{
		image = ImageIO.read(is);
		is.close();
		checkBGRImage(image, BufferedImage.TYPE_3BYTE_BGR, "TYPE_3BYTE_BGR");
		Raster r = image.getRaster();
		DataBufferByte dbi = (DataBufferByte) r.getDataBuffer();
		data = dbi.getData();
		
		width = image.getWidth();
		height = image.getHeight();
	}
	
	public BufferedImage getBufferedImage()
	{
		return image;
	}
	
	public final int getWidth() 
	{
		return width;
	}

	public final int getHeight() 
	{
		return height;
	}
	
	public void reset()
	{
		byte zero = (byte) 0;
		for (int i=0; i<data.length; i++)
		{
			data[i] = zero;
		}
	}
	
	public void setAll(RgbColor color)
	{
		int pixels = width * height;
		for (int i=0; i<pixels; i++)
		{
			set(i, color);
		}
	}
	
	public void set(BgrByteImage image)
	{
		for (int i=0; i<data.length; i++)
		{
			data[i] = image.data[i];
		}
	}
	
	public void draw(BgrByteImage source, int xSource, int ySource, int xTarget, int yTarget, int width, int height)
	{
		RgbColor color = new RgbColor();
		for (int y=0; y<height; y++)
		{
			for (int x=0; x<width; x++)
			{
				source.getColor(xSource + x, ySource + y, color);
				set(xTarget + x, yTarget + y, color);
			}
		}
	}
	
	public boolean compare(BgrByteImage other, int xOther, int yOther, int xSelf, int ySelf, int width, int height, float colorEpsilon)
	{
		RgbColor color1 = new RgbColor();
		RgbColor color2 = new RgbColor();
		for (int y=0; y<height; y++)
		{
			for (int x=0; x<width; x++)
			{
				other.getColor(xOther + x, yOther + y, color1);
				getColor(xSelf + x, ySelf + y, color2);
				
				if (!color1.isSame(color2, colorEpsilon))
				{
					return false;
				}
			}
		}
		
		return true;
	}
	
	public int compareCount(BgrByteImage other, int xOther, int yOther, int xSelf, int ySelf, int width, int height, float colorEpsilon)
	{
		int count = 0;
		RgbColor color1 = new RgbColor();
		RgbColor color2 = new RgbColor();
		for (int y=0; y<height; y++)
		{
			for (int x=0; x<width; x++)
			{
				other.getColor(xOther + x, yOther + y, color1);
				getColor(xSelf + x, ySelf + y, color2);
				
				if (color1.isSame(color2, colorEpsilon))
				{
					count++;
				}
			}
		}
		
		return count;
	}

	public static void checkBGRImage(BufferedImage image, int requiredType, String typeString) throws Exception
	{
		if (image.getType() != requiredType)
		{
			String type = "unknown";
			switch (image.getType())
			{
				case BufferedImage.TYPE_3BYTE_BGR: type = "TYPE_3BYTE_BGR"; break;
				case BufferedImage.TYPE_4BYTE_ABGR : type = "TYPE_4BYTE_ABGR"; break;
				case BufferedImage.TYPE_4BYTE_ABGR_PRE : type = "TYPE_4BYTE_ABGR_PRE"; break;
				case BufferedImage.TYPE_BYTE_BINARY : type = "TYPE_BYTE_BINARY"; break;
				case BufferedImage.TYPE_BYTE_GRAY : type = "TYPE_BYTE_GRAY"; break;
				case BufferedImage.TYPE_BYTE_INDEXED : type = "TYPE_BYTE_INDEXED"; break;
				case BufferedImage.TYPE_CUSTOM : type = "TYPE_CUSTOM"; break;
				case BufferedImage.TYPE_INT_ARGB : type = "TYPE_INT_ARGB"; break;
				case BufferedImage.TYPE_INT_ARGB_PRE : type = "TYPE_INT_ARGB_PRE"; break;
				case BufferedImage.TYPE_INT_BGR : type = "TYPE_INT_BGR"; break;
				case BufferedImage.TYPE_INT_RGB : type = "TYPE_INT_RGB"; break;
				case BufferedImage.TYPE_USHORT_555_RGB : type = "TYPE_USHORT_555_RGB"; break;
				case BufferedImage.TYPE_USHORT_565_RGB : type = "TYPE_USHORT_565_RGB"; break;
				case BufferedImage.TYPE_USHORT_GRAY : type = "TYPE_USHORT_GRAY"; break;
			}
			
			throw new Exception("Not supported image type: " + typeString + ". Your image type is " + type + ".");
		}
	}
	
	public final void set(int x, int y, RgbColor color)
	{
		if (x < 0 || y < 0 || x >= width || y >= height) return;
		int index3 = (y*width + x)*3;
		color.fillColorBgr(data, index3);
	}
	
	public final void set(int x, int y, RgbColor color, float blend, RgbColor temp)
	{
		getColor(x, y, temp);
		temp.blend(color, blend);
		set(x, y, temp);
	}
	
	public final void set(int index, RgbColor color, float blend, RgbColor temp)
	{
		getColor(index, temp);
		temp.blend(color, blend);
		set(index, temp);
	}
	
	public final void set(int index, RgbColor color)
	{
		color.fillColorBgr(data, index * 3);
	}
	
	public final void set(float x, float y, RgbColor color)
	{
		int xi = (int) x;
		int yi = (int) y;
		set(xi, yi, color);
	}
	
	private static final int[] neigbors3x3_x = new int[] { -1,  0,  1, -1, 1, -1, 0, 1};
	private static final int[] neigbors3x3_y = new int[] { -1, -1, -1,  0, 0,  1, 1, 1};
	public final void set3x3(int x, int y, RgbColor color)
	{
		if (x < 1 || y < 1 || x >= width - 1 || y >= height - 1) return;
		for (int i=0; i<neigbors3x3_x.length; i++)
		{
			set(x + neigbors3x3_x[i], y + neigbors3x3_y[i], color);
		}
	}
	
	public final void set(int x, int y, int radius, RgbColor color, float blend, RgbColor temp)
	{
		int minY = Math.max(0, y - radius);
		int maxY = Math.min(height - 1, y + radius);
		int minX = Math.max(0, x - radius);
		int maxX = Math.min(width - 1, x + radius);
		
		for (int yi=minY; yi<=maxY; yi++)
		{
			for (int xi=minX; xi<=maxX; xi++)
			{
				getColor(xi, yi, temp);
				temp.blend(color, blend);
				set(xi, yi, temp);
			}
		}
	}
	

	public final void set(int x, int y, int radius, RgbColor color)
	{
		int minY = Math.max(0, y - radius);
		int maxY = Math.min(height - 1, y + radius);
		int minX = Math.max(0, x - radius);
		int maxX = Math.min(width - 1, x + radius);
		
		for (int yi=minY; yi<=maxY; yi++)
		{
			for (int xi=minX; xi<=maxX; xi++)
			{
				set(xi, yi, color);
			}
		}
	}
	
	private static final int[] neigbors2x2_x = new int[] { 0,  1,  0,  1};
	private static final int[] neigbors2x2_y = new int[] { 0,  0,  1,  1};
	public final void set2x2(int x, int y, RgbColor color)
	{
		if (x < 0 || y < 0 || x >= width - 1 || y >= height - 1) return;
		for (int i=0; i<neigbors2x2_x.length; i++)
		{
			set(x + neigbors2x2_x[i], y + neigbors2x2_y[i], color);
		}
	}
	
	public void getColor(int x, int y, RgbColor colorOut)
	{
		x = Math.max(0, Math.min(width - 1, x));
		y = Math.max(0, Math.min(height - 1, y));
		int index3 = (y*width + x)*3;
		colorOut.setBgr(data, index3);
	}
	
	public final void getColor(int index, RgbColor colorOut)
	{
		colorOut.setBgr(data, index * 3);
	}
	
	public final void floodfill(int startX, int startY, RgbColor fillColor, RgbColor floodColor)
	{
		List<Integer> sx = new ArrayList<>();
		sx.add(startX);
		List<Integer> sy = new ArrayList<>();
		sy.add(startY);
		
		floodfill(sx, sy, fillColor, floodColor);
	}
	
	public final void floodfill(List<Integer> startX, List<Integer> startY, RgbColor fillColor, RgbColor floodColor)
	{
		Set<Integer> connectedPixels = new HashSet<>();
		for (int i=0; i<startX.size(); i++)
		{
			int index = startY.get(i)*width + startX.get(i);
			connectedPixels.add(index);
		}

		RgbColor testColor = new RgbColor();
		int[] neighborOffsetsX = new int[] { 0, -1, 1, 0 };
		int[] neighborOffsetsY = new int[] { -1, 0, 0, 1 };
		
		Set<Integer> newNeighbors = new HashSet<Integer>(connectedPixels);
		while (newNeighbors.size() > 0)
		{
			Set<Integer> evenNewerNeighbors = new HashSet<Integer>();
			for (Integer sn : newNeighbors)
			{
				int snX = sn % width;
				int snY = sn / width;
				for (int i=0; i<neighborOffsetsX.length; i++)
				{
					int newx = snX + neighborOffsetsX[i];
					int newy = snY + neighborOffsetsY[i];
					if (newx < 0 || newy < 0 || newx >= width || newy >= height) continue;

					int newIndex = newy*width + newx;
					getColor(newIndex, testColor);
					
					if (testColor.isSame(floodColor, 0.03f)) // || (leakColor != null && testColor.isSame(leakColor, 0.05f)))
					{
						if (connectedPixels.add(newIndex))
						{
							evenNewerNeighbors.add(newIndex);
						}
					}
				}
			}
			
			newNeighbors = evenNewerNeighbors;
		}
		
		for (Integer i : connectedPixels)
		{
			set(i, fillColor);
		}
	}
	
	public final void drawLine(int fromX, int fromY, int toX, int toY, RgbColor color)
	{
		toX -= fromX;
		toY -= fromY;
		
		float realX = 0f;
		float realY = 0f;

		int xDir = toX;
		if (xDir != 0) xDir /= Math.abs(toX);
		
		int yDir = toY;
		if (yDir != 0) yDir /= Math.abs(toY);
		
		float m = toX == 0 ? Float.MAX_VALUE : (float) toY / toX;
		
		float realStepX = 0f;
		float realStepY = 0f;
		int noOfSteps = 0;
		
		if (m <= 1f && m >= -1f)
		{
			realStepX = xDir;
			realStepY = m * xDir;
			noOfSteps = Math.abs(toX);
		}
		else
		{
			realStepX = 1f/m * yDir;
			realStepY = yDir;
			noOfSteps = Math.abs(toY);
		}
		
		int steps = 0;
		while (steps < noOfSteps)
		{
			// move
			realX += realStepX;
			realY += realStepY;
			
			steps++;
			
			// check joint
			set(fromX + realX, fromY + realY, color);
		}
	}
	
	public final void drawLine(int fromX, int fromY, int toX, int toY, RgbColor color, float alpha, RgbColor temp)
	{
		toX -= fromX;
		toY -= fromY;
		
		float realX = 0f;
		float realY = 0f;

		int xDir = toX;
		if (xDir != 0) xDir /= Math.abs(toX);
		
		int yDir = toY;
		if (yDir != 0) yDir /= Math.abs(toY);
		
		float m = toX == 0 ? Float.MAX_VALUE : (float) toY / toX;
		
		float realStepX = 0f;
		float realStepY = 0f;
		int noOfSteps = 0;
		
		if (m <= 1f && m >= -1f)
		{
			realStepX = xDir;
			realStepY = m * xDir;
			noOfSteps = Math.abs(toX);
		}
		else
		{
			realStepX = 1f/m * yDir;
			realStepY = yDir;
			noOfSteps = Math.abs(toY);
		}
		
		int steps = 0;
		while (steps < noOfSteps)
		{
			// move
			realX += realStepX;
			realY += realStepY;
			
			steps++;
			
			// check joint
			set(Math.round(fromX + realX), Math.round(fromY + realY), color, alpha, temp);
		}
	}
	
	public final void rasterTexturedQuadBilinearly(TexturedQuad quad)
	{
		rasterTriangle(quad.v1, quad.v3, quad.v4, quad.texture, null);
		rasterTriangle(quad.v1, quad.v2, quad.v3, quad.texture, null);
	}
	
	private RgbColor col1, col2, col3, col4;
	private final void setBilinear(int x, int y, Vector2f texCoords, BgrByteImage texture)
	{
		if (col1 == null)
		{
			col1 = new RgbColor(0f, 0f, 0f);
			col2 = new RgbColor(0f, 0f, 0f);
			col3 = new RgbColor(0f, 0f, 0f);
			col4 = new RgbColor(0f, 0f, 0f);
		}
		
		int xL = (int) Math.floor(texCoords.x);
		int xR = (int) Math.ceil(texCoords.x);
		int yU = (int) Math.floor(texCoords.y);
		int yB = (int) Math.ceil(texCoords.y);
		float weightRight = texCoords.x - xL;
		float weightBottom = texCoords.y - yU;
	
		texture.getColor(xL, yU, col1);
		texture.getColor(xR, yU, col2);
		col3.blend(col2, col1, weightRight);
		
		texture.getColor(xL, yB, col1);
		texture.getColor(xR, yB, col2);
		col4.blend(col2, col1, weightRight);
		
		col1.blend(col4, col3, weightBottom);
		
		set(x, y, col4);
	}
	
//	private RgbColor debugCol = new RgbColor(0f, 0f, 0f);
	static final void getBaricentricCoords(float x, float y, Vertex2f v1, Vertex2f v2, Vertex2f v3, Vector2f textCoordsOut)
	{
		float g1 = ((v2.pos.y - v3.pos.y) * (x -        v3.pos.x) + (v3.pos.x - v2.pos.x) * (y        - v3.pos.y)) / 
				   ((v2.pos.y - v3.pos.y) * (v1.pos.x - v3.pos.x) + (v3.pos.x - v2.pos.x) * (v1.pos.y - v3.pos.y));
		
		float g2 = ((v3.pos.y - v1.pos.y) * (x -        v3.pos.x) + (v1.pos.x - v3.pos.x) * (y -        v3.pos.y)) / 
				   ((v2.pos.y - v3.pos.y) * (v1.pos.x - v3.pos.x) + (v3.pos.x - v2.pos.x) * (v1.pos.y - v3.pos.y));
		
		float g3 = 1f - g1 - g2;
		
		textCoordsOut.x = g1 * v1.tex.x + g2 * v2.tex.x + g3 * v3.tex.x;
		textCoordsOut.y = g1 * v1.tex.y + g2 * v2.tex.y + g3 * v3.tex.y;
		
//		debugCol.set(g1, g2, g3);
//		set((int) x, (int) y, debugCol);
	}
	
	
	
	/**
	 * @return z value
	 */
	static final float getBaricentricCoords(float x, float y, Vertex3f v1, Vertex3f v2, Vertex3f v3, Vector2f textCoordsOut)
	{
		float g1 = ((v2.pos.y - v3.pos.y) * (x -        v3.pos.x) + (v3.pos.x - v2.pos.x) * (y        - v3.pos.y)) / 
				   ((v2.pos.y - v3.pos.y) * (v1.pos.x - v3.pos.x) + (v3.pos.x - v2.pos.x) * (v1.pos.y - v3.pos.y));
		
		float g2 = ((v3.pos.y - v1.pos.y) * (x -        v3.pos.x) + (v1.pos.x - v3.pos.x) * (y -        v3.pos.y)) / 
				   ((v2.pos.y - v3.pos.y) * (v1.pos.x - v3.pos.x) + (v3.pos.x - v2.pos.x) * (v1.pos.y - v3.pos.y));
		
		float g3 = 1f - g1 - g2;
		
		textCoordsOut.x = g1 * v1.tex.x + g2 * v2.tex.x + g3 * v3.tex.x;
		textCoordsOut.y = g1 * v1.tex.y + g2 * v2.tex.y + g3 * v3.tex.y;
		
		return g1 * v1.pos.z + g2 * v2.pos.z + g3 * v3.pos.z;
		
//		debugCol.set(g1, g2, g3);
//		set((int) x, (int) y, debugCol);
	}
	
	/**
	 * @return z value
	 */
	static final float getBaricentricCoords(float x, float y, Vector3f v1, Vector3f v2, Vector3f v3)
	{
		float g1 = ((v2.y - v3.y) * (x -    v3.x) + (v3.x - v2.x) * (y    - v3.y)) / 
				   ((v2.y - v3.y) * (v1.x - v3.x) + (v3.x - v2.x) * (v1.y - v3.y));
		
		float g2 = ((v3.y - v1.y) * (x -    v3.x) + (v1.x - v3.x) * (y -    v3.y)) / 
				   ((v2.y - v3.y) * (v1.x - v3.x) + (v3.x - v2.x) * (v1.y - v3.y));
		
		float g3 = 1f - g1 - g2;
		
		return g1 * v1.z + g2 * v2.z + g3 * v3.z;
	}
	
	public static final void getBarycentricCoords(float x, float y, Vector3f v1, Vector3f v2, Vector3f v3, Vector3f baryCoordsOut)
	{
		float g1 = ((v2.y - v3.y) * (x -    v3.x) + (v3.x - v2.x) * (y    - v3.y)) / 
				   ((v2.y - v3.y) * (v1.x - v3.x) + (v3.x - v2.x) * (v1.y - v3.y));
		
		float g2 = ((v3.y - v1.y) * (x -    v3.x) + (v1.x - v3.x) * (y -    v3.y)) / 
				   ((v2.y - v3.y) * (v1.x - v3.x) + (v3.x - v2.x) * (v1.y - v3.y));
		
		baryCoordsOut.set(g1, g2, 1f - g1 - g2);
	}
	
	public static final void getBarycentricCoords(float x, float y, Vector2f v1, Vector2f v2, Vector2f v3, Vector3f baryCoordsOut)
	{
		float g1 = ((v2.y - v3.y) * (x -    v3.x) + (v3.x - v2.x) * (y    - v3.y)) / 
				   ((v2.y - v3.y) * (v1.x - v3.x) + (v3.x - v2.x) * (v1.y - v3.y));
		
		float g2 = ((v3.y - v1.y) * (x -    v3.x) + (v1.x - v3.x) * (y -    v3.y)) / 
				   ((v2.y - v3.y) * (v1.x - v3.x) + (v3.x - v2.x) * (v1.y - v3.y));
		
		baryCoordsOut.set(g1, g2, 1f - g1 - g2);
	}
	
	
	public final void rasterTriangle(Vertex2f v1, Vertex2f v2, Vertex2f v3, BgrByteImage texture, ByteBuffer mask)
	{
		rasterTriangle(v1, v2, v3, texture, mask, null);
	}
	
	private Vector2f texCoordsTemp;
	public final void rasterTriangle(Vertex2f v1, Vertex2f v2, Vertex2f v3, BgrByteImage texture, ByteBuffer mask, RgbColor overrideColor)
	{
		// must be counter-clockwise
		if (clockwise(v1.pos, v2.pos, v3.pos))
		{
			Vertex2f temp = v2;
			v2 = v3;
			v3 = temp;
		}
		
		if (texCoordsTemp == null) texCoordsTemp = new Vector2f();
		
		// 28.4 fixed-point coordinates
	    int Y1 = Math.round(16.0f * v1.pos.y);
	    int Y2 = Math.round(16.0f * v2.pos.y);
	    int Y3 = Math.round(16.0f * v3.pos.y);

	    int X1 = Math.round(16.0f * v1.pos.x);
	    int X2 = Math.round(16.0f * v2.pos.x);
	    int X3 = Math.round(16.0f * v3.pos.x);

	    // Deltas
	    int DX12 = X1 - X2;
	    int DX23 = X2 - X3;
	    int DX31 = X3 - X1;

	    int DY12 = Y1 - Y2;
	    int DY23 = Y2 - Y3;
	    int DY31 = Y3 - Y1;

	    // Fixed-point deltas
	    int FDX12 = DX12 << 4;
	    int FDX23 = DX23 << 4;
	    int FDX31 = DX31 << 4;

	    int FDY12 = DY12 << 4;
	    int FDY23 = DY23 << 4;
	    int FDY31 = DY31 << 4;

	    // Bounding rectangle
	    int minx = (Math.min(Math.min(X1, X2), X3) + 0xF) >> 4;
	    int maxx = (Math.max(Math.max(X1, X2), X3) + 0xF) >> 4;
	    int miny = (Math.min(Math.min(Y1, Y2), Y3) + 0xF) >> 4;
	    int maxy = (Math.max(Math.max(Y1, Y2), Y3) + 0xF) >> 4;

	    // Block size, standard 8x8 (must be power of two)
	    int q = 8;

	    // Start in corner of 8x8 block
	    minx &= ~(q - 1);
	    miny &= ~(q - 1);

	    // Half-edge constants
	    int C1 = DY12 * X1 - DX12 * Y1;
	    int C2 = DY23 * X2 - DX23 * Y2;
	    int C3 = DY31 * X3 - DX31 * Y3;

	    // Correct for fill convention
	    if(DY12 < 0 || (DY12 == 0 && DX12 > 0)) C1++;
	    if(DY23 < 0 || (DY23 == 0 && DX23 > 0)) C2++;
	    if(DY31 < 0 || (DY31 == 0 && DX31 > 0)) C3++;

	    // Loop through blocks
	    for(int y = miny; y < maxy; y += q)
	    {
	        for(int x = minx; x < maxx; x += q)
	        {
	            // Corners of block
	            int x0 = x << 4;
	            int x1 = (x + q - 1) << 4;
	            int y0 = y << 4;
	            int y1 = (y + q - 1) << 4;

	            // Evaluate half-space functions
	            byte a00 = C1 + DX12 * y0 - DY12 * x0 > 0 ? (byte)1 : (byte)0;
	            byte a10 = C1 + DX12 * y0 - DY12 * x1 > 0 ? (byte)1 : (byte)0;
	            byte a01 = C1 + DX12 * y1 - DY12 * x0 > 0 ? (byte)1 : (byte)0;
	            byte a11 = C1 + DX12 * y1 - DY12 * x1 > 0 ? (byte)1 : (byte)0;
	            int a = (a00 << 0) | (a10 << 1) | (a01 << 2) | (a11 << 3);
	    
	            byte b00 = C2 + DX23 * y0 - DY23 * x0 > 0 ? (byte)1 : (byte)0;
	            byte b10 = C2 + DX23 * y0 - DY23 * x1 > 0 ? (byte)1 : (byte)0;
	            byte b01 = C2 + DX23 * y1 - DY23 * x0 > 0 ? (byte)1 : (byte)0;
	            byte b11 = C2 + DX23 * y1 - DY23 * x1 > 0 ? (byte)1 : (byte)0;
	            int b = (b00 << 0) | (b10 << 1) | (b01 << 2) | (b11 << 3);
	    
	            byte c00 = C3 + DX31 * y0 - DY31 * x0 > 0 ? (byte)1 : (byte)0;
	            byte c10 = C3 + DX31 * y0 - DY31 * x1 > 0 ? (byte)1 : (byte)0;
	            byte c01 = C3 + DX31 * y1 - DY31 * x0 > 0 ? (byte)1 : (byte)0;
	            byte c11 = C3 + DX31 * y1 - DY31 * x1 > 0 ? (byte)1 : (byte)0;
	            int c = (c00 << 0) | (c10 << 1) | (c01 << 2) | (c11 << 3);

	            // Skip block when outside an edge
	            if(a == 0x0 || b == 0x0 || c == 0x0) continue;

	            // Accept whole block when totally covered
	            if(a == 0xF && b == 0xF && c == 0xF)
	            {
	                for(int iy = y; iy < y + q; iy++)
	                {
	                    for(int ix = x; ix < x + q; ix++)
	                    {     
	                    	getBaricentricCoords(ix, iy, v1, v2, v3, texCoordsTemp);
	                    	
	                    	if (mask != null)
	                    	{
	                    		int index = ((int) texCoordsTemp.y) * texture.width + (int) texCoordsTemp.x;
	                    		if (index < 0 || index >= mask.capacity() || mask.get(index) == 0) continue;
	                    	}
	                    		
	                    	if (overrideColor != null) set(ix, iy, overrideColor);	                
	                    	else setBilinear(ix, iy, texCoordsTemp, texture);	                    	
	                    }
	                }
	            }
	            else // Partially covered block
	            {
	                int CY1 = C1 + DX12 * y0 - DY12 * x0;
	                int CY2 = C2 + DX23 * y0 - DY23 * x0;
	                int CY3 = C3 + DX31 * y0 - DY31 * x0;

	                for(int iy = y; iy < y + q; iy++)
	                {
	                    int CX1 = CY1;
	                    int CX2 = CY2;
	                    int CX3 = CY3;

	                    for(int ix = x; ix < x + q; ix++)
	                    {
	                        if(CX1 > 0 && CX2 > 0 && CX3 > 0)
	                        {
		                    	getBaricentricCoords(ix, iy, v1, v2, v3, texCoordsTemp);
		                    	
		                    	if (mask != null)
		                    	{
		                    		int index = ((int) texCoordsTemp.y) * texture.width + (int) texCoordsTemp.x;
		                    		if (index < 0 || index >= mask.capacity() || mask.get(index) == 0) continue;
		                    	}	                 
		                    	
		                    	if (overrideColor != null) set(ix, iy, overrideColor);	                
		                    	else setBilinear(ix, iy, texCoordsTemp, texture);	
	                        }

	                        CX1 -= FDY12;
	                        CX2 -= FDY23;
	                        CX3 -= FDY31;
	                    }

	                    CY1 += FDX12;
	                    CY2 += FDX23;
	                    CY3 += FDX31;     
	                }
	            }
	        }
	    }
	}
	
	/**
	 * performs a z buffer check before rendering a pixel. smaller z needed for rendering.
	 */
	public final void rasterTriangleDepth(Vertex3f v1, Vertex3f v2, Vertex3f v3, BgrByteImage texture, FloatImage depth)
	{
		// must be counter-clockwise
		if (clockwise(v1.pos, v2.pos, v3.pos))
		{
			Vertex3f temp = v2;
			v2 = v3;
			v3 = temp;
		}
		
		if (texCoordsTemp == null) texCoordsTemp = new Vector2f();
		
		// 28.4 fixed-point coordinates
	    int Y1 = Math.round(16.0f * v1.pos.y);
	    int Y2 = Math.round(16.0f * v2.pos.y);
	    int Y3 = Math.round(16.0f * v3.pos.y);

	    int X1 = Math.round(16.0f * v1.pos.x);
	    int X2 = Math.round(16.0f * v2.pos.x);
	    int X3 = Math.round(16.0f * v3.pos.x);

	    // Deltas
	    int DX12 = X1 - X2;
	    int DX23 = X2 - X3;
	    int DX31 = X3 - X1;

	    int DY12 = Y1 - Y2;
	    int DY23 = Y2 - Y3;
	    int DY31 = Y3 - Y1;

	    // Fixed-point deltas
	    int FDX12 = DX12 << 4;
	    int FDX23 = DX23 << 4;
	    int FDX31 = DX31 << 4;

	    int FDY12 = DY12 << 4;
	    int FDY23 = DY23 << 4;
	    int FDY31 = DY31 << 4;

	    // Bounding rectangle
	    int minx = (Math.min(Math.min(X1, X2), X3) + 0xF) >> 4;
	    int maxx = (Math.max(Math.max(X1, X2), X3) + 0xF) >> 4;
	    int miny = (Math.min(Math.min(Y1, Y2), Y3) + 0xF) >> 4;
	    int maxy = (Math.max(Math.max(Y1, Y2), Y3) + 0xF) >> 4;

	    // Block size, standard 8x8 (must be power of two)
	    int q = 8;

	    // Start in corner of 8x8 block
	    minx &= ~(q - 1);
	    miny &= ~(q - 1);

	    // Half-edge constants
	    int C1 = DY12 * X1 - DX12 * Y1;
	    int C2 = DY23 * X2 - DX23 * Y2;
	    int C3 = DY31 * X3 - DX31 * Y3;

	    // Correct for fill convention
	    if(DY12 < 0 || (DY12 == 0 && DX12 > 0)) C1++;
	    if(DY23 < 0 || (DY23 == 0 && DX23 > 0)) C2++;
	    if(DY31 < 0 || (DY31 == 0 && DX31 > 0)) C3++;

	    // Loop through blocks
	    for(int y = miny; y < maxy; y += q)
	    {
	        for(int x = minx; x < maxx; x += q)
	        {
	            // Corners of block
	            int x0 = x << 4;
	            int x1 = (x + q - 1) << 4;
	            int y0 = y << 4;
	            int y1 = (y + q - 1) << 4;

	            // Evaluate half-space functions
	            byte a00 = C1 + DX12 * y0 - DY12 * x0 > 0 ? (byte)1 : (byte)0;
	            byte a10 = C1 + DX12 * y0 - DY12 * x1 > 0 ? (byte)1 : (byte)0;
	            byte a01 = C1 + DX12 * y1 - DY12 * x0 > 0 ? (byte)1 : (byte)0;
	            byte a11 = C1 + DX12 * y1 - DY12 * x1 > 0 ? (byte)1 : (byte)0;
	            int a = (a00 << 0) | (a10 << 1) | (a01 << 2) | (a11 << 3);
	    
	            byte b00 = C2 + DX23 * y0 - DY23 * x0 > 0 ? (byte)1 : (byte)0;
	            byte b10 = C2 + DX23 * y0 - DY23 * x1 > 0 ? (byte)1 : (byte)0;
	            byte b01 = C2 + DX23 * y1 - DY23 * x0 > 0 ? (byte)1 : (byte)0;
	            byte b11 = C2 + DX23 * y1 - DY23 * x1 > 0 ? (byte)1 : (byte)0;
	            int b = (b00 << 0) | (b10 << 1) | (b01 << 2) | (b11 << 3);
	    
	            byte c00 = C3 + DX31 * y0 - DY31 * x0 > 0 ? (byte)1 : (byte)0;
	            byte c10 = C3 + DX31 * y0 - DY31 * x1 > 0 ? (byte)1 : (byte)0;
	            byte c01 = C3 + DX31 * y1 - DY31 * x0 > 0 ? (byte)1 : (byte)0;
	            byte c11 = C3 + DX31 * y1 - DY31 * x1 > 0 ? (byte)1 : (byte)0;
	            int c = (c00 << 0) | (c10 << 1) | (c01 << 2) | (c11 << 3);

	            // Skip block when outside an edge
	            if(a == 0x0 || b == 0x0 || c == 0x0) continue;

	            // Accept whole block when totally covered
	            if(a == 0xF && b == 0xF && c == 0xF)
	            {
	                for(int iy = y; iy < y + q; iy++)
	                {
	                    for(int ix = x; ix < x + q; ix++)
	                    {   
	                    	if (ix < 0 || iy < 0 || ix >= width || iy >= height) continue; // because of unchecked depth access
	                    	float z = getBaricentricCoords(ix, iy, v1, v2, v3, texCoordsTemp);
	                    	float oldZ = depth.get(ix, iy); // possibly out of image borders
	                    	
	                    	if (z < oldZ)
	                    	{
	                    		depth.set(ix, iy, z);
	                    		setBilinear(ix, iy, texCoordsTemp, texture);	                    
	                    	}	
	                    }
	                }
	            }
	            else // Partially covered block
	            {
	                int CY1 = C1 + DX12 * y0 - DY12 * x0;
	                int CY2 = C2 + DX23 * y0 - DY23 * x0;
	                int CY3 = C3 + DX31 * y0 - DY31 * x0;

	                for(int iy = y; iy < y + q; iy++)
	                {
	                    int CX1 = CY1;
	                    int CX2 = CY2;
	                    int CX3 = CY3;

	                    for(int ix = x; ix < x + q; ix++)
	                    {
	                        if(CX1 > 0 && CX2 > 0 && CX3 > 0)
	                        {                
	                        	if (ix < 0 || iy < 0 || ix >= width || iy >= height) continue; // because of unchecked depth access
		                    	
	                        	float z = getBaricentricCoords(ix, iy, v1, v2, v3, texCoordsTemp);	             
		                    	float oldZ = depth.get(ix, iy); 
		                    	
		                    	if (z < oldZ)
		                    	{
		                    		depth.set(ix, iy, z);
		                    		setBilinear(ix, iy, texCoordsTemp, texture);	                    
		                    	}
	                        }

	                        CX1 -= FDY12;
	                        CX2 -= FDY23;
	                        CX3 -= FDY31;
	                    }

	                    CY1 += FDX12;
	                    CY2 += FDX23;
	                    CY3 += FDX31;     
	                }
	            }
	        }
	    }
	}
	
	public final void rasterTriangle(Vector2f v1, Vector2f v2, Vector2f v3, RgbColor color)
	{
		// must be counter-clockwise
		if (clockwise(v1, v2, v3))
		{
			Vector2f temp = v2;
			v2 = v3;
			v3 = temp;
		}
		
		if (texCoordsTemp == null) texCoordsTemp = new Vector2f();
		
		// 28.4 fixed-point coordinates
	    int Y1 = Math.round(16.0f * v1.y);
	    int Y2 = Math.round(16.0f * v2.y);
	    int Y3 = Math.round(16.0f * v3.y);

	    int X1 = Math.round(16.0f * v1.x);
	    int X2 = Math.round(16.0f * v2.x);
	    int X3 = Math.round(16.0f * v3.x);

	    // Deltas
	    int DX12 = X1 - X2;
	    int DX23 = X2 - X3;
	    int DX31 = X3 - X1;

	    int DY12 = Y1 - Y2;
	    int DY23 = Y2 - Y3;
	    int DY31 = Y3 - Y1;

	    // Fixed-point deltas
	    int FDX12 = DX12 << 4;
	    int FDX23 = DX23 << 4;
	    int FDX31 = DX31 << 4;

	    int FDY12 = DY12 << 4;
	    int FDY23 = DY23 << 4;
	    int FDY31 = DY31 << 4;

	    // Bounding rectangle
	    int minx = (Math.min(Math.min(X1, X2), X3) + 0xF) >> 4;
	    int maxx = (Math.max(Math.max(X1, X2), X3) + 0xF) >> 4;
	    int miny = (Math.min(Math.min(Y1, Y2), Y3) + 0xF) >> 4;
	    int maxy = (Math.max(Math.max(Y1, Y2), Y3) + 0xF) >> 4;

	    // Block size, standard 8x8 (must be power of two)
	    int q = 8;

	    // Start in corner of 8x8 block
	    minx &= ~(q - 1);
	    miny &= ~(q - 1);

	    // Half-edge constants
	    int C1 = DY12 * X1 - DX12 * Y1;
	    int C2 = DY23 * X2 - DX23 * Y2;
	    int C3 = DY31 * X3 - DX31 * Y3;

	    // Correct for fill convention
	    if(DY12 < 0 || (DY12 == 0 && DX12 > 0)) C1++;
	    if(DY23 < 0 || (DY23 == 0 && DX23 > 0)) C2++;
	    if(DY31 < 0 || (DY31 == 0 && DX31 > 0)) C3++;

	    // Loop through blocks
	    for(int y = miny; y < maxy; y += q)
	    {
	        for(int x = minx; x < maxx; x += q)
	        {
	            // Corners of block
	            int x0 = x << 4;
	            int x1 = (x + q - 1) << 4;
	            int y0 = y << 4;
	            int y1 = (y + q - 1) << 4;

	            // Evaluate half-space functions
	            byte a00 = C1 + DX12 * y0 - DY12 * x0 > 0 ? (byte)1 : (byte)0;
	            byte a10 = C1 + DX12 * y0 - DY12 * x1 > 0 ? (byte)1 : (byte)0;
	            byte a01 = C1 + DX12 * y1 - DY12 * x0 > 0 ? (byte)1 : (byte)0;
	            byte a11 = C1 + DX12 * y1 - DY12 * x1 > 0 ? (byte)1 : (byte)0;
	            int a = (a00 << 0) | (a10 << 1) | (a01 << 2) | (a11 << 3);
	    
	            byte b00 = C2 + DX23 * y0 - DY23 * x0 > 0 ? (byte)1 : (byte)0;
	            byte b10 = C2 + DX23 * y0 - DY23 * x1 > 0 ? (byte)1 : (byte)0;
	            byte b01 = C2 + DX23 * y1 - DY23 * x0 > 0 ? (byte)1 : (byte)0;
	            byte b11 = C2 + DX23 * y1 - DY23 * x1 > 0 ? (byte)1 : (byte)0;
	            int b = (b00 << 0) | (b10 << 1) | (b01 << 2) | (b11 << 3);
	    
	            byte c00 = C3 + DX31 * y0 - DY31 * x0 > 0 ? (byte)1 : (byte)0;
	            byte c10 = C3 + DX31 * y0 - DY31 * x1 > 0 ? (byte)1 : (byte)0;
	            byte c01 = C3 + DX31 * y1 - DY31 * x0 > 0 ? (byte)1 : (byte)0;
	            byte c11 = C3 + DX31 * y1 - DY31 * x1 > 0 ? (byte)1 : (byte)0;
	            int c = (c00 << 0) | (c10 << 1) | (c01 << 2) | (c11 << 3);

	            // Skip block when outside an edge
	            if(a == 0x0 || b == 0x0 || c == 0x0) continue;

	            // Accept whole block when totally covered
	            if(a == 0xF && b == 0xF && c == 0xF)
	            {
	                for(int iy = y; iy < y + q; iy++)
	                {
	                    for(int ix = x; ix < x + q; ix++)
	                    {                	
	                    	set(ix, iy, color);
	                    }
	                }
	            }
	            else // Partially covered block
	            {
	                int CY1 = C1 + DX12 * y0 - DY12 * x0;
	                int CY2 = C2 + DX23 * y0 - DY23 * x0;
	                int CY3 = C3 + DX31 * y0 - DY31 * x0;

	                for(int iy = y; iy < y + q; iy++)
	                {
	                    int CX1 = CY1;
	                    int CX2 = CY2;
	                    int CX3 = CY3;

	                    for(int ix = x; ix < x + q; ix++)
	                    {
	                        if(CX1 > 0 && CX2 > 0 && CX3 > 0)
	                        {             
	                        	set(ix, iy, color);
	                        }

	                        CX1 -= FDY12;
	                        CX2 -= FDY23;
	                        CX3 -= FDY31;
	                    }

	                    CY1 += FDX12;
	                    CY2 += FDX23;
	                    CY3 += FDX31;     
	                }
	            }
	        }
	    }
	}
	
	public void save(File file)
	{
		String extension = file.getName().substring(file.getName().lastIndexOf(".") + 1);
		try 
		{
			ImageIO.write(image, extension, file);
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
	}
	
	public void save(OutputStream os, String extension)
	{
		try 
		{
			ImageIO.write(image, extension, os);
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
	}
	
	public ByteBuffer createByteBuffer()
	{
		ByteBuffer buffer = ByteBuffer.allocateDirect(data.length);
		buffer.put(data);
        buffer.rewind();
        return buffer;
	}
	
	private static Matrix3f m = new Matrix3f();
	public static boolean clockwise(Vector2f v1, Vector2f v2, Vector2f v3)
	{
		m.m00 = 1f;
		m.m01 = v1.x;
		m.m02 = v1.y;
		m.m10 = 1f;
		m.m11 = v2.x;
		m.m12 = v2.y;
		m.m20 = 1f;
		m.m21 = v3.x;
		m.m22 = v3.y;
		return m.determinant() >= 0;
	}
	
	public static boolean clockwise(Vector3f v1, Vector3f v2, Vector3f v3)
	{
		m.m00 = 1f;
		m.m01 = v1.x;
		m.m02 = v1.y;
		m.m10 = 1f;
		m.m11 = v2.x;
		m.m12 = v2.y;
		m.m20 = 1f;
		m.m21 = v3.x;
		m.m22 = v3.y;
		return m.determinant() >= 0;
	}
	
	public static void main(String[] args) throws Exception
	{
		BgrByteImage test = new BgrByteImage(100, 100);
		
		Vector2f v1 = new Vector2f(0f, 0f);
		Vector2f v2 = new Vector2f(50f, 30f);
		Vector2f v3 = new Vector2f(90f, 10f);
		
		System.out.println("cc: " + clockwise(v1, v2, v3));
		System.out.println("cc: " + clockwise(v2, v3, v1));
		System.out.println("cc: " + clockwise(v3, v1, v2));
		
		System.out.println("c: " + clockwise(v1, v3, v2));
		System.out.println("c: " + clockwise(v2, v1, v3));
		System.out.println("c: " + clockwise(v3, v2, v1));
		
		RgbColor color = new RgbColor(1f, 0f, 0f);
		
		test.rasterTriangle(v3, v2, v1, color);
		
		test.save(new File("D:/test.png"));
	}
}
