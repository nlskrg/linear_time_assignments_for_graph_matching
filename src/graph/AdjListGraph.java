package graph;

import java.util.ArrayList;


public class AdjListGraph extends AbstractExtendibleGraph {
	
	ArrayList<AdjListVertex> vertices;
	ArrayList<AdjListEdge> edges;
	
	public AdjListGraph() {
		super();
		vertices = new ArrayList<AdjListVertex>();
		edges = new ArrayList<AdjListEdge>();
	}
	
	public AdjListGraph(int n, int m) {
		vertices = new ArrayList<AdjListVertex>(n);
		edges = new ArrayList<AdjListEdge>(m);
	}

	public Iterable<AdjListEdge> edges() {
		return edges;
	}

	public Iterable<AdjListVertex> vertices() {
		return vertices;
	}

	public AdjListVertex getVertex(int index) {
		return vertices.get(index);
	}
	
	public AdjListEdge getEdge(int index) {
		return edges.get(index);
	}

	/**
	 * Note: This method has runtime O( min{deg(u),deg(v)} )
	 */
	public AdjListEdge getEdge(Vertex u, Vertex v) {
		return (AdjListEdge)ImplementationHelper.getEdge(u, v);
	}
	
	/**
	 * @see #getEdge(graph.Graph.Vertex, graph.Graph.Vertex)
	 */
	public boolean hasEdge(Vertex u, Vertex v) {
		return getEdge(u, v) != null;
	}
	
	public int getVertexCount() {
		return vertices.size();
	}
	
	public int getEdgeCount() {
		return edges.size();
	}
	
	public AdjListVertex createVertex() {
		AdjListVertex v = new AdjListVertex(vertices.size());
		vertices.add(v);
		
		notifyVertexCreated(v);
		return v;
	}
	
	public AdjListEdge createEdge(Vertex u, Vertex v) {
		AdjListEdge e = new AdjListEdge((AdjListVertex)u, (AdjListVertex)v, edges.size());
		edges.add(e);
		
		notifyEdgeCreated(e);
		return e;
	}
	
	public String toString() {
		return ImplementationHelper.toString(this);
	}
	
	public class AdjListVertex implements Vertex {

		final int index;
		ArrayList<AdjListEdge> edges;
		
		public AdjListVertex(int index) {
			this.index = index;
			edges = new ArrayList<AdjListEdge>();
		}
		
		public ArrayList<AdjListEdge> edges() {
			return edges;
		}
		
		public Iterable<? extends AdjListVertex> neighbors() {
			return ImplementationHelper.createNeighborIterator(this);
		}
		
		public int getDegree() {
			return edges.size();
		}
		
		public int getIndex() {
			return index;
		}
		
		protected void addEdge(AdjListEdge e) {
			edges.add(e);
		}
		
		protected void removeEdge(AdjListEdge e) {
			edges.remove(e);
		}
		
		public String toString() {
			return String.valueOf(index);
		}
	}
	
	// TODO inherit from Pair?
	public class AdjListEdge implements Edge {
		
		final int index;
		private AdjListVertex u, v;
		
		public AdjListEdge(AdjListVertex u, AdjListVertex v, int index) {
			this.index = index;
			setFirstVertex(u);
			setSecondVertex(v);
		}

		/**
		 * Sets the first vertex of the edge and updates its
		 * adjacency list.
		 */
		protected void setFirstVertex(AdjListVertex u) {
			if (u != null) {
				u.removeEdge(this);
			}
			this.u = u;
			u.addEdge(this);
		}
		
		/**
		 * Sets the second vertex of the edge and updates its
		 * adjacency list.
		 */
		protected void setSecondVertex(AdjListVertex v) {
			if (v != null) {
				v.removeEdge(this);
			}
			this.v = v;
			v.addEdge(this);
		}
		
		/**
		 * @param w the vertex that should remain unchanged
		 * @param v new vertex
		 */
		protected void setOppositeVertex(Vertex w, AdjListVertex v) {
			assert (w == u || w == v);
			if (w == u) {
				setSecondVertex(v);
			} else {
				setFirstVertex(v);
			}
		}

		
		public AdjListVertex getFirstVertex() {
			return u;
		}
		
		public AdjListVertex getSecondVertex() {
			return v;
		}
		
		public AdjListVertex getOppositeVertex(Vertex w) {
			assert (w == u || w == v);
			return (w == u) ? v : u;
		}
		
		public int getIndex() {
			return index;
		}
		
		public String toString() {
			return String.valueOf(index);
		}
	}

}