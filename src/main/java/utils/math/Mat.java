package utils.math;


import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.Arrays;
import java.util.List;

import utils.text.TextFileUtil;


/**
 * row-wise matrix
 *
 */
public class Mat implements IMat
{
	protected int cols; // width
	protected int rows; // height
	
	public float[] data;
	
	public Mat(int cols, int rows)
	{
		this.cols = cols;
		this.rows = rows;
		
		data = new float[cols * rows];
	}
	
	public Mat(Mat other)
	{
		cols = other.cols;
		rows = other.rows;
		data = Arrays.copyOf(other.data, other.data.length);
	}
	
	public Mat(int cols, int rows, float[] data)
	{
		this.cols = cols;
		this.rows = rows;
		this.data = data;
	}
	
	public final void set(int index, float value)
	{
		data[index] = value;
	}
	
	public final int getCols() 
	{
		return cols;
	}

	public final int getRows() 
	{
		return rows;
	}
	
	public final float[] getRowData(int rowIndex)
	{
		float[] data = new float[cols];
		for (int i=0; i<cols; i++) data[i] = get(i, rowIndex);
		return data;
	}
	
	public final void setRowData(int rowIndex, float[] data)
	{
		for (int i=0; i<cols; i++) set(i, rowIndex, data[i]);
	}
	
	
	public final float[] getColumnData(int colIndex)
	{
		return getColumnData(colIndex, 0, rows);
	}
	
	public final float[] getColumnData(int colIndex, int fromRow, int toRowEx)
	{
		int noOfRows = toRowEx - fromRow;
		float[] data = new float[noOfRows];
		for (int i=0; i<noOfRows; i++) data[i] = get(colIndex, fromRow + i);
		return data;
	}
	
	public void setAllEntriesTo(float value)
	{
		for (int i=0; i<data.length; i++)
		{
			data[i] = value;
		}
	}
	
	public Mat(Matrix3f m)
	{
		this(3, 3);
        data[0] = m.m00;
        data[1] = m.m01;
        data[2] = m.m02;
        data[3] = m.m10;
        data[4] = m.m11;
        data[5] = m.m12;
        data[6] = m.m20;
        data[7] = m.m21;
        data[8] = m.m22;
	}

	public final void set(int col, int row, float value)
	{
		data[row * cols + col] = value;
	}

	public final void set(Mat other)
	{
		for (int i=0; i<other.data.length; i++)
		{
			data[i] = other.data[i];
		}
	}
	
	public final void add(int col, int row, float value)
	{
		data[row * cols + col] += value;
	}
	
	
	/**
	 * no boundary checks
	 */
	public final void set(int colOffset, int rowOffset, Mat m)
	{
		for (int y=0; y<m.rows; y++)
		{
			int ny = y + rowOffset;
			for (int x=0; x<m.cols; x++)
			{
				data[ny*cols + x + colOffset] = m.data[y*m.cols + x];
			}
		}
	}
	
	public void set(FloatBuffer buffer)
	{
		buffer.rewind();
		for (int i=0; i<buffer.capacity(); i++)
		{
			data[i] = buffer.get();
		}
		buffer.rewind();
	}
	
	/**
	 * slow, use only for debugging!
	 */
	public final void mul(Mat A, Mat B) throws Exception
	{
		if (A.cols != B.rows || A.rows != rows || B.cols != cols) throw new Exception("Matrix dimensions do not match for multiplacation.");
		
		int c = A.cols;
		int index = 0;
		for (int y=0; y<rows; y++)
		{
			for (int x=0; x<cols; x++)
			{
				float sum = 0f;
				for (int i=0; i<c; i++)
				{
					sum += A.get(i, y) * B.get(x, i);
				}
				
				data[index] = sum;
				index++;
			}
		}
	}
	
	public final float get(int index)
	{
		return data[index];
	}
	
	public final float get(int col, int row)
	{
		return data[row * cols + col];
	}
	
	public final boolean isSame(Mat o, float epsilon)
	{
		if (o.cols != cols || o.rows != rows) return false;
				
		for (int i=0; i<data.length; i++)
		{
			if (Math.abs(data[i] - o.data[i]) > epsilon) return false;
		}
		
		return true;
	}
	
//	public MWNumericArray toMatlab()
//	{
//		return MWNumericArray.newInstance(new int[] {rows, cols}, createTranspose().data, MWClassID.DOUBLE); 
//	}
//	
//	public MWNumericArray toMatlabSparse()
//	{
//		int noOfElements = 0;
//		for (int i=0; i<data.length; i++)
//		{
//			if (data[i] != 0f) noOfElements++;
//		}
//		
//		int[] rowIndices = new int[noOfElements];
//		int[] colIndices = new int[noOfElements];
//		float[] values = new float[noOfElements];
//		
//		int p = 0;
//		for (int i=0; i<data.length; i++)
//		{
//			if (data[i] != 0f)
//			{
//				rowIndices[p] = i / cols + 1; // matlab has 1-based indices
//				colIndices[p] = i % cols + 1;
//				values[p] = data[i];
//				p++;
//			}
//		}
//		
//		System.out.println("dense: " + (data.length * 4) + " bytes. sparse: " + (noOfElements*4) + " bytes.");
//		
//		// int[] rowindex, int[] colindex, java.lang.Object rData, int rows, int cols, MWClassID classid) 
//		return MWNumericArray.newSparse(rowIndices, colIndices, values, rows, cols, MWClassID.DOUBLE); 
//	}
	
//	public Mat(MWNumericArray m) throws Exception
//	{
//		float[][] array = (float[][]) m.toFloatArray();
//		rows = array.length;
//		cols = rows > 0 ? array[0].length : 0;
//		data = new float[cols * rows];
//		
//		for (int y=0; y<rows; y++)
//		{
//			for (int x=0; x<cols; x++)
//			{
//				data[y*cols + x] = array[y][x];
//			}
//		}
//	}
	
	public Mat createTranspose()
	{
		Mat m = new Mat(rows, cols);
		for (int y=0; y<rows; y++)
		{
			for (int x=0; x<cols; x++)
			{
				m.set(y, x, get(x, y));
			}
		}
		return m;
	}

	public String toString() 
	{
		StringBuilder sb = new StringBuilder();
		for (int y=0; y<rows; y++)
		{
			for (int x=0; x<cols; x++)
			{
				sb.append((x > 0 ? " " : "") + data[y*cols + x]);
			}
			sb.append("\n");
		}
		
		return sb.toString();
	}
	
	public void fromString(List<String> lines)
	{
		for (int y=0; y<lines.size(); y++)
		{
			String[] parts = lines.get(y).split(" ");
			for (int x=0; x<parts.length; x++)
			{
				data[y*cols + x] = new Float(parts[x]);
			}
		}
	}
	
	public void save(File textFile) throws Exception
	{
		TextFileUtil.writeText(toString(), textFile);
	}
	
	public Mat(File textFile) throws Exception
	{
		List<String> lines = TextFileUtil.loadList(textFile);
		rows = lines.size();
		cols = lines.get(0).split(" ").length;
		data = new float[cols * rows];
		fromString(lines);
	}
	
	public byte[] toByteArray()
	{
		ByteBuffer byteBuffer = ByteBuffer.allocate(data.length * 4); // 1 float
		FloatBuffer floatBuffer = byteBuffer.asFloatBuffer();
		for (int i=0; i<data.length; i++)
		{
			floatBuffer.put(data[i]);
		}
		floatBuffer.rewind();
		
		return byteBuffer.array();
	}
	
	/**
	 * Matlab 1-based indices
	 * (rowindex, colindex, value)*
	 * @return
	 */
	public byte[] toSparseByteArrayOldAndSlower()
	{
		int noOfElements = 0;
		for (int i=0; i<data.length; i++)
		{
			 noOfElements += data[i] == 0f ? 0 : 1;
		}
		
		ByteBuffer byteBuffer = ByteBuffer.allocate(noOfElements * 3 * 4); // 2 integer + 1 float per entry
		
		for (int i=0; i<data.length; i++)
		{
			if (data[i] != 0f)
			{
				byteBuffer.putInt(i / cols + 1);
				byteBuffer.putInt(i % cols + 1);
				byteBuffer.putFloat(data[i]);
			}
		}
		byteBuffer.rewind();
		
		return byteBuffer.array();
	}
	
	/**
	 * Matlab 1-based indices
	 * (rowindex, colindex, value)*
	 * @return
	 */
	public byte[] toSparseByteArray()
	{
		ByteArrayOutputStream bos = new ByteArrayOutputStream(data.length / 10);
		DataOutputStream dos = new DataOutputStream(bos);
		
		try
		{
			for (int i=0; i<data.length; i++)
			{
				if (data[i] != 0f)
				{
					dos.writeInt(i / cols + 1);
					dos.writeInt(i % cols + 1);
					dos.writeFloat(data[i]);
				}
			}
		}
		catch (Exception ex)
		{
			
		}

		
		return bos.toByteArray();
	}
	
	public Mat(int cols, int rows, byte[] floatBytes)
	{
		this.cols = cols;
		this.rows = rows;
		data = new float[cols * rows];
		ByteBuffer byteBuffer = ByteBuffer.wrap(floatBytes); 
		FloatBuffer floatBuffer = byteBuffer.asFloatBuffer();
		for (int i=0; i<data.length; i++)
		{
			data[i] = floatBuffer.get();
		}
	}
	
	public Matrix3f toMatrix3f()
	{
		Matrix3f m = new Matrix3f();
		m.set(data);
		return m;
	}
}
