package utils.image;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

import utils.math.Vector3f;

public class DistanceTransform 
{
	
	int[] neighborOffsets;
	float[] neighborDistances;
	
	static final float startValue = 0f;
	static final float fillValue = Float.MAX_VALUE;
	
	FloatImage image;
	
	public DistanceTransform(FloatImage image, float startValue)
	{
		int w = image.getWidth();
		int pixels = w * image.getHeight();
		this.image = image;
		for (int i=0; i<pixels; i++)
		{
			image.data[i] = image.data[i] == startValue ? DistanceTransform.startValue : fillValue;
		}
		
		float d = (float) Math.sqrt(2);
		neighborOffsets = new int[] { -w-1, -w, -w+1,  -1, 1,  w-1, w, w+1 };
		neighborDistances = new float[] { d, 1f, d,  1f, 1f,  d, 1f, d};
	}
	
	public void exec()
	{
		List<Integer> currentPixels = new ArrayList<>();
		
		// find starting pixels
		int p = image.getWidth() * image.getHeight();
		for (int i=0; i<p; i++)
		{
			if (image.data[i] != startValue) continue;
			
			for (int n=0; n<neighborOffsets.length; n++)
			{
				int ni = i + neighborOffsets[n];
				if (ni < 0 || ni >= p) continue;
				if (image.data[ni] == fillValue)
				{
					currentPixels.add(i);
					break;
				}
			}
		}
		
		
		int steps = 0;
		int[] indices = new int[Math.min(p, currentPixels.size() * 10)];
		int length = currentPixels.size();
		for (int i=0; i<currentPixels.size(); i++) indices[i] = currentPixels.get(i);
		int[] newIndices = new int[indices.length];
		
		
		while (length > 0)
		{
			steps++;
//			System.out.println("start with " + currentPixels.size() + " pixels.");
			int newLength = 0;
			
			for (int j=0; j<length; j++)
			{
				int i = indices[j];
				float d = image.data[i];
				for (int n=0; n<neighborOffsets.length; n++)
				{
					int ni = i + neighborOffsets[n];
					if (ni < 0 || ni >= p) continue;
					
					float nd = d + neighborDistances[n];
					if (image.data[ni] > nd)
					{
						image.data[ni] = nd;
						newIndices[newLength] = ni;
						newLength++;
					}
				}
			}
			
			int[] temp = indices;
			indices = newIndices;
			newIndices = temp;
			length = newLength;
		}
//		System.out.println(steps + " steps.");
	}
	
	public static void main(String[] args) throws Exception
	{
		FloatImage image = new FloatImage(new File("C:/Temp/capture/kinect4/2D/mask.png"));
		DistanceTransform dt = new DistanceTransform(image, 1f);
		
		long time = System.currentTimeMillis();
		dt.exec();
		System.out.println("distance transform in " + (System.currentTimeMillis() - time) + " ms.");
		
		BgrByteImage vizImage = new BgrByteImage(image.getWidth(), image.getHeight());
		ColorMapper colorMapper = new ColorMapper(new ColorMap[] {
		        new ColorMap(4f, new RgbColor(0.0f, 0.0f, 0.5f)),
		        new ColorMap(8f, new RgbColor(0.0f, 0.0f, 1.0f)),
		        new ColorMap(16f, new RgbColor(0.0f, 1.0f, 1.0f)),
		        new ColorMap(24f, new RgbColor(1.0f, 1.0f, 0.0f)),
		        new ColorMap(32f, new RgbColor(1.0f, 0.0f, 0.0f)),
		        new ColorMap(40f, new RgbColor(0.5f, 0.0f, 0.0f)) });
		RgbColor color = new RgbColor();
		int p = image.getWidth() * image.getHeight();
		for (int i=0; i<p; i++)
		{
			RgbColor c = colorMapper.map(image.get(i));
			color.set(c);
			vizImage.set(i, color);
		}
		
		image.normalize();
		color.set(1f, 1f, 1f);
		for (int i=0; i<p; i++)
		{
			if (image.data[i] == 0f)
			{
				image.data[i] = 1f;
				vizImage.set(i, color);
			}
		}
		
		vizImage.save(new File("C:/Temp/capture/kinect4/2D/mask_distanceTransform_viz.png"));
		image.saveToPNG(new FileOutputStream(new File("C:/Temp/capture/kinect4/2D/mask_distanceTransform.png")));
		
		
	}
}
