package utils.text;

import java.util.List;

public interface CsvListener 
{
	void newLine(int index, List<String> parts);
}
