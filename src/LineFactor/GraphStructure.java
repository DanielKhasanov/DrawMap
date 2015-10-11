package LineFactor;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.PriorityQueue;

public class GraphStructure {
	private ArrayList<GraphNode> nodes; //not always well ordered! traverse using the linked structure of neighbors
	double absX; //longitude
	double absY; //latitude
	
	GraphStructure(double x, double y) {
		this.absX = x;
		this.absY = y;
		this.nodes = new ArrayList<GraphNode>();
		this.nodes.add(new GraphNode(x, y));
	}
	
	GraphStructure(double[] pairs) throws Exception {
		if (pairs.length % 2 != 0) {
			throw new Exception("Error, mismatched coordinate system");
		}
		double x = pairs[0];
		double y = pairs[1];
		this.absX = x;
		this.absY = y;
		this.nodes = new ArrayList<GraphNode>();
		for (int i = 0; i < pairs.length -1; i += 2) {
			this.addNode(pairs[i], pairs[i+1]);
		}
	}
	
	//only used for initialization of a new graph, at this time nodes is in draw-order
	void addNode(double x, double y) {
		int size = this.nodes.size();
		if (size == 0) {
			GraphNode newest = new GraphNode(x, y);
			this.nodes.add(newest);
		} else {
			GraphNode latest = this.nodes.get(this.nodes.size() - 1);
			GraphNode newest = new GraphNode(x, y);
			latest.addNeighbor(newest);
			newest.addNeighbor(latest);
			this.nodes.add(newest);
		}
		
	}
	
	
	//if disconnected, removes nodes without resolving neighbors, else tries to mend hole
	void removeNode(GraphNode g, boolean disconnect) {
		if (disconnect) {
			for (GraphNode neighbor : g.neighbors()) {
				g.removeNeighbor(neighbor);
				neighbor.removeNeighbor(g);
				this.nodes.remove(g);
			}
		} else {
			//check if node is essential to graph structure
			if (g.neighbors().size() == 2) {
				GraphNode temp0 = g.neighbors().get(0);
				GraphNode temp1 = g.neighbors().get(1);
				temp0.removeNeighbor(g); temp0.addNeighbor(temp1);
				temp1.removeNeighbor(g); temp1.addNeighbor(temp0);
				this.nodes.remove(g);
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
		HashSet<GraphEdge> visited = new HashSet<GraphEdge>();
		//Runtime could get extremely complex, which is why the front end tries to smooth out lines and minimize intersections
		for (int i = 0; i < this.nodes.size(); i++) {
			GraphNode n = this.nodes.get(i);
			for (GraphNode o : n.neighbors()) {
				GraphEdge thisEdge = new GraphEdge(n, o);
				if (!visited.contains(thisEdge)) {
					visited.add(thisEdge);
					HashSet<GraphEdge> visited2 = new HashSet<GraphEdge>();
					for (int j = 0; j < g.nodes.size(); j++) {
						GraphNode p = g.nodes.get(j);
						for (GraphNode q : p.neighbors()) {
							GraphEdge gEdge = new GraphEdge(p, q);
							if (!visited2.contains(gEdge)) {
								visited2.add(gEdge);
								double[] intersectionCoords = nodeIntersection(n, o, p, q);
								if (intersectionCoords != null) {
									/*System.out.println("Intersection Detected with coordinates: " + n.getX() + " " + o.getX() + " " + p.getX() + " " + q.getX());
									System.out.println("Intersection Detected with coordinates: " + n.getY() + " " + o.getY() + " " + p.getY() + " " + q.getY());
									System.out.println("Resolving with coordinate: " + intersectionCoords[0] + " " + intersectionCoords[1]);*/
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
		//new line with correctly ordered intersection points, size should be intersection size + 2
		ArrayList<GraphNode> ordered = new ArrayList<GraphNode>();
		for (int i = 0; i < intersections.size() + 2; i ++) {
			ordered.add(null);
		}
		if (a.getX() == b.getX()) {
			if (a.getY() < b.getY()) {
				ordered.set(0, a);
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
					ordered.set(k, current);
					
				}
				ordered.set(intersections.size() + 1, b);
			} else {
				ordered.add(0, b);
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
					ordered.set(k, current);	
				}
				ordered.set(intersections.size() + 1, a);
			}
		} else if (a.getX() < b.getX()) {
			ordered.set(0, a);
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
				ordered.set(k, current);
			}
			ordered.set(intersections.size() + 1, b);
		} else {
			ordered.add(0, b);
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
				ordered.set(k, current);
			}	
			ordered.set(intersections.size() + 1, a);
		}
		
		//the resolving of the neighbors 
		for (int i = 0; i < ordered.size() - 1; i++) {
			GraphNode curr = ordered.get(i);
			GraphNode next = ordered.get(i + 1);
			curr.addNeighbor(next); next.addNeighbor(curr);
		}
		//adding them into the nodes arrayList, without modifying contents of g
		for (int i = 0; i < ordered.size(); i++) {
			if (!this.nodes.contains(ordered.get(i))) {
				this.nodes.add(ordered.get(i));
			}
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
		
		//clobbers but adds the remaining nodes to this nodes array
		for (GraphNode toAdd : g.nodes) {
			if (!this.nodes.contains(toAdd)) {
				this.nodes.add(toAdd);
			}
		}
		return this;		
	}
	
	//returns reverse list of nodes in the a* path from a to b
	public ArrayList<GraphNode> aStarPath(GraphNode a, GraphNode b) {
		boolean found = false;
		Comparator<GraphNode> comparator = new NodeComparator(b);
		PriorityQueue<GraphNode> q = new PriorityQueue<GraphNode>(comparator);
		HashSet<GraphNode> visited = new HashSet<GraphNode>();
		q.add(a);
		a.setDistanceCost(0.0);
		GraphNode current = a;
		visited.add(a);
		
		prioLoop:
		while (q.size() > 0) {
			current = q.poll();
			for (GraphNode neighbor : current.neighbors()) {
				if (!visited.contains(neighbor)) {
					visited.add(neighbor);
					q.add(neighbor);
					neighbor.setPredecessor(current);
					neighbor.setDistanceCost(current.getDistanceCost() + current.distanceFromNode(neighbor));
				}
				if (neighbor.equals(b)) {
					b.setPredecessor(current);
					b.setDistanceCost(current.getDistanceCost() + current.distanceFromNode(b));
					found = true;
					break prioLoop;
				}
			}
			
		}
		if (found) {
			ArrayList<GraphNode> path= new ArrayList<GraphNode>();
			GraphNode predecessor = b;
			while (predecessor != null) {
				System.out.println(predecessor);
				path.add(predecessor);
				predecessor = predecessor.getPredecessor();
			}
			return path;
		}
		return null;
	}
	
	//need to account for latitude flip
	//also jumps not supported
	private GraphNode closestNode(double x, double y) {
		double minDist = 420.00; //ayy
		GraphNode minNode = null;
		for (GraphNode g : this.nodes) {
			double m = g.distanceFromPoint(x, y);
			if (m < minDist) {
				minDist = m;
				minNode = g;
			}
		}
		return minNode;
	}
	
	public double[] exportPath(double x1, double y1, double x2, double y2) {
		GraphNode a = closestNode(x1, y1);
		GraphNode b = closestNode(x2, y2);
		ArrayList<GraphNode> path = aStarPath(a, b);
		double[] retPath = new double[path.size() * 2];
		int j = 0;
		for (GraphNode g : path) {
			retPath[j] = g.getX();
			retPath[j+1] = g.getY();
			j += 2;
		}
		return retPath;	
	}
	
	//returns acyclic non intersecting paths to give draw instruction 
	private ArrayList<ArrayList<GraphNode>> sectionalize() {
		HashSet<GraphNode> visited = new HashSet<GraphNode>();
		ArrayList<ArrayList<GraphNode>> returnArrays = new ArrayList<ArrayList<GraphNode>>();
		for (GraphNode g : this.nodes) {
			if (!visited.contains(g)) {
				//branches in all directions to avoid gaps
				for (GraphNode n : g.neighbors()) {
					ArrayList<GraphNode> path = new ArrayList<GraphNode>();
					GraphNode nextOne = n;
					path.add(g);
					visited.add(g);
					path.add(nextOne);
					//follows the first valid neighbor onwards
					while (!visited.contains(nextOne)) {
						visited.add(nextOne);
						if (!path.contains(nextOne)) {
							path.add(nextOne);
						}
						for (GraphNode potential : nextOne.neighbors()) {
							if (!visited.contains(potential)) {
								nextOne = potential;
							}
						}
					}
					returnArrays.add(path); // no new neighbors found, end draw function					
				}
			}
		}	
		return returnArrays;
	}
	//return array representation of drawpaths
	public double[][] export(){
		ArrayList<ArrayList<GraphNode>> nodeArray = this.sectionalize();
		double[][] exportArray = new double[nodeArray.size()][];
		for (int i = 0; i < nodeArray.size(); i++) {
			ArrayList<GraphNode> tempArray = nodeArray.get(i);
			double[] currPath = new double[tempArray.size()*2];
			int j = 0;
			for (GraphNode g : tempArray) {
				currPath[j] = g.getX();
				currPath[j+1] = g.getY();
				j += 2;
			}
			exportArray[i] = currPath;
		}
		return exportArray;
	}
	
	private void printNodes() {
		for (GraphNode g : this.nodes) {
			System.out.print("{" + g.getX() + ", " + g.getY() + "} ");
		}
		System.out.println();
	}
	
	public static void main(String[] args) throws Exception {
		/*double[] init = {0.0, 1.0, 2.0, 4.0, 3.0, 9.0, 2.5, 8.0, 2.0, 7.0};
		double[] crossDown = {1.0, 4.0, 1.3, 3.95, 1.7, 4.02, 2.1, 3.5, 2.8, 4.0, 3.7, 3.865, 4.1, 4.2};
		
		GraphStructure g = new GraphStructure(init);
		ArrayList<ArrayList<GraphNode>> test = g.sectionalize();
		System.out.println(test.size());
		GraphStructure cross = new GraphStructure(crossDown);
		g.mergeStructure(cross);
		test = g.sectionalize();
		System.out.println(test.size());
		for (ArrayList<GraphNode> t : test) {
			for (GraphNode x: t) {
				System.out.print(x);
			}
			System.out.println();
		}

		double[] bruh = g.exportPath(-1.0, -1.0, 100.0, 100.0);
		for (double d : bruh) {
			System.out.print(d + " ");
		}
		System.out.println("....");
		double[][] broh = g.export();
		for (double[] meh : broh) {
			for (double d : meh) {
				System.out.print(d + " ");
			}
			System.out.println();
		}*/
	}
 
}
