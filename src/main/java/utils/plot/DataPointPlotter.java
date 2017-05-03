package utils.plot;

import nu.xom.Element;
import utils.math.Vector2f;

public interface DataPointPlotter 
{
	void plot(int index, Vector2f screenCoords, Element svg, String ns);
}
