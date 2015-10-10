package LineFactor;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.PriorityQueue;

public class GraphStructure {
	private ArrayList<GraphNode> nodes;
	double absX; //longittude
	double absY; //lattitdue
	GraphStructure(double x, double y) {
		this.absX = x;
		this.absY = y;
		this.nodes = new ArrayList<GraphNode>();
		this.nodes.add(new GraphNode(x, y));
	}
	
	//only used for initialization
	void addNode(double x, double y) {
		GraphNode latest = this.nodes.get(this.nodes.size() - 1);
		GraphNode newest = new GraphNode(x, y);
		latest.addNeighbor(newest);
		newest.addNeighbor(latest);
		this.nodes.add(newest);
	}
	
	//if disconnected
	void removeNode(GraphNode g, boolean disconnect) {
		if (disconnect) {
			for (GraphNode neighbor : g.neighbors()) {
				g.removeNeighbor(neighbor);
				neighbor.removeNeighbor(g);
			}
		} else {
			//check if node is essential to graph structure
			if (g.neighbors().size() == 2) {
				GraphNode temp0 = g.neighbors().get(0);
				GraphNode temp1 = g.neighbors().get(1);
				temp0.removeNeighbor(g); temp0.addNeighbor(temp1);
				temp1.removeNeighbor(g); temp1.addNeighbor(temp0);
			}
		}
		
	}
	//Based on an algorithm in Andre LeMothe's "Tricks of the Windows Game Programming Gurus". 
	// Returns intersection point if lines intersect, otherwise null.
	private double[] lineIntersection(double p0_x, double p0_y, double p1_x, double p1_y, double p2_x, double p2_y, double p3_x, double p3_y){
	    double s1_x, s1_y, s2_x, s2_y;
	    s1_x = p1_x - p0_x;     s1_y = p1_y - p0_y;
	    s2_x = p3_x - p2_x;     s2_y = p3_y - p2_y;

	    double s, t;
	    s = (-s1_y * (p0_x - p2_x) + s1_x * (p0_y - p2_y)) / (-s2_x * s1_y + s1_x * s2_y);
	    t = ( s2_x * (p0_y - p2_y) - s2_y * (p0_x - p2_x)) / (-s2_x * s1_y + s1_x * s2_y);

	    if (s >= 0 && s <= 1 && t >= 0 && t <= 1) {
	        // Collision detected
	    	double[] rv = new double[2];
	        rv[0] = p0_x + (t * s1_x);
	        rv[1] = p0_y + (t * s1_y);
	        return rv;
	    }
	    return null; // No collision
	}
	
	private double[] nodeIntersection(GraphNode a1, GraphNode a2, GraphNode b1, GraphNode b2) {
		double a1x = a1.getX();
		double a1y = a1.getY();
		double a2x = a2.getX();
		double a2y = a2.getY();
		double b1x = b1.getX();
		double b1y = b1.getY();
		double b2x = b2.getX();
		double b2y = b2.getY();
		return lineIntersection(a1x, a1y, a2x, a2y, b1x, b1y, b2x, b2y);
	}
	
//	private void mergeNodes(GraphNode a1, GraphNode a2, GraphNode b1, GraphNode b2) {
//		double a1x = a1.getX();
//		double a1y = a1.getY();
//		double a2x = a2.getX();
//		double a2y = a2.getY();
//		double b1x = b1.getX();
//		double b1y = b1.getY();
//		double b2x = b2.getX();
//		double b2y = b2.getY();
//		double[] intersectionCoords = lineIntersection(a1x, a1y, a2x, a2y, b1x, b1y, b2x, b2y);
//		if (intersectionCoords != null){
//			GraphNode merger = new GraphNode(intersectionCoords[0], intersectionCoords[1]);
//			a1.removeNeighbor(a2); a2.removeNeighbor(a1);
//			b1.removeNeighbor(b2); b2.removeNeighbor(b1);
//			a1.addNeighbor(merger); a2.addNeighbor(merger);
//			b1.addNeighbor(merger); b2.addNeighbor(merger);
//		}
//	}
	//[HashMap for this, HashMap for struct g] HashMap of Node pair to all intersections within it
	private ArrayList<HashMap<GraphEdge, ArrayList<GraphNode>>> enumerateIntersections(GraphStructure g) {
		ArrayList<HashMap<GraphEdge, ArrayList<GraphNode>>> rTuple  = new ArrayList<HashMap<GraphEdge, ArrayList<GraphNode>>>();
		HashMap<GraphEdge, ArrayList<GraphNode>> thisMap = new HashMap<GraphEdge, ArrayList<GraphNode>>();
		HashMap<GraphEdge, ArrayList<GraphNode>> gMap = new HashMap<GraphEdge, ArrayList<GraphNode>>();
		HashSet<GraphNode> visited = new HashSet<GraphNode>();
		//Runtime could get extremely complex, which is why the front end tries to smooth out lines and minimize intersections
		for (int i = 0; i < this.nodes.size(); i++) {
			GraphNode n = this.nodes.get(i);
			for (GraphNode o : n.neighbors()) {
				if (!visited.contains(o)) {
					GraphEdge thisEdge = new GraphEdge(n, o);

					HashSet<GraphNode> visited2 = new HashSet<GraphNode>();
					for (int j = 0; j < g.nodes.size(); j++) {
						GraphNode p = g.nodes.get(j);
						for (GraphNode q : n.neighbors()) {
							if (!visited2.contains(q)) {
								GraphEdge gEdge = new GraphEdge(p, q);
								
								double[] intersectionCoords = nodeIntersection(n, o, p, q);
								if (intersectionCoords != null) {
									GraphNode x = new GraphNode(intersectionCoords[0], intersectionCoords[1]);
									if (thisMap.containsKey(thisEdge)) {
										thisMap.get(thisEdge).add(x);
									} else {
										ArrayList<GraphNode> temp = new ArrayList<GraphNode>();
										temp.add(x);
										thisMap.put(thisEdge, temp);
									}
									
									if (gMap.containsKey(gEdge)) {
										gMap.get(gEdge).add(x);
									} else {
										ArrayList<GraphNode> temp = new ArrayList<GraphNode>();
										temp.add(x);
										gMap.put(gEdge, temp);
									}	
								}
							}
						}
					}
				}
			}
		}
		rTuple.add(thisMap);
		rTuple.add(gMap);
		return rTuple;
	}
	
	//reconnects co-linear intersections on an edge by longitude, latitude if vertical
	private void resolveIntersections(GraphEdge e, ArrayList<GraphNode> intersections) {
		GraphNode a = e.a();
		GraphNode b = e.b();
		a.removeNeighbor(b);
		b.removeNeighbor(a);
		ArrayList<GraphNode> ordered = new ArrayList<GraphNode>();
		if (a.getX() == b.getX()) {
			if (a.getY() < b.getY()) {
				ordered.add(0, a); ordered.add(intersections.size() + 1, b);
				for (int i = 0; i < intersections.size(); i++) {
					GraphNode current = intersections.get(i);
					int k = 1;
					for (int j = 0; j < intersections.size(); j++) {
						if (j != i) {
							GraphNode temp = intersections.get(j);
							if (current.getY() > temp.getY()) {
								k++;
							}
						}	
					}
					ordered.add(k, current);
				}
			} else {
				ordered.add(0, b); ordered.add(intersections.size() + 1, a);
				for (int i = 0; i < intersections.size(); i++) {
					GraphNode current = intersections.get(i);
					int k = 1;
					for (int j = 0; j < intersections.size(); j++) {
						if (j != i) {
							GraphNode temp = intersections.get(j);
							if (current.getY() > temp.getY()) {
								k++;
							}
						}	
					}
					ordered.add(k, current);	
				}
			}
		} else if (a.getX() < b.getX()) {
			ordered.add(0, a); ordered.add(intersections.size() + 1, b);
			for (int i = 0; i < intersections.size(); i++) {
				GraphNode current = intersections.get(i);
				int k = 1;
				for (int j = 0; j < intersections.size(); j++) {
					if (j != i) {
						GraphNode temp = intersections.get(j);
						if (current.getX() > temp.getX()) {
							k++;
						}
					}	
				}
				ordered.add(k, current);
			}
			
		} else {
			ordered.add(0, b); ordered.add(intersections.size() + 1, a);
			for (int i = 0; i < intersections.size(); i++) {
				GraphNode current = intersections.get(i);
				int k = 1;
				for (int j = 0; j < intersections.size(); j++) {
					if (j != i) {
						GraphNode temp = intersections.get(j);
						if (current.getX() > temp.getX()) {
							k++;
						}
					}	
				}
				ordered.add(k, current);
			}	
		}
		
		//the resolving of the neighbors 
		for (int i = 0; i < ordered.size() - 1; i++) {
			GraphNode curr = ordered.get(i);
			GraphNode next = ordered.get(i + 1);
			curr.addNeighbor(next); next.addNeighbor(curr);
		}
		
	}
	
	//merges this with structure g, then returns this
	public GraphStructure mergeStructure(GraphStructure g) {
		ArrayList<HashMap<GraphEdge, ArrayList<GraphNode>>> TupleBind =  enumerateIntersections(g);
		HashMap<GraphEdge, ArrayList<GraphNode>> thisMap = TupleBind.get(0);
		HashMap<GraphEdge, ArrayList<GraphNode>> gMap = TupleBind.get(1);
		for (GraphEdge thisEdge : thisMap.keySet()) {
			resolveIntersections(thisEdge, thisMap.get(thisEdge));
		}
		
		for (GraphEdge gEdge : gMap.keySet()) {
			resolveIntersections(gEdge, gMap.get(gEdge));
		}
		return this;		
	}
	
	//returns reverse list of nodes in the a* path from a to b
	public ArrayList<GraphNode> aStarPath(GraphNode a, GraphNode b) {
		boolean found = false;
		Comparator<GraphNode> comparator = new NodeComparator(b);
		PriorityQueue<GraphNode> q = new PriorityQueue<GraphNode>(comparator);
		q.add(a);
		GraphNode current = a;
		a.setDistanceCost(0.0);
		
		prioLoop:
		while (current != null) {
			current = q.poll();
			for (GraphNode neighbor : current.neighbors()) {
				if (neighbor == b) {
					b.setPredecessor(current);
					b.setDistanceCost(current.getDistanceCost() + current.distanceFrom(b));
					found = true;
					break prioLoop;
				}
				neighbor.setPredecessor(current);
				neighbor.setDistanceCost(current.getDistanceCost() + current.distanceFrom(neighbor));
			}
		}
		if (!found) {
			ArrayList<GraphNode> path= new ArrayList<GraphNode>();
			GraphNode predecessor = b;
			while (predecessor != null) {
				path.add(predecessor);
				predecessor = predecessor.getPredecessor();
			}
			return path;
		}
		return null;
	}
 
}
