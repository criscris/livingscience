package utils.io;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;

import utils.ToolExecuter;

public class BinaryFileUtil 
{
	public static byte[] getFileData(File file) throws IOException
	{
		byte data[] = new byte[(int) file.length()];
//		ByteArrayInputStream bis = new ByteArrayInputStream(data);
		BufferedInputStream in = new BufferedInputStream(new FileInputStream(file));
		if (in.read(data) != data.length)
		{
			in.close();
			throw new IOException("file length != read length for " + file.getAbsolutePath());
		}
		in.close();
		return data;
	}
	
	public static int[] getFileDataInt(File file) throws IOException
	{
		int data[] = new int[(int) (file.length()/4)];
		DataInputStream dis = new DataInputStream(new BufferedInputStream(new FileInputStream(file)));
		for (int i=0; i<data.length; i++)
		{
			data[i] = dis.readInt();
		}
		dis.close();
		return data;
	}
	
	public static long[] getFileDataLong(File file) throws IOException
	{
		long data[] = new long[(int) (file.length()/8)];
		// use with BufferedInputStream, otherwise very slow!
		DataInputStream dis = new DataInputStream(new BufferedInputStream(new FileInputStream(file)));
		for (int i=0; i<data.length; i++)
		{
			data[i] = dis.readLong();
		}
		dis.close();
		return data;
	}
	
	public static float[] getFileDataFloat(File file) throws IOException
	{
		return getFileDataFloat(new FileInputStream(file), (int) file.length());
	}
	
	public static float[] getFileDataFloat(InputStream is, int streamLength) throws IOException
	{
		float data[] = new float[(int) (streamLength/4)];
		// use with BufferedInputStream, otherwise very slow!
		DataInputStream dis = new DataInputStream(new BufferedInputStream(is));
		for (int i=0; i<data.length; i++)
		{
			data[i] = dis.readFloat();
		}
		dis.close();
		return data;
	}
	
	public static double[] getFileDataDouble(File file) throws IOException
	{
		return getFileDataDouble(new FileInputStream(file), (int) file.length());
	}
	
	public static double[] getFileDataDouble(InputStream is, int streamLength) throws IOException
	{
		double data[] = new double[(int) (streamLength/8)];
		// use with BufferedInputStream, otherwise very slow!
		DataInputStream dis = new DataInputStream(new BufferedInputStream(is));
		for (int i=0; i<data.length; i++)
		{
			data[i] = dis.readDouble();
		}
		dis.close();
		return data;
	}
	
	public static void writeToFile(byte[] data, File file) throws IOException
	{
		FileOutputStream fos = new FileOutputStream(file);
		fos.write(data);
		fos.close();
	}
	
	public static void writeToFile(long[] data, File file) throws IOException
	{
		// use with BufferedOutputStream, otherwise very slow!
		DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(file)));
		for (int i=0; i<data.length; i++) dos.writeLong(data[i]);
		dos.close();
	}
	
	public static void writeToFile(float[] data, File file) throws IOException
	{
		// use with BufferedOutputStream, otherwise very slow!
		DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(file)));
		for (int i=0; i<data.length; i++) dos.writeFloat(data[i]);
		dos.close();
	}
	
	public static void writeToFile(double[] data, File file) throws IOException
	{
		// use with BufferedOutputStream, otherwise very slow!
		DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(file)));
		for (int i=0; i<data.length; i++) dos.writeDouble(data[i]);
		dos.close();
	}
	
	public static void writeToFile(int[] data, File file) throws IOException
	{
		DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(file)));
		for (int i=0; i<data.length; i++) dos.writeInt(data[i]);
		dos.close();
	}
	
	public static void writeToZipFile(byte[] data, File file) throws IOException
	{
		GZIPOutputStream fos = new GZIPOutputStream(new FileOutputStream(file));
		fos.write(data);
		fos.close();
	}
	
	public static long copy(File in, File out) throws IOException
	{
		return copy(new BufferedInputStream(new FileInputStream(in)), new BufferedOutputStream(new FileOutputStream(out)));
	}
	
	public static byte[] copy(InputStream in) throws IOException
	{
		ByteArrayOutputStream bos = new ByteArrayOutputStream(16384);
		copy(in, bos);
		return bos.toByteArray();
	}

	public static long copy(InputStream in, OutputStream out, boolean closeInputStream, boolean closeOutputStream) throws IOException
	{
		try 
		{
			long byteCount = 0;
			byte[] buffer = new byte[16384];
			int bytesRead = -1;
			while ((bytesRead = in.read(buffer)) != -1) 
			{
				out.write(buffer, 0, bytesRead);
				byteCount += bytesRead;
			}
			out.flush();
			return byteCount;
		}
		finally 
		{
			try 
			{
				if (closeInputStream) in.close();
			} 
			catch (IOException localIOException3) 
			{
			}
			try 
			{
				if (closeOutputStream) out.close();
			} 
			catch (IOException localIOException4) 
			{
			}
		}
	}
	
	public static long copy_log(InputStream in, OutputStream out, long estimatedMBytes) throws IOException
	{
		try 
		{
			long byteCount = 0;
			byte[] buffer = new byte[16384];
			
			
			int bytesRead = -1;
			long time = System.currentTimeMillis();
			
			int i = 0;
			while ((bytesRead = in.read(buffer)) != -1) 
			{
				out.write(buffer, 0, bytesRead);
				byteCount += bytesRead;
				i++;
				if (i % 5000 == 0) 
				{
					long mbytes = (byteCount / 1000 / 1000);
					double mbytespersec = (double) byteCount / (System.currentTimeMillis() - time) / 1000;
					mbytespersec = (double) (int) (mbytespersec * 100.0) / 100.0;
					
					long mbytesLeft = estimatedMBytes - mbytes;
					int secsLeft = (int) (mbytesLeft / mbytespersec);
					
					System.out.println(mbytes + " MByte. " + mbytespersec + " MBytes/sec. " + secsLeft + " secs left.");
				}
			}
			out.flush();
			return byteCount;
		} 
		finally 
		{
			try 
			{
				 in.close();
			} 
			catch (IOException localIOException3) 
			{
			}
			try 
			{
				out.close();
			} 
			catch (IOException localIOException4) 
			{
			}
		}
	}
	
	public static long copy(InputStream in, OutputStream out) throws IOException
	{
		return copy(in, out, true, true);
	}
	
	public static void copyInNewThread(final InputStream src, final OutputStream dest)
	{
	    new Thread(new Runnable() 
	    {
	        public void run() 
	        {
	        	try 
	        	{
					BinaryFileUtil.copy(src, dest);
				} 
	        	catch (IOException e) 
	        	{

				}
	        }
	    }).start();
	}
	
	/**
	 * 
	 * @param source a file or directory
	 * @param targetPath
	 */
	public static long copyToPath(File source, File targetPath, FilenameFilter filter) throws IOException
	{
		long bytesTotal = 0;
		if (source.isFile())
		{
			if (filter != null && !filter.accept(targetPath, source.getName())) return bytesTotal;
			copy(source, new File(targetPath, source.getName()));
			bytesTotal += source.length();
		}
		else if (source.isDirectory())
		{
			File subDir = new File(targetPath, source.getName());
			subDir.mkdirs();
			for (File f : source.listFiles())
			{
				bytesTotal += copyToPath(f, subDir, filter);
			}
		}
		return bytesTotal;
	}
	
	public static void untargz(File targzFile, IOnFileListener listener) throws Exception
	{
		ArchiveInputStream input = new ArchiveStreamFactory().createArchiveInputStream(new BufferedInputStream(
				new GZIPInputStream(new FileInputStream(targzFile))));
		
		ArchiveEntry entry = null;
		int index = 0;
		while ((entry = input.getNextEntry()) != null)
		{
			String name = entry.getName();
			if (entry.isDirectory()) continue;
			
			if (entry.getSize() > Integer.MAX_VALUE) throw new Exception("file entry " + name + ", size=" + entry.getSize() + " too big.");
			
			ByteArrayOutputStream bos = new ByteArrayOutputStream((int) entry.getSize());
			BinaryFileUtil.copy(input, bos, false, true);
			
			listener.onFile(index, bos.toByteArray(), name);
			index++;
		}
		
		input.close();
	}
	
	public static File[] listFiles(File dir, final String endsWith)
	{
		return dir.listFiles(new FilenameFilter() 
		{
			public boolean accept(File dir, String name) 
			{
				return name.endsWith(endsWith);
			}
		});
	}
	
	public static void compareDirsRecursively(File dir1, File dir2)
	{
		long time = System.currentTimeMillis();
		List<File> files1 = listFilesRecursively(dir1, null);
		System.out.println((System.currentTimeMillis() - time) + " ms. " + files1.size() + " files in " + dir1.getAbsolutePath());
		
		time = System.currentTimeMillis();
		List<File> files2 = listFilesRecursively(dir2, null);
		System.out.println((System.currentTimeMillis() - time) + " ms. " + files2.size() + " files in " + dir2.getAbsolutePath());
		
		String dir2String = dir2.getAbsolutePath();
		Set<String> files2set = new HashSet<>();
		for (File file : files2)
		{
			if (file.getName().startsWith("._")) continue;
			files2set.add(file.getAbsolutePath().substring(dir2String.length()));
		}
		
		String dir1String = dir1.getAbsolutePath();
		int count = 0;
		for (File file : files1)
		{
			if (file.getName().startsWith("._")) continue;
			String fileName = file.getAbsolutePath().substring(dir1String.length());
			if (!files2set.contains(fileName))
			{
				System.out.println(fileName);
				count++;
			}
		}
		System.out.println(count + " of " + files1.size() + " files in " + dir1.getAbsolutePath() + " were not found in the " + files2.size() + " files of " + dir2.getAbsolutePath());
	}
	
	public static List<File> listFilesRecursively(File dir,  String endsWith)
	{
		List<File> acceptedFiles = new ArrayList<>();
		listFilesRecursively(dir, endsWith, acceptedFiles);
		return acceptedFiles;
	}
	
	static void listFilesRecursively(File dir, String endsWith, List<File> acceptedFiles)
	{
		for (File file : dir.listFiles())
		{
			if (file.isFile())
			{
				if (endsWith == null || file.getName().endsWith(endsWith)) 
				{
					acceptedFiles.add(file);
					if (acceptedFiles.size() % 1000 == 0) System.out.print(".");
				}
			}
			else if (file.isDirectory())
			{
				listFilesRecursively(file, endsWith, acceptedFiles);
			}
		}
	}
	
	public static void decompressSingle7z(File file, final OutputStream os) throws Exception 
	{
		throw new Exception("Not implemeneted.");
		
//		long time = System.currentTimeMillis();
//		RandomAccessFile randomAccessFile = new RandomAccessFile(file, "r");
//		ISevenZipInArchive inArchive = SevenZip.openInArchive(null, new RandomAccessFileInStream(randomAccessFile));
//
//		ISimpleInArchive simpleInArchive = inArchive.getSimpleInterface();
//		for (ISimpleInArchiveItem item : simpleInArchive.getArchiveItems()) 
//		{
//			if (!item.isFolder()) 
//			{
//				
//				item.extractSlow(new ISequentialOutStream() 
//				{
//					long written = 0;
//					int count = 1;
//					
//					public int write(byte[] data) throws SevenZipException 
//					{
//						try 
//						{
//							os.write(data);
//						} 
//						catch (IOException e) 
//						{
//						}
//						
//						written += data.length;
//						if (written >= 100000000L * count)
//						{
//							System.out.println((written/1000/1000) + " MByte");
//							count++;
//						}
//						
//						return data.length;
//					}
//				});
//			}
//			break; // only first item
//		}
//		inArchive.close();
//		randomAccessFile.close();
//		System.out.println(file.getAbsolutePath() + " decompressed in " + (System.currentTimeMillis() - time) + " ms."); 
	}
	
	
	/**
	 * could lead to errors
	 */
	public static InputStream decompressSingle7z(final File file) throws Exception
	{
		final PipedOutputStream os = new PipedOutputStream();
		PipedInputStream is = new PipedInputStream(os, 16384);
		new Thread(new Runnable() {
			
			public void run() 
			{
				try 
				{
					decompressSingle7z(file, os);
					os.close();
				} 
				catch (Exception e) 
				{
					e.printStackTrace();
				}
				
			}
		}).start();
		return is;
	}
	
	public static void zipDir(OutputStream os, File dir) throws Exception 
	{
		ZipOutputStream zos = new ZipOutputStream(os);
		addDirToZipArchive(zos, dir, null);
		zos.flush();
		zos.close();
	}
	
	static void addDirToZipArchive(ZipOutputStream zos, File fileToZip, String parentDirectoryName) throws Exception 
	{
	    if (fileToZip == null || !fileToZip.exists()) 
	    {
	        return;
	    }

	    String zipEntryName = fileToZip.getName();
	    if (parentDirectoryName!=null && !parentDirectoryName.isEmpty()) 
	    {
	        zipEntryName = parentDirectoryName + "/" + fileToZip.getName();
	    }

	    if (fileToZip.isDirectory()) 
	    {
	        for (File file : fileToZip.listFiles()) 
	        {
	            addDirToZipArchive(zos, file, zipEntryName);
	        }
	    } 
	    else 
	    {
	        byte[] buffer = new byte[1024];
	        FileInputStream fis = new FileInputStream(fileToZip);
	        zos.putNextEntry(new ZipEntry(zipEntryName));
	        int length;
	        while ((length = fis.read(buffer)) > 0) 
	        {
	            zos.write(buffer, 0, length);
	        }
	        zos.closeEntry();
	        fis.close();
	    }
	}
	
	public static void unzip(File zipFile, File targetDir) throws IOException
	{
		unzip(zipFile, targetDir, null);
	}
	
	public static boolean testZip(File zipFile)
	{
		try
		{
			ZipInputStream zin = new ZipInputStream(new FileInputStream(zipFile));
			ZipEntry entry;
//			byte[] buffer = new byte[16384];
			while ((entry = zin.getNextEntry()) != null)
			{
				if (entry.isDirectory()) 
				{

				}
				else
				{
//					int bytesRead = -1;
//					while ((bytesRead = zin.read(buffer)) != -1) 
//					{
//
//					}
				}
			}
			zin.close();
		}
		catch (Exception ex)
		{
			return false;
		}

		return true;
	}
	
	public static InputStream unzipFirstEntry(InputStream zipIS) throws IOException
	{
	      ZipInputStream zin = new ZipInputStream(zipIS);
	      ZipEntry entry = zin.getNextEntry();
	      if (entry == null) return null;
	      return zin;
	}
	
	public static void unzip(File zipFile, IOnFileListener listener, FilenameFilter filter) throws IOException
	{
	      ZipInputStream zin = new ZipInputStream(new FileInputStream(zipFile));
	      ZipEntry entry;
	      byte[] buffer = new byte[16384];
	      int index = 0;
	      while ((entry = zin.getNextEntry()) != null)
	      {  
	    	  if (!entry.isDirectory())
	    	  {
	    		  File file =  new File(entry.getName());
	    		  if (filter != null && !filter.accept(file.getParentFile(), file.getName())) continue;

	    		  if (entry.getSize() > Integer.MAX_VALUE) 
	    		  {
	    			  zin.close();
	    			  throw new IOException("File" + entry.getName() + " too large.");
	    		  }
	    		  
	    		  ByteArrayOutputStream bos = new ByteArrayOutputStream();
	    		  int bytesRead = -1;
	    		  while ((bytesRead = zin.read(buffer)) != -1) 
	    		  {
	    			  bos.write(buffer, 0, bytesRead);
	    		  }
	    		  bos.close();
	    		  
	    		  listener.onFile(index, bos.toByteArray(), entry.getName());
	    		  index++;
	    	  }
	      }
	      zin.close();
	}
	
	public static void unzip(File zipFile, File targetDir, FilenameFilter filter) throws IOException
	{
	      ZipInputStream zin = new ZipInputStream(new FileInputStream(zipFile));
	      ZipEntry entry;
	      byte[] buffer = new byte[16384];
	      while ((entry = zin.getNextEntry()) != null)
	      {
	    	  File file = new File(targetDir, entry.getName());
	    	  if (entry.isDirectory()) 
	    	  {
	    		  if (!file.exists()) file.mkdirs();
	    	  }
	    	  else
	    	  {
	    		  File dir = file.getParentFile();
	    		  if (filter != null && !filter.accept(dir, file.getName())) continue;
	    		  
	    		  if (!dir.exists()) dir.mkdirs();

	    		  BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
	    		  int bytesRead = -1;
	    		  while ((bytesRead = zin.read(buffer)) != -1) 
	    		  {
	    			  bos.write(buffer, 0, bytesRead);
	    		  }
	    		  bos.close();
	    	  }
	      }
	      zin.close();
	}
	
	
	public static File findDir(File start, String name) throws IOException
	{
		if (start.isDirectory())
		{
			if (name.equals(start.getName())) return start;
			File[] children = start.listFiles();
			if (children != null)
			{
				for (File f : children)
				{
					File dir = findDir(f, name);
					if (dir != null) return dir;
				}	
			}
		}
		return null;
	}
	
	
	
	public static long dirSize(File dir) throws Exception
	{
		if (dir.isDirectory())
		{
			long byteCount = 0;
			for (File file : dir.listFiles()) byteCount += dirSize(file);
			return byteCount;
		}
		return dir.length();
	}
	
	public static void removeRecursivley(File fileOrDir) throws IOException
	{
		if (fileOrDir.isDirectory())
		{
			ToolExecuter.execBashSilent(fileOrDir.getParentFile(), 864000, "rm -rf " + fileOrDir.getName());
		}
		else
		{
			fileOrDir.delete();
		}
	}
	
	/**
	 * 
	 * @param is0
	 * @param is1
	 * @return the position of the first byte that is different. -1 when no difference found.
	 * @throws Exception
	 */
	public static long compareFirstDifference(InputStream is0, InputStream is1) throws Exception
	{
		long byteCount = 0;
		byte[] buffer0 = new byte[16384];
		int bytesRead0 = -1;
		
		BufferedInputStream bis1 = new BufferedInputStream(is1);
		byte buffer1[] = new byte[1];
		
		while ((bytesRead0 = is0.read(buffer0)) != -1) 
		{
			for (int i=0; i<bytesRead0; i++)
			{
				int bytesRead1 = bis1.read(buffer1);
				if (bytesRead1 != 1) return byteCount; // is1 is shorter than is0
				if (buffer0[i] != buffer1[0]) return byteCount;
				
				byteCount++;
			}
		}
		return bis1.read(buffer1) == -1 ? -1 : byteCount;
	}
	
	public static void main(String[] args) throws Exception
	{
//		File source = new File("/Volumes/TB2/b201306/Mp3/Rock'n'Pop-Indie");
//		File target = new File("/Users/cschulz/Documents/data/Mp3");
//		
//		long time = System.currentTimeMillis();
//		long bytes = copyToPath(source, target, new FilenameFilter() 
//		{
//			public boolean accept(File dir, String name) 
//			{
//				return name.endsWith(".mp3");
//			}
//		});
//		time = System.currentTimeMillis() - time;
//		System.out.println((bytes/1000/1000) + " MB. " + (bytes / time) + " KB/sec");
		
		double a = Math.random();
		if (a < 2) return;
		
		File file = new File("temp.bin");
		int[] d = new int[20000000];
		for (int i=0; i<d.length; i++) d[i] = i;
		
		long time = System.currentTimeMillis();
		writeToFile(d, file);
		System.out.println((System.currentTimeMillis() - time) + " ms for writing.");
		System.out.println(file.length()/1024/1024 + " MBytes written.");
		
		time = System.currentTimeMillis();
		int[] d2 = getFileDataInt(file);
		System.out.println((System.currentTimeMillis() - time) + " ms for reading.");
		
		boolean same = true;
		for (int i=0; i<d.length; i++)
		{
			if (d[i] != d2[i]) same = false;
		}
		System.out.println("same: " + same);
		
		file.delete();
	}
}
