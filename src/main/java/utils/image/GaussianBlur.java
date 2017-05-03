package utils.image;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Arrays;

import utils.math.Math3D;

public class GaussianBlur 
{
	float[] gaussianLine;
	
	float[] tempForFirstPass;
	int w;
	int h;
	int l;
	int hl;
	
	public GaussianBlur(int radiusInPixels, int imageWidth, int imageHeight)
	{
		// 10f, 33f, 71f, 91f, 71f, 33f, 10f
		// 7f, 26f, 41f, 26f, 7f
		
		gaussianLine = new float[radiusInPixels*2 + 1];
		for (int i=0; i<=radiusInPixels; i++)
		{
			float val = Math3D.gaussianDistribution(i, (float) radiusInPixels/2.14f);
			
			gaussianLine[gaussianLine.length / 2 + i] = val;
			gaussianLine[gaussianLine.length / 2 - i] = val;
		}
		normalize(gaussianLine);
//		System.out.println(Arrays.toString(gaussianLine));
		l = gaussianLine.length;
		hl = (gaussianLine.length - 1)/2;
		
		w = imageWidth;
		h = imageHeight;
		tempForFirstPass = new float[w*h];

	}
	
	private void normalize(float[] arr)
	{
		float sum = 0f;
		for (int i=0; i<arr.length; i++)
		{
			sum += arr[i];
		}
		
		for (int i=0; i<arr.length; i++)
		{
			arr[i] /= sum;
		}
	}

	public void exec(FloatImage input, FloatImage output)
	{	
		// Pass 1: horiz
		for (int y=0; y<h; y++)
		{
			for (int x=0; x<w; x++)
			{
				tempForFirstPass[y*w + x] = 0f;
				int leftX = x - hl;
				int i=0;
				for (int s=leftX; s<leftX+l; s++)
				{
					int snorm = Math.max(0, Math.min(w-1, s));
					float value = input.data[y*w+snorm];
					float weight = gaussianLine[i];
					tempForFirstPass[y*w + x] +=  value * weight;
					i++;
				}	
			}
		}
		// Pass 2
		for (int y=0; y<h; y++)
		{
			for (int x=0; x<w; x++)
			{
				output.data[y*w + x] = 0f;
				int topY = y - hl;
				int i=0;
				for (int t=topY; t<topY+l; t++)
				{
					int tnorm = Math.max(0, Math.min(h-1, t));
					float value = tempForFirstPass[tnorm*w+x];
					float weight = gaussianLine[i];
					output.data[y*w + x] +=  value * weight;
					i++;
				}		
			}
		}
	}
	
	BgrByteImage tempBgr;
	public void exec(BgrByteImage input, BgrByteImage output)
	{	
		if (tempBgr == null)
		{
			tempBgr = new BgrByteImage(input.getWidth(), input.getHeight());
		}
		RgbColor color = new RgbColor(0f, 0f, 0f);
		RgbColor sumColor = new RgbColor(0f, 0f, 0f);
		tempBgr.reset();
		
		// Pass 1: horiz
		for (int y=0; y<h; y++)
		{
			for (int x=0; x<w; x++)
			{
				int leftX = x - hl;
				int i=0;
				sumColor.set(0f, 0f, 0f);
				for (int s=leftX; s<leftX+l; s++)
				{
					int snorm = Math.max(0, Math.min(w-1, s));
					
					input.getColor(snorm, y, color);
					float weight = gaussianLine[i];
					color.set(color.getR() * weight, color.getG() * weight, color.getB() * weight);
					sumColor.add(color);
					i++;
				}
				
				tempBgr.set(x, y, sumColor);
			}
		}
		// Pass 2
		output.reset();
		for (int y=0; y<h; y++)
		{
			for (int x=0; x<w; x++)
			{
				int topY = y - hl;
				int i=0;
				sumColor.set(0f, 0f, 0f);
				for (int t=topY; t<topY+l; t++)
				{
					int tnorm = Math.max(0, Math.min(h-1, t));
					
					tempBgr.getColor(x, tnorm, color);
					float weight = gaussianLine[i];
					color.set(color.getR() * weight, color.getG() * weight, color.getB() * weight);
					
					sumColor.add(color);
					i++;
				}	
				
				output.set(x, y, sumColor);
			}
		}
	}
	
	public void exec(FloatImage input, FloatImage output, boolean[] mask)
	{	
		// Pass 1: horiz
		for (int y=0; y<h; y++)
		{
			for (int x=0; x<w; x++)
			{
				int index = y*w + x;
				if (!mask[index]) continue;
	
				float sum = 0f;
				float weights = 0;
				for (int s=0; s<l; s++)
				{
					int xs = x - hl + s;
					if (xs < 0 || xs >= w) continue;
					int sIndex = y*w+xs;
					if (!mask[sIndex]) continue;
					
					float weight = gaussianLine[s];
					float value = input.data[sIndex];
					sum +=  value * weight;
					weights += weight;
				}
				tempForFirstPass[index] = sum / weights;
			}
		}
		// Pass 2
		for (int y=0; y<h; y++)
		{
			for (int x=0; x<w; x++)
			{
				int index = y*w + x;
				if (!mask[index])
				{
					output.data[index] = input.data[index];
					continue;
				}
	
				float sum = 0f;
				float weights = 0f;
				for (int s=0; s<l; s++)
				{
					int ys = y - hl + s;
					if (ys < 0 || ys >= h) continue;
					int sIndex = ys*w+x;
					if (!mask[sIndex]) continue;
					
					
					float weight = gaussianLine[s];
					float value = tempForFirstPass[sIndex];
					sum +=  value * weight;
					weights += weight;
				}
				output.data[index] = sum / weights;	
			}
		}
	}
	
	public static void main(String[] args) throws Exception
	{
		FloatImage image = new FloatImage(new File("C:/Temp/gray.png"));
		boolean[] mask = new boolean[image.getWidth() * image.getHeight()];
		
		for (int y=0; y<image.getHeight(); y++)
		{
			for (int x=0; x<image.getWidth(); x++)
			{
				mask[y*image.getWidth() + x] = x > 320; 
			}
		}
		
		GaussianBlur blur = new GaussianBlur(10, image.getWidth(), image.getHeight());
		
		FloatImage output = new FloatImage(image.getWidth(), image.getHeight());
		blur.exec(image, output, mask);
		
		output.saveToPNG(new FileOutputStream(new File("C:/Temp/gray_blurred.png")));
	}
}