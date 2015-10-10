package LineFactor;

public class GraphEdge{ //only used for equality
	private GraphNode a;
	private GraphNode b;
	
	GraphEdge(GraphNode x, GraphNode y) {
		this.a = x;
		this.b = y;
	}
	
	GraphNode a(){
		return a;
	}
	
	GraphNode b() {
		return b;
	}
	
	@Override 
	public boolean equals(Object o) { //as long as they have the same two nodes they are equal
		if(o instanceof GraphEdge){
			GraphEdge e = (GraphEdge) o;
			return (((this.a == e.a) && (this.b == e.b)) || ((this.a == e.b) && (this.b == e.a)));
		}
		return false;
	}
	
	@Override
	public int hashCode(){
        return (this.a.hashCode() * this.b.hashCode());
    }
}
