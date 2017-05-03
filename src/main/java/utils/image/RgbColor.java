package utils.image;

import java.awt.Color;
import java.io.Serializable;

public class RgbColor implements Serializable
{
	private static final long serialVersionUID = 1941478433960955390L;
	
	private float r;
	private float g;
	private float b;
	
	byte[] bgr;
	
	public static final RgbColor Blue = new RgbColor(0f, 0f, 1f);
	public static final RgbColor Red = new RgbColor(1f, 0f, 0f);
	
	public RgbColor()
	{
		bgr = new byte[3];
		set(0f, 0f, 0f);
	}
	
	public RgbColor(RgbColor color)
	{
		bgr = new byte[3];
		set(color.r, color.g, color.b);
	}
	
	public RgbColor(float r, float g, float b)
	{
		bgr = new byte[3];
		
		set(r, g, b);
	}
	
	public static RgbColor fromInt(int r, int g, int b)
	{
		return new RgbColor((float) r / 255f, (float) g / 255f, (float) b / 255f);
	}
	
	public final void setBgr(byte[] data, int offset)
	{
		bgr[0] = data[offset];
		bgr[1] = data[offset + 1];
		bgr[2] = data[offset + 2];
		
		// bytes are from -127 .. 127. 0..127 maps to 0...0.5f, -127 .. -1 maps to 0.5f..1f
		b = (float) (bgr[0] & 0xFF) / 255f;
		g = (float) (bgr[1] & 0xFF) / 255f;
		r = (float) (bgr[2] & 0xFF) / 255f;
	}
	
	public final void add(RgbColor color)
	{
		this.r += color.r;
		this.g += color.g;
		this.b += color.b;
		
		bgr[0] = (byte)  (b * 255f);
		bgr[1] = (byte)  (g * 255f);
		bgr[2] = (byte)  (r * 255f);
	}
	
	public final void set(RgbColor c)
	{
		set(c.getR(), c.getG(), c.getB());
	}
	
	public final void set(float r, float g, float b)
	{
		this.r = r;
		this.g = g;
		this.b = b;
		
		bgr[0] = (byte)  (b * 255f);
		bgr[1] = (byte)  (g * 255f);
		bgr[2] = (byte)  (r * 255f);
	}
	
	/**
	 * does not set byte values
	 */
	public final void blend(RgbColor color1, RgbColor color2, float weight1)
	{
		float weight2 = 1f - weight1;
		set(color1.r * weight1 + color2.r * weight2,
		    color1.g * weight1 + color2.g * weight2,
		    color1.b * weight1 + color2.b * weight2);
	}
	
	public final void blend(RgbColor otherColor, float otherWeight)
	{
		float weight2 = 1f - otherWeight;
		set(r * weight2 + otherColor.r * otherWeight,
		    g * weight2 + otherColor.g * otherWeight,
		    b * weight2 + otherColor.b * otherWeight);
	}
	
	public final byte getRByte()
	{
		return bgr[2];
	}
	
	public final byte getGByte()
	{
		return bgr[1];
	}
	
	public final byte getBByte()
	{
		return bgr[0];
	}
	
	public final void fillColorBgr(byte[] data, int offset)
	{
		data[offset] = bgr[0];
		data[offset + 1] = bgr[1];
		data[offset + 2] = bgr[2];
	}

	public final float getR() 
	{
		return r;
	}

	public final float getG() 
	{
		return g;
	}

	public final float getB() 
	{
		return b;
	}
	
	public final int getR255() 
	{
		return (int) (r*255f);
	}

	public final int getG255() 
	{
		return (int) (g*255f);
	}

	public final int getB255() 
	{
		return (int) (b*255f);
	}
	
	public final float getGrayScale() 
	{
		return r*0.299f + g*0.587f + b*0.114f;
	}
	
	public boolean isSame(RgbColor color)
	{
		return r == color.r && g == color.g && b == color.b;
	}
	
	public boolean isSame(RgbColor color, float espilon)
	{
		return 	Math.abs(r - color.r) < espilon && 
				Math.abs(g - color.g) < espilon && 
				Math.abs(b - color.b) < espilon; 
	}
	
	public String css()
	{
		return "rgb(" + getR255() + "," + getG255() + "," + getB255() + ")";
	}
	
	/**
	 * 
	 * @param hsbOut hue, saturation, brightness are all 0..1f
	 */
	public void toHSB(float[] hsbOut)
	{
		Color.RGBtoHSB((int) (r * 255f), (int) (g * 255f), (int) (b * 255f), hsbOut);
	}
	
	public Color getAWTColor()
	{
		return new Color(r, g, b);
	}
}
