package utils.math;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;

/**
 * produces a matlab compatible byte stream
 * Matlab 1-based indices
 * (rowindex, colindex, value)*
 * @return
 */
public class MatSparseWritable implements IMat
{
	int cols; // width
	int rows; // height
	
	ByteArrayOutputStream bos;
	DataOutputStream dos;
	
	public MatSparseWritable(int cols, int rows)
	{
		this.cols = cols;
		this.rows = rows;
		bos = new ByteArrayOutputStream(200000);
		dos = new DataOutputStream(bos);
	}
	
	public final void set(int colOffset, int rowOffset, Mat m) throws Exception
	{
		for (int y=0; y<m.rows; y++)
		{
			int ny = y + rowOffset;
			for (int x=0; x<m.cols; x++)
			{
				float val = m.data[y*m.cols + x];
				if (val != 0f) addEntry(x + colOffset, ny, val);
			}
		}
	}
	
	public final void addEntry(int col, int row, float value) throws Exception
	{
		if (value == 0f) return;
		dos.writeInt(row + 1);
		dos.writeInt(col + 1);
		dos.writeFloat(value);
	}
	
	public byte[] getBytes()
	{
		return bos.toByteArray();
	}
	
	public final int getCols() 
	{
		return cols;
	}

	public final int getRows() 
	{
		return rows;
	}
}
