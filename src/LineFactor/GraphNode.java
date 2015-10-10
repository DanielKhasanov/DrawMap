package LineFactor;

import java.util.ArrayList;

public class GraphNode {
	private double posX;
	private double posY;
	private double distanceCost;
	private GraphNode predecessor;
	
	private ArrayList<GraphNode> neighbors;
	
	GraphNode(double x, double y) {
		this.posX = x;
		this.posY = y;
	}
	
	double getX(){
		return posX;
	}
	double getY(){
		return posY;
	}
	void addNeighbor(GraphNode neighbor){ //usually shouldn't exceed 2;
		if (this.neighbors == null) {
			this.neighbors = new ArrayList<GraphNode>();
		}
		this.neighbors.add(neighbor);
	}
	
	void removeNeighbor(GraphNode neighbor){
		if (this.neighbors != null) {
			this.neighbors.remove(neighbor);
		}
	}
	
	ArrayList<GraphNode> neighbors() {
		return this.neighbors;
	}
	
	double distanceFrom(GraphNode n) {
		return Math.sqrt(Math.pow((n.getX() - this.posX), 2) + Math.pow((n.getY() - this.posY), 2));
	}
	
	void setDistanceCost(double d) {
		this.distanceCost = d;
	}
	double getDistanceCost() {
		return this.distanceCost;
	}
	void setPredecessor(GraphNode g) {
		this.predecessor = g;
	}
	GraphNode getPredecessor() {
		return this.predecessor;
	}
	
	
	
}
