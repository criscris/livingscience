package utils;

import java.io.File;
import java.io.IOException;

public class ToolExecuter
{
	public static final String Inkscape_MacOS = "/Applications/Inkscape.app/Contents/Resources/bin/inkscape";
	
	private File programFile;
	
	public ToolExecuter(File programFile)
	{
		this.programFile = programFile;
	}
	
	public void exec(String commandParams)
	{
		if (programFile == null || !programFile.exists()) return;
		String commandStr = programFile.getAbsolutePath() + (commandParams == null ? "" : " " + commandParams);
//		System.out.println(commandStr);
		try 
		{
			Process process = Runtime.getRuntime().exec(commandStr, null, programFile.getParentFile().getAbsoluteFile());
			process.waitFor();
		} 
		catch (InterruptedException e) 
		{
			e.printStackTrace();
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * 
	 * @param commands
	 * @param workingDirectory
	 * @param timeOutInSeconds > 0
	 * @return true if program ended before timeout && no exception on java side
	 */
	public static boolean execBashSilent(File workingDirectory, int timeOutInSeconds, String... commands)
	{
		boolean success = false;
		try
		{
			boolean w = OSValidator.isWindows();
			
			StringBuilder sb = new StringBuilder();
			for (int i=0; i<commands.length; i++)
			{
				sb.append(commands[i]);
				if (i < commands.length - 1) sb.append(w ? " & " : "\n");
			}
			String c = sb.toString();
			
			String[] script = w ? new String[] { "cmd.exe", "/C", c, "2>&1" } : 
				new String[] {"/bin/sh", "-c", sb.toString()};
			
			Process proc = Runtime.getRuntime().exec(script, null, workingDirectory);
			
			long startTime = System.currentTimeMillis();
			while (System.currentTimeMillis() < startTime + 1000L * timeOutInSeconds)
			{
				try
				{
					proc.exitValue();
					success = true;
					break;
				}
				catch (IllegalThreadStateException ex)
				{
					// process not finished yet
				}
				
				Thread.sleep(500);
			}
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			return false;
		}
		return success;
	}
	
	public static void main(String[] args) throws Exception
	{
		System.out.println(execBashSilent(new File("/Users/cschulz/Documents/data/github/temp/"), 120, 
				"git clone https://github.com/cjb/serverless-webrtc.git test",
				"mkdir test2",
				"mkdir test3"));
	}
}
