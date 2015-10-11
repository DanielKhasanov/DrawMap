package LineFactor;
import java.util.Arrays;

import org.json.JSONArray;
import org.json.JSONException;

public class ReadDrawing {
	public JSONArray prepStructure(GraphStructure g) throws JSONException {
		double[][] temp = g.export();
		JSONArray[] temp2 = new JSONArray[temp.length];
		for (int i = 0; i < temp.length; i++) {
			temp2[i] = new JSONArray(Arrays.asList(temp[i]));
		}
		return new JSONArray(temp2);
	}
	
	public JSONArray prepPath(GraphStructure g, double x1, double y1, double x2, double y2) throws JSONException {
		double[] path = g.exportPath(x1, y1, x2, y2);
		return new JSONArray(path);
	}
}
