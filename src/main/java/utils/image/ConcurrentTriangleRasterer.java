package utils.image;

import utils.math.Vector2f;
import utils.math.Vector3f;

public class ConcurrentTriangleRasterer 
{
	public Vertex3f[] triangle;
	BgrByteImage image;
	
	private Vector2f texCoordsTemp = new Vector2f();
	
	public ConcurrentTriangleRasterer(BgrByteImage image)
	{
		this.image = image;
		
		triangle = new Vertex3f[3];
		for (int i=0; i<3; i++) triangle[i] = new Vertex3f(new Vector3f(), new Vector2f());
	}
	
	BgrByteImage texture;
	FloatImage depth;
	public void set(BgrByteImage texture, FloatImage depth)
	{
		this.texture = texture;
		this.depth = depth;
	}
	
	/**
	 * performs a z buffer check before rendering a pixel. smaller z needed for rendering.
	 */
	public final void rasterTriangleDepth()
	{
		Vertex3f v1 = triangle[0];
		Vertex3f v2 = triangle[1];
		Vertex3f v3 = triangle[2];
		
		// don't render triangles which are partly outside of the framebuffer
		// this is for performance reasons to avoid large triangles, comment this out; code would work as well
		for (int v=0; v<triangle.length; v++) if (triangle[v].pos.x  < 0 || triangle[v].pos.y < 0 || triangle[v].pos.x  >= image.getWidth() || triangle[v].pos.y >= image.getHeight()) return;
		
		// must be counter-clockwise
		if (BgrByteImage.clockwise(v1.pos, v2.pos, v3.pos))
		{
			Vertex3f temp = v2;
			v2 = v3;
			v3 = temp;
		}
		
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
	                    	if (ix < 0 || iy < 0 || ix >= image.getWidth() || iy >= image.getHeight()) continue; // because of unchecked depth access
	                    	float z = BgrByteImage.getBaricentricCoords(ix, iy, v1, v2, v3, texCoordsTemp);
	                    	float oldZ = depth.get(ix, iy);
	                    	
	                    	if (z < oldZ)
	                    	{	
	                    		RgbColor color = getBilinear(ix, iy, texCoordsTemp, texture);
	                    		
	                    		oldZ = depth.get(ix, iy);
		                    	if (z < oldZ)
		                    	{	
		                    		depth.set(ix, iy, z);
		                    		image.set(ix, iy, color); // still a chance that another thread has already set a different depth
		                    	}
	                    		
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
	                        	if (ix < 0 || iy < 0 || ix >= image.getWidth() || iy >= image.getHeight()) continue; // because of unchecked depth access
		                    	
	                        	float z = BgrByteImage.getBaricentricCoords(ix, iy, v1, v2, v3, texCoordsTemp);	             
		                    	float oldZ = depth.get(ix, iy); 
		                    	
		                    	if (z < oldZ)
		                    	{	
		                    		RgbColor color = getBilinear(ix, iy, texCoordsTemp, texture);
		                    		
		                    		oldZ = depth.get(ix, iy);
			                    	if (z < oldZ)
			                    	{	
			                    		depth.set(ix, iy, z);
			                    		image.set(ix, iy, color);
			                    	}
		                    		
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
	
	/**
	 * performs a z buffer check before rendering a pixel. smaller z needed for rendering.
	 * only renders pixels when textureMask is 1f.
	 */
	public final void rasterTriangleDepth(FloatImage textureMask)
	{
		Vertex3f v1 = triangle[0];
		Vertex3f v2 = triangle[1];
		Vertex3f v3 = triangle[2];
		
		// don't render triangles which are partly outside of the framebuffer
		// this is for performance reasons to avoid large triangles, comment this out; code would work as well
		for (int v=0; v<triangle.length; v++) if (triangle[v].pos.x  < 0 || triangle[v].pos.y < 0 || triangle[v].pos.x  >= image.getWidth() || triangle[v].pos.y >= image.getHeight()) return;
		
		// must be counter-clockwise
		if (BgrByteImage.clockwise(v1.pos, v2.pos, v3.pos))
		{
			Vertex3f temp = v2;
			v2 = v3;
			v3 = temp;
		}
		
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
	                    	if (ix < 0 || iy < 0 || ix >= image.getWidth() || iy >= image.getHeight()) continue; // because of unchecked depth access
	                    	float z = BgrByteImage.getBaricentricCoords(ix, iy, v1, v2, v3, texCoordsTemp);
	                    	float oldZ = depth.get(ix, iy);
	                    	
	                    	if (z < oldZ)
	                    	{	
	                    		if (getBilinear(ix, iy, texCoordsTemp, textureMask) >= 0.99f)
	                    		{
		                    		RgbColor color = getBilinear(ix, iy, texCoordsTemp, texture);
		                    		
		                    		oldZ = depth.get(ix, iy);
			                    	if (z < oldZ)
			                    	{	
			                    		depth.set(ix, iy, z);
			                    		image.set(ix, iy, color); // still a chance that another thread has already set a different depth
			                    	}
	                    		}
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
	                        	if (ix < 0 || iy < 0 || ix >= image.getWidth() || iy >= image.getHeight()) continue; // because of unchecked depth access
		                    	
	                        	float z = BgrByteImage.getBaricentricCoords(ix, iy, v1, v2, v3, texCoordsTemp);	             
		                    	float oldZ = depth.get(ix, iy); 
		                    	
		                    	if (z < oldZ)
		                    	{	
		                    		if (getBilinear(ix, iy, texCoordsTemp, textureMask) >= 0.99f)
		                    		{
			                    		RgbColor color = getBilinear(ix, iy, texCoordsTemp, texture);
			                    		
			                    		oldZ = depth.get(ix, iy);
				                    	if (z < oldZ)
				                    	{	
				                    		depth.set(ix, iy, z);
				                    		image.set(ix, iy, color);
				                    	}
		                    		}
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
	
	private RgbColor col1 = new RgbColor(0f, 0f, 0f);
	private RgbColor col2 = new RgbColor(0f, 0f, 0f);
	private RgbColor col3 = new RgbColor(0f, 0f, 0f);
	private RgbColor col4 = new RgbColor(0f, 0f, 0f);
	private final RgbColor getBilinear(int x, int y, Vector2f texCoords, BgrByteImage texture)
	{
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
		
		return col1; // was col4
	}
	
	private final float getBilinear(int x, int y, Vector2f texCoords, FloatImage texture)
	{
		int xL = (int) Math.floor(texCoords.x);
		int xR = (int) Math.ceil(texCoords.x);
		int yU = (int) Math.floor(texCoords.y);
		int yB = (int) Math.ceil(texCoords.y);
		float weightRight = texCoords.x - xL;
		float weightBottom = texCoords.y - yU;
	
		float col1 = texture.get_BoundCheck(xL, yU);
		float col2 = texture.get_BoundCheck(xR, yU);
		float col3 = col2 * weightRight + col1 * (1f - weightRight);

		col1 = texture.get_BoundCheck(xL, yB);
		col2 = texture.get_BoundCheck(xR, yB);
		float col4 = col2 * weightRight + col1 * (1f - weightRight);
		
		return col4 * weightBottom + col3 * (1f - weightBottom);
	}
}
