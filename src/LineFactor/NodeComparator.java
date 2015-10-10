package LineFactor;

import java.util.Comparator;

public class NodeComparator implements Comparator<GraphNode>{
	private double xPos;
	private double yPos;
	NodeComparator(GraphNode heuristic) {
		this.xPos = heuristic.getX();
		this.yPos = heuristic.getY();
	}
	
	private double distanceFrom(GraphNode n) {
		return Math.sqrt(Math.pow((n.getX() - xPos), 2) + Math.pow((n.getY() - yPos), 2));
	}

	@Override
	public int compare(GraphNode o1, GraphNode o2) {
		double f1 = o1.getDistanceCost() + distanceFrom(o1);
		double f2 = o2.getDistanceCost() + distanceFrom(o2);
		if (f1 < f2) {
            return -1;
        }
		else if (f1 > f2) {
            return 1;
        }
        return 0;
	}

}
