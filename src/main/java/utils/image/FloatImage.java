package utils.image;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.FloatBuffer;
import java.util.Arrays;
import java.util.zip.GZIPOutputStream;

import javax.imageio.ImageIO;

import utils.io.BinaryFileUtil;
import utils.math.Matrix3f;
import utils.math.Vector2f;
import utils.math.Vector3f;

public class FloatImage 
{
	private int width;
	private int height;
	public float data[];
	
	public FloatImage(File file) throws Exception
	{
		this(new FileInputStream(file));
	}
	
	public FloatImage(InputStream is) throws Exception
	{
		BufferedImage image = ImageIO.read(is);
		BgrByteImage.checkBGRImage(image, BufferedImage.TYPE_BYTE_GRAY, "TYPE_BYTE_GRAY");
		
		Raster r = image.getRaster();
		DataBufferByte dbi = (DataBufferByte) r.getDataBuffer();
		byte b[] = dbi.getData();
		
		width = image.getWidth();
		height = image.getHeight();
		data = new float[width * height];
		
		for (int i=0; i<data.length; i++)
		{
			data[i] = (float) (b[i] & 0xFF) / 255f;
		}
	}
	
	public FloatImage(BgrByteImage image)
	{
		width = image.getWidth();
		height = image.getHeight();
		data = new float[width * height];
		
		RgbColor color = new RgbColor(0f, 0f, 0f);
		for (int i=0; i<data.length; i++)
		{
			image.getColor(i, color);
			data[i] = 0.3f * color.getR() +  0.59f * color.getG() + 0.11f * color.getB();
		}
	}
	
	public void set(BgrByteImage image)
	{
		RgbColor color = new RgbColor(0f, 0f, 0f);
		for (int i=0; i<data.length; i++)
		{
			image.getColor(i, color);
			data[i] = 0.3f * color.getR() +  0.59f * color.getG() + 0.11f * color.getB();
		}
	}
	
	private static final float sobel[] = new float[] {-1, -2, -1, 1, 2, 1};
	private static final int sobelyOffsets[][] = new int[][] {{-1, -1}, {0, -1}, {1, -1}, {-1, 1}, {0, 1}, {1, 1}};
	private static final int sobelxOffsets[][] = new int[][] {{-1, -1}, {-1, 0}, {-1, 1}, {1, -1}, {1, 0}, {1, 1}};
	
	private final int idx(int x, int y)
	{
		return y*width + x;
	}
	
	public void setToGradientImage(FloatImage image)
	{
		int sobelyOffsets[] = new int[] {idx(-1, -1), idx(0, -1), idx(1, -1), idx(-1, 1), idx(0, 1), idx(1, 1)};
		int sobelxOffsets[] = new int[] {idx(-1, -1), idx(-1, 0), idx(-1, 1), idx(1, -1), idx(1, 0), idx(1, 1)};
		
//		float max = -Float.MAX_VALUE;
		for (int y=1; y<height-1; y++)
		{	
			for (int x=1; x<width-1; x++)
			{
				float gradientY = 0f;
				float gradientX = 0f;
				
				int index = y*width + x;
				for (int i=0; i<sobel.length; i++)
				{
					gradientY += image.data[index + sobelyOffsets[i]] * sobel[i];
					gradientX += image.data[index + sobelxOffsets[i]] * sobel[i];
				}
				
				float g = (float) Math.sqrt(gradientX*gradientX + gradientY*gradientY);
				data[index] = g;
//				max = Math.max(max, g);
			}
		}
	}
	
	public final void set(int index, float value)
	{
		data[index] = value;
	}
	
	public final void set(int x, int y, float value)
	{
		data[y*width + x] = value;
	}
	
	public final void add(int x, int y, float value)
	{
		data[y*width + x] += value;
	}
	
	public final void add(FloatImage other)
	{
		for (int i=0; i<data.length; i++) data[i] += other.data[i];
	}
	
	public final void add(int x, int y, int radius, float value)
	{
		int minY = Math.max(0, y - radius);
		int maxY = Math.min(height - 1, y + radius);
		int minX = Math.max(0, x - radius);
		int maxX = Math.min(width - 1, x + radius);
		
		for (int yi=minY; yi<=maxY; yi++)
		{
			for (int xi=minX; xi<=maxX; xi++)
			{
				add(xi, yi, value);
			}
		}
	}
	
	public void reset(float value)
	{
		Arrays.fill(data, value);
	}
	
	public final float get(int index)
	{
		return data[index];
	}
	
	public final float get(int x, int y)
	{
		return data[y*width + x];
	}
	
	public final float get_BoundCheck(int x, int y)
	{
		x = Math.max(0, Math.min(width - 1, x));
		y = Math.max(0, Math.min(height - 1, y));
		return data[y*width + x];
	}
	
	public FloatImage(int width, int height)
	{
		this.width = width;
		this.height = height;
		data = new float[width * height];
	}
	
	public FloatImage(int width, int height, float[] data)
	{
		this.width = width;
		this.height = height;
		this.data = data;
	}
	
	public void set(FloatBuffer buffer)
	{
		buffer.rewind();
		for (int i=0; i<data.length; i++)
		{
			data[i] = buffer.get();
		}
		buffer.rewind();
	}
	
	public final int getWidth() 
	{
		return width;
	}



	public final int getHeight() 
	{
		return height;
	}

	public void setToMaxValue()
	{
		Arrays.fill(data, Float.MAX_VALUE);
	}
	
	private Vector3f baryCoords;
	public final void rasterTriangleDepth(Vector3f v1, Vector3f v2, Vector3f v3)
	{
		// must be counter-clockwise
		if (clockwise(v1, v2, v3))
		{
			Vector3f temp = v2;
			v2 = v3;
			v3 = temp;
		}
		
		if (baryCoords == null) baryCoords = new Vector3f();
		
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
	                    	setDepth(ix, iy, v1, v2, v3, baryCoords);
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
	                        	setDepth(ix, iy, v1, v2, v3, baryCoords);
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
	
	public final void rasterTriangleUniform(Vector2f v1, Vector2f v2, Vector2f v3, float value)
	{
		// must be counter-clockwise
		if (clockwise(v1, v2, v3))
		{
			Vector2f temp = v2;
			v2 = v3;
			v3 = temp;
		}
		
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
	                    	int index = iy*width + ix;       	
	                    	if (index >= 0 && index < data.length)
	                    	{
	                    		data[index] = value;
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
		                    	int index = iy*width + ix;       	
		                    	if (index >= 0 && index < data.length)
		                    	{
		                    		data[index] = value;
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
	
	private final void setDepth(int ix, int iy, Vector3f v1, Vector3f v2, Vector3f v3, Vector3f paramsTemp)
	{
    	getBaricentric(ix, iy, v1, v2, v3, paramsTemp);
    	float depth = paramsTemp.x * v1.z +  paramsTemp.y * v2.z + paramsTemp.z * v3.z;
    	int index = iy*width + ix;
    	
    	if (index >= 0 && index < data.length && depth < data[index])
    	{
    		data[index] = depth;
    	}
	}
	
	private final boolean hasLEDepth(int ix, int iy, Vector3f v1, Vector3f v2, Vector3f v3, Vector3f paramsTemp, float epsilon)
	{
    	getBaricentric(ix, iy, v1, v2, v3, paramsTemp);
    	float depth = paramsTemp.x * v1.z +  paramsTemp.y * v2.z + paramsTemp.z * v3.z;
    	int index = iy*width + ix;
    	
    	return index >= 0 && index < data.length && depth <= data[index] + epsilon;
	}
	
	private final void getBaricentric(float x, float y, Vector3f v1, Vector3f v2, Vector3f v3, Vector3f paramsOut)
	{
		float g1 = ((v2.y - v3.y) * (x -    v3.x) + (v3.x - v2.x) * (y    - v3.y)) / 
				   ((v2.y - v3.y) * (v1.x - v3.x) + (v3.x - v2.x) * (v1.y - v3.y));
		
		float g2 = ((v3.y - v1.y) * (x -    v3.x) + (v1.x - v3.x) * (y    - v3.y)) / 
				   ((v2.y - v3.y) * (v1.x - v3.x) + (v3.x - v2.x) * (v1.y - v3.y));
		
		float g3 = 1f - g1 - g2;
		
		paramsOut.set(g1, g2, g3);
	}
	
	public final boolean isTriangleCompletelyVisible(Vector3f v1, Vector3f v2, Vector3f v3, float epsilon)
	{
		// must be counter-clockwise
		if (clockwise(v1, v2, v3))
		{
			Vector3f temp = v2;
			v2 = v3;
			v3 = temp;
		}
		
		boolean isVisible = true;
		if (baryCoords == null) baryCoords = new Vector3f();
		
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
	                    	if (!hasLEDepth(ix, iy, v1, v2, v3, baryCoords, epsilon))
	                    	{
	                    		isVisible = false;
	                    		break;
	                    	}
	                    }              
	                    if (!isVisible) break;
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
		                    	if (!hasLEDepth(ix, iy, v1, v2, v3, baryCoords, epsilon))
		                    	{
		                    		isVisible = false;
		                    		break;
		                    	}
	                        }

	                        CX1 -= FDY12;
	                        CX2 -= FDY23;
	                        CX3 -= FDY31;
	                    }
	                    if (!isVisible) break;

	                    CY1 += FDX12;
	                    CY2 += FDX23;
	                    CY3 += FDX31;     
	                }
	            }
	            if (!isVisible) break;
	        }
	        if (!isVisible) break;
	    }
	    
	    return isVisible;
	}
	
	private static Matrix3f m = new Matrix3f();
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
	
	public void normalize()
	{
		float maxValue = 0f;
		for (int i=0; i<data.length; i++)
		{
			maxValue = Math.max(maxValue, data[i]);
		}
		System.out.println("max value is: " + maxValue);
		
		for (int i=0; i<data.length; i++)
		{
			data[i] /= maxValue;
		}
	}
	
	public void normalize(float maxValue)
	{
		for (int i=0; i<data.length; i++)
		{
			data[i] = Math.min(1f, data[i] / maxValue);
		}
	}
	
	public void saveToPNG(OutputStream os) throws IOException
	{
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
		Raster r = image.getRaster();
		DataBufferByte dbi = (DataBufferByte) r.getDataBuffer();
		byte b[] = dbi.getData();
		
		int length = width * height;
		for (int i=0; i<length; i++)
		{
			b[i] = (byte) (Math.min(1f, data[i]) * 255f);
		}
		
		ImageIO.write(image, "png", os);
	}
}
