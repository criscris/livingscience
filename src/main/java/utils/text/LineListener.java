package utils.text;

import java.io.IOException;

public interface LineListener 
{
	void newLine(int index, String line) throws IOException;
}
