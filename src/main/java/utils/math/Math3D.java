package utils.math;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 * 
 * 
 * @author Christian Schulz
 */
public class Math3D
{
	public static final float PI = 3.14159265358979323846f;
	public static final float PIH = PI/2f;
	public static final float PI2 = PI*2f;
	
	public static Matrix4f getProjectionMatrix(float fov, float aspectRatio, float zNear, float zFar, Matrix4f setmatrix)
	{
		float deltaZ =  zFar - zNear;
		float fov_rad = fov / 180f * PI;
		float cot = (float) (1.0/Math.tan(fov_rad * 0.5f));
		
		Matrix4f m = setmatrix == null ? new Matrix4f() : setmatrix;
		
		m.set(cot, 0.0f, 0.0f, 0.0f,
			0.0f, cot * aspectRatio, 0.0f, 0.0f,
			0.0f, 0.0f, (zFar + zNear) / deltaZ, -2.0f * zNear * zFar / deltaZ,
			0.0f, 0.0f, 1.0f, 0.0f);
		
		return m;
	}
	
	public static Matrix4f getProjectionMatrix_SuperBible(float fov, float aspectRatio, float zNear, float zFar, Matrix4f setmatrix)
	{
		float fov_rad = fov / 180f * PI;
	    float yMax = (float) (zNear * Math.tan(fov_rad * 0.5f));
	    float yMin = -yMax;
		float xMin = yMin * aspectRatio;
	    float xMax = -xMin; 
	    
	    Matrix4f m = setmatrix == null ? new Matrix4f() : setmatrix;
		
	    m.set((2.0f * zNear) / (xMax - xMin), 0.0f, 0.0f, 0.0f,
		 0.0f, (2.0f * zNear) / (yMax - yMin), 0.0f, 0.0f,
		 (xMax + xMin) / (xMax - xMin), (yMax + yMin) / (yMax - yMin), -((zFar + zNear) / (zFar - zNear)), -((2.0f * (zFar*zNear))/(zFar - zNear)),
		 0.0f, 0.0f, -1.0f, 0.0f); // original superbible exchanges -1.0 and -((2.0f * (zFar*zNear))/(zFar - zNear))
	    
	    return m;
	}
	
	public static Matrix4f getOrthoMatrix(float left, float right, float bottom, float top, float near, float far, Matrix4f setmatrix)
	{
		float rml = right - left;
		float fmn = far - near;
		float tmb = top - bottom;

		float _1over_rml = 1f / rml;
		float _1over_fmn = 1f / fmn;
		float _1over_tmb = 1f / tmb;
		
		Matrix4f m = setmatrix == null ? new Matrix4f() : setmatrix;

		m.m00 = 2f* _1over_rml;
		m.m11 = 2f * _1over_tmb;
		m.m22 = -2f * _1over_fmn;

		m.m30 = -(right + left) *  _1over_rml;
		m.m31 = -(top + bottom) *  _1over_tmb;
		m.m32 = -(far + near) * _1over_fmn;
		m.m33 = 1f;
		
		return m;
	}

	
	public static Matrix4f getViewMatrix(Vector3f eyePos, Vector3f lookAtPos, Vector3f upVector, Matrix4f setmatrix)
	{
		Vector3f forward = new Vector3f(lookAtPos);
		forward.sub(eyePos);
		forward.normalize();
		
		
		Vector3f side = new Vector3f();
		side.cross(forward, upVector);
		side.normalize();
		
		Vector3f up = new Vector3f();
		up.cross(side, forward);
		up.normalize();
		
		Matrix4f m = setmatrix == null ? new Matrix4f() : setmatrix;
		
		m.set(side.x, side.y, side.z, -eyePos.dot(side),
			  up.x, up.y, up.z, -eyePos.dot(up),
			  forward.x, forward.y, forward.z, -eyePos.dot(forward),
			  0.0f, 0.0f, 0.0f, 1.0f);	
		
		return m;
	}
	
	/**
	 * @return array of the matrix with a column-major order layout
	 */
	public static float[] getMatrixArray(Matrix4f m)
	{
		float[] marray = new float[16];
		for (int col=0; col<4; col++)
		{
			for (int row=0; row<4; row++)
			{
				marray[col*4 + row] = m.getElement(row, col);
			}
		}
		return marray; 
	}
	
	/**
	 * @return array of the matrix with a column-major order layout (OpenGL)
	 */
	public static FloatBuffer getMatrixArrayBuffer(Matrix4f m)
	{
		ByteBuffer b = ByteBuffer.allocateDirect(16*4);
		FloatBuffer buffer = b.asFloatBuffer();
		
		for (int col=0; col<4; col++) // column-major
		{
			for (int row=0; row<4; row++)
			{
				buffer.put(m.getElement(row, col));
			}
		}
		
		buffer.rewind();
		return buffer; 
	}
	
	/**
	 * @return array of the matrix with a column-major order layout (OpenGL), native order as required by GLSL
	 */
	public static FloatBuffer getMatrixArrayBufferNative(Matrix4f m)
	{
		ByteBuffer b = ByteBuffer.allocateDirect(16*4).order(ByteOrder.nativeOrder());
		FloatBuffer buffer = b.asFloatBuffer();
		
		for (int col=0; col<4; col++) // column-major
		{
			for (int row=0; row<4; row++)
			{
				buffer.put(m.getElement(row, col));
			}
		}
		
		buffer.rewind();
		return buffer; 
	}
	
	public static float[] getMatrixArray(Matrix3f m)
	{
		float[] marray = new float[9];
		for (int col=0; col<3; col++)
		{
			for (int row=0; row<3; row++)
			{
				marray[col*3 + row] = m.getElement(row, col);
			}
		}
		return marray; 
	}
	
	public static Matrix4f getMatrix(FloatBuffer m, Matrix4f output)
	{
		m.rewind();
		for (int col=0; col<4; col++)
		{
			for (int row=0; row<4; row++)
			{
				output.setElement(row, col, m.get());
			}
		}
		m.rewind();
		return output;
	}
	
	public static Matrix4f getMatrix(float[] m, Matrix4f output)
	{
		for (int col=0; col<4; col++)
		{
			for (int row=0; row<4; row++)
			{
				output.setElement(row, col, m[col*4 + row]);
			}
		}
		return output;
	}
	
	/**
	 * linear interpolation
	 * @param value1
	 * @param value2
	 * @param weight2 0..1f this is the weight of value2
	 * @return
	 */
	public static float mix(float value1, float value2, float weight2)
	{
		return value1 * (1f - weight2) + value2 * weight2;
	}
	
	public static float squareXZDistance(Vector3f pos1, Vector3f pos2)
	{
		return (float) (Math.pow(pos2.x - pos1.x, 2) + Math.pow(pos2.z - pos1.z, 2));
	}
	
	/**
	 * calculates the heading of the given vector.
	 * positive x-axis is 0 rad, neg. z is pi/2, pos. z is -pi/2
	 * @param direction
	 * @return angle -pi .. +pi
	 */
	public static float getHeading(Vector3f direction)
	{
		Vector3f dir = new Vector3f(direction);
		dir.normalize();
		
		double alpha = Math.acos(dir.x);
		if (Math.asin(dir.z) > 0) alpha *= -1.0;
		return (float) alpha;
	}
	
	public static float gaussianDistribution(float x, float y, float sigma)
	{
	    float g = 1f / (float) Math.sqrt(2f * PI * sigma * sigma);
	    g *= Math.exp(-( x * x + y * y ) / ( 2 * sigma * sigma ));
	    return g;
	}
	
	public static float gaussianDistribution(float x, float sigma)
	{
		 float g = 1f / (float) Math.sqrt(2f * PI * sigma * sigma);
		 g *= Math.exp(-x*x / (2*sigma*sigma));
		 return g;
	}
	
    public static float toRadians(float angdeg) 
    {
    	return angdeg / 180f * PI;
    }
    
    public static float toRadians(double angdeg) 
    {
    	return (float) (angdeg / 180.0 * Math.PI);
    }
    
    public static float toDegrees(double angrad) 
    {
    	return (float) (angrad * 180.0 / Math.PI);
    }
    
    public static float toDegrees(float angrad) 
    {
    	return angrad * 180f / PI;
    }
    
    public static float clamp01(float value)
    {
    	return Math.max(0f, Math.min(1f, value));
    }
    
    public static double exp(double value)
    {
	    long tmp = (long) (1512775 * value + 1072632447);
	    return value < -700 ? 0.0 : Double.longBitsToDouble(tmp << 32);
    }
}
