package utils.math;

import java.nio.ByteBuffer;

public class MatSparse implements IMat
{
	public int[] rowsIndices;
	public int[] colsIndices;
	public float[] values;
	
	public MatSparse(Mat dense)
	{
		byte[] bytes = dense.toByteArray();
		ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);	
		int sparseValues = bytes.length / 4 / 3;
		rowsIndices = new int[sparseValues];
		colsIndices = new int[sparseValues];
		values = new float[sparseValues];
		for (int j=0; j<sparseValues; j++)
		{
			rowsIndices[j] = byteBuffer.getInt();
			colsIndices[j] = byteBuffer.getInt();
			values[j] = byteBuffer.getFloat();
		}
	}
}
