package utils.image;

import java.util.Arrays;

import utils.math.Vector2f;
import utils.math.Vector3f;

public class IntImage 
{
	public final int width;
	public final int height;
	public final int data[];
	
	public IntImage(int width, int height)
	{
		this.width = width;
		this.height = height;
		data = new int[width * height];
	}
	
	public void setAll(int value)
	{
		Arrays.fill(data, value);
	}
	
	public final int get(int index)
	{
		return data[index];
	}
	
	public final boolean isWithin(float x, float y)
	{
		int xi = (int) x;
		int yi = (int) y;
		
		return xi >= 0 && xi < width && yi >= 0 && yi < height;
	}
	
	public final int get(float x, float y)
	{
		return data[(((int) y) * width + (int) x)];
	}
	
	public final void set(int x, int y, int value)
	{
		data[y*width + x] = value;
	}
	
	public final void add(int x, int y, int value)
	{
		data[y*width + x] += value;
	}
	
	public final void set(int index, int value)
	{
		data[index] = value;
	}
	
	public final void rasterTriangleDepth(Vector3f v1, Vector3f v2, Vector3f v3, int pixelValue, FloatImage depth)
	{
		// must be counter-clockwise
		if (BgrByteImage.clockwise(v1, v2, v3))
		{
			Vector3f temp = v2;
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
	                    	if (ix < 0 || iy < 0 || ix >= width || iy >= height) continue; // because of unchecked depth access
	                    	float z = BgrByteImage.getBaricentricCoords(ix, iy, v1, v2, v3);
	                    	float oldZ = depth.get(ix, iy);
	                    	
	                    	if (z < oldZ)
	                    	{
		                    	depth.set(ix, iy, z);
		                    	set(ix, iy, pixelValue);
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
	                        	float z = BgrByteImage.getBaricentricCoords(ix, iy, v1, v2, v3);	             
		                    	float oldZ = depth.get(ix, iy); 
		                    	
		                    	if (z < oldZ)
		                    	{	
			                    	depth.set(ix, iy, z);
			                    	set(ix, iy, pixelValue);
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
	
	public final void rasterTriangleDepth(Vector2f v1, Vector2f v2, Vector2f v3, int pixelValue)
	{
		// must be counter-clockwise
		if (BgrByteImage.clockwise(v1, v2, v3))
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
		                    set(ix, iy, pixelValue);
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
			                    set(ix, iy, pixelValue);
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
}
