package absolutelyaya.ultracraft.util;

import net.minecraft.util.math.MathHelper;
import org.joml.Vector3f;

import java.awt.*;

public class ColorUtil
{
	public static Vector3f rgb2hsv(Color c)
	{
		float[] arr = new float[3];
		Color.RGBtoHSB(c.getRed(), c.getGreen(), c.getBlue(), arr);
		return new Vector3f(arr[0], arr[1], arr[2]);
	}
	
	public static Vector3f hsv2rgb(Vector3f c)
	{
		int col = Color.HSBtoRGB(c.x, c.y, c.z);
		return asVector3f(col, true);
	}
	
	public static Vector3f saturate(Vector3f c)
	{
		return new Vector3f(c).max(new Vector3f()).min(new Vector3f(1f));
	}
	
	public static Vector3f fract(Vector3f v)
	{
		return new Vector3f(MathHelper.fractionalPart(v.x), MathHelper.fractionalPart(v.y), MathHelper.fractionalPart(v.z));
	}
	
	
	public static int getAsHex(Vector3f c)
	{
		return new Color(c.x, c.y, c.z).getRGB();
	}
	
	/**
	 * Converts a Hex Color from an Integer to a Vector3f
	 * @param col The color to convert
	 * @param min Whether the result should be on a scale of 0-1; if false, scale will be 0-255
	 * @return The Color as a Math friendly Vector3f
	 */
	public static Vector3f asVector3f(int col, boolean min)
	{
		Color col1 = new Color(col);
		Vector3f out = new Vector3f(col1.getBlue(), col1.getGreen(), col1.getRed());
		return min ? out.div(255f) : out;
	}
	
	/**
	 * Maps a given Color to a hsvMaps pixel.
	 * HSV maps work with the delta to 100 on each value (if map.x == 99 => colIn.x - 1)
	 * @param colIn The Color to Map
	 * @param map A pixel from a hsvMap
	 * @return The Mapped Color
	 */
	public static int mapHSV(int colIn, int map)
	{
		Vector3f hsvIn = rgb2hsv(new Color(colIn));
		Vector3f mapV = asVector3f(map, false).sub(100f, 100f, 100f).div(360f, 100f, 100f);
		Vector3f v1 = hsvIn.add(mapV).max(new Vector3f(0f)).min(new Vector3f(255f));
		Vector3f v = hsv2rgb(v1);
		return getAsHex(v);
	}
}
