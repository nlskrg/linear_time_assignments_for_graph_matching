package graph;

/**
 * Represents a subgraph, i.e., a graph consisting of a subset of the 
 * vertices and edges of a given graph. The vertices and edges of 
 * the subgraph are distinct objects, but have the same id as the
 * corresponding objects in the original graph. Therefore, vertex and
 * edge annotations can be shared. 
 * 
 * Note: Altering the original graph after a subgraph has been created
 * may lead to inconsistent states. Please copy the subgraph to a new
 * graph object in this case. 
 * 
 * 
 * @author Nils Kriege
 *
 */
public class Subgraph extends AdjListEditable2Graph {
	
	protected Graph graph;
	
	public Subgraph(Graph graph) {
		super(graph.getVertexCount(), graph.getEdgeCount());
		this. graph = graph;
		
		for (int i=0; i<graph.getVertexCount(); i++)
			vertices.add(null);
		
		for (int i=0; i<graph.getEdgeCount(); i++)
			edges.add(null);
	}
	
	public AdjListVertex createVertex() {
		throw new UnsupportedOperationException();
	}
	
	public AdjListVertex createVertex(Vertex v) {
		AdjListVertex w = new AdjListVertex(v.getIndex());
		vertices.set(v.getIndex(), w);
		llVertices.add(w);
		
		notifyVertexCreated(w);
		return w;
	}
	
	public AdjListEdge createEdge(Vertex u, Vertex v) {
		throw new UnsupportedOperationException();
	}
	
	/**
	 * Creates the edge and related vertices if required.
	 * @param e
	 * @return
	 */
	public AdjListEdge createEdge(Edge e) {
		AdjListVertex u = vertices.get(e.getFirstVertex().getIndex());
		if (u == null) {
			u = createVertex(e.getFirstVertex());
		}
		AdjListVertex v = vertices.get(e.getSecondVertex().getIndex());
		if (v == null) {
			v = createVertex(e.getSecondVertex());
		}
		AdjListEdge f = new AdjListEdge(u, v, e.getIndex());
		edges.set(e.getIndex(), f);
		llEdges.add(f);
		
		notifyEdgeCreated(f);
		return f;
	}
	
	public boolean containsVertex(Vertex v) {
		return (vertices.get(v.getIndex()) != null);
	}
	
	public boolean containsEdge(Edge e) {
		return (edges.get(e.getIndex()) != null);
	}
	
}
