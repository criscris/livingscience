package utils.math;



/**
 * represents a moving direction for an object in the world.
 * 
 *              -z  -PI/2 LEFT SIDE
 *               |
 *               |
 *               |
 *   -x ---------|-----------> +x  0
 *               |
 *               |
 *               | 
 *              +z  +PI/2 RIGHT SIDE
 */
public class Direction
{
	public static final float PI = Math3D.PI;
	public static final float PIH = PI/2f;
	public static final float PIF = PI/4f;
	
	public float rad;
	
	public Direction(float radian)
	{
		set(radian);
	}
	
	public Direction(Vector3f direction)
	{
		set(direction);
	}
	
	public void set(Vector3f direction)
	{
		Vector3f dir = new Vector3f(direction);
		dir.normalize();
		
		double alpha = Math.acos(dir.x);
		if (Math.asin(dir.z) < 0) alpha *= -1.0;
		set((float) alpha);
	}
	
	public void set(float radian)
	{
		rad = radian % (2f*PI);
		if (radian <= -PI) rad = 2f*PI + rad;
		else if (radian > PI) rad = -2f*PI + rad;
	}
	
	private Vector3f dir = new Vector3f(); // for caching
	public Vector3f getVector()
	{
		dir.set((float) Math.cos(rad), 0f, (float) Math.sin(rad));
		return dir;
	}
	
	public Vector3f getVectorNormalized()
	{
		Vector3f dirN = getVector();
		dirN.normalize();
		return dirN;
	}
	
	/**
	 * interpolate between two direction, this instance is set to the result
	 * @param weightD2 0..1f, 1f -> d2 is used
	 */
	public void interpolate(Direction d1, Direction d2, float weightD2)
	{
		if (d1.rad == d2.rad)
		{
			set(d1.rad);
			return;
		}
		float rad1 = d1.rad < 0f ? d1.rad + PI*2f : d1.rad;
		float rad2 = d2.rad < 0f ? d2.rad + PI*2f : d2.rad;
		
		if (rad2 > rad1) interpolate(rad1, rad2, weightD2);
		else interpolate(rad2, rad1, 1f-weightD2);
	}
	
	private void interpolate(float smaller, float bigger, float weightBigger)
	{
		if (bigger - smaller < PI)
		{
			set(smaller * (1f - weightBigger) + bigger * weightBigger);
		}
		else
		{
			set(smaller - (PI*2f - (bigger - smaller))*weightBigger);
		}
	}
	
	public static void main2(String[] args)
	{
		Direction result = new Direction(0f);
		
		Direction d1 = new Direction(Math3D.toRadians(150f));
		Direction d2 = new Direction(Math3D.toRadians(-160f));
		result.interpolate(d1, d2, 0.25f);
		System.out.println(Math3D.toDegrees(result.rad));
	}
	
	public boolean epsilonEquals(Direction dir, float epsilon)
	{
		if (dir.rad == rad) return true;
		float smaller = Math.min(dir.rad, rad);
		float bigger = Math.max(dir.rad, rad);
		float delta = bigger - smaller;
		if (delta < epsilon) return true;
		if (PI*2f - delta < epsilon) return true;
		return false;
	}
}
