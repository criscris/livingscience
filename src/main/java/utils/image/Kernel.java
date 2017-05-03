package utils.image;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Kernel 
{
	public static int[] createOffsets(int radius, int imageWidth)
	{
		List<Integer> offsets = new ArrayList<>();
		
		for (int y=-radius; y<=radius; y++)
		{
			float yd = y*y;
			
			for (int x=-radius; x<=radius; x++)
			{
				float xd = x*x;
				if (xd + yd <= radius*radius + 0.0001f)
				{
					offsets.add(y*imageWidth + x);
				}			
			}
		}
		
		int[] offs = new int[offsets.size()];
		for (int i=0; i<offsets.size(); i++) offs[i] = offsets.get(i);
	
//		System.out.println("offsets: " + Arrays.toString(offs));	
//		debug(offs, radius, imageWidth);
		
		return offs;
	}
	
	private static void debug(int[] offsets, int radius, int imageWidth)
	{
		BgrByteImage image = new BgrByteImage(imageWidth, imageWidth / 4 * 3); 
		int centerX = image.getWidth()/2;
		int centerY = image.getHeight()/2;
		int i = centerY * imageWidth + centerX;
		
		RgbColor color = new RgbColor(1f, 1f, 1f);
		for (int n=0; n<offsets.length; n++)
		{
			image.set(i + offsets[n], color);
		}
		
		image.save(new File("D:/testkernel.png"));
	}
}
