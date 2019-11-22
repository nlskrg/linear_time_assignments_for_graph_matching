package graph;

import java.util.ArrayList;
import java.util.LinkedHashSet;

/**
 * A subgraph induced by a vertex set which can be changed dynamically.
 * The implementation assumes that the subgraph initially is equal to 
 * the graph and than vertices are deleted and recreated.
 * 
 * Note: You may use vertex- and edge arrays of the supergraph for this 
 * instance!
 * 
 * Do not use {@link AdjListGraph#createVertex()}) or 
 * {@link AdjListGraph#createEdge(Vertex, Vertex)}() inherited from 
 * AdjListGraph, since this will break the subgraph relation.
 * 
 * @author kriege
 *
 */
public class DynamicInducedSubgraph extends AdjListGraph {
	
	LinkedHashSet<AdjListVertex> llVertices;
	LinkedHashSet<AdjListEdge> llEdges;
	
	/**
	 * Note: The vertices and edges og g should be numbered 1,...,|V| and
	 * 1,...,|E| respectively.
	 * @param g
	 */
	public DynamicInducedSubgraph(Graph g) {
		super();
		GraphTools.copyGraph(g, this);
		llVertices = new LinkedHashSet<AdjListVertex>(vertices);
		llEdges = new LinkedHashSet<AdjListEdge>(edges);
	}
	
	public Iterable<AdjListEdge> edges() {
		return llEdges;
	}

	public Iterable<AdjListVertex> vertices() {
		return llVertices;
	}

	public int getVertexCount() {
		return llVertices.size();
	}
	
	public int getEdgeCount() {
		return llEdges.size();
	}
	
	public int getNextVertexIndex() {
		return vertices.size();
	}

	public int getNextEdgeIndex() {
		return edges.size();
	}
	
	
	/**
	 * @param v either of the super or the subgraph
	 */
	public void deleteVertex(final Vertex v) {
		AdjListVertex thisV = vertices.get(v.getIndex()); // make sure we refer to the vertex of this graph
		vertices.set(v.getIndex(), null);
		ArrayList<AdjListEdge> es = thisV.edges();
		while (!es.isEmpty()) {
			deleteEdge(es.get(es.size()-1));
		}
		llVertices.remove(thisV);
		
		notifyVertexDeleted(thisV); 
	}
	
	private void deleteEdge(AdjListEdge e) {
		edges.set(e.getIndex(), null);
		e.getFirstVertex().removeEdge(e);
		e.getSecondVertex().removeEdge(e);
		llEdges.remove(e);
		
		notifyEdgeDeleted(e);
	}
	
	/**
	 * Restores the vertex v of the supergraph in this subgraph.
	 * @param v
	 */
	public AdjListVertex createVertex(Vertex v) {
		// check if present
		if (containsVertex(v)) return vertices.get(v.getIndex());
		
		AdjListVertex vThis = new AdjListVertex(v.getIndex());
		vertices.set(v.getIndex(), vThis);
		
		// recreate the edges to vertices present
		for (Edge e : v.edges()) {
			if (containsVertex(e.getOppositeVertex(v))) {
				createEdge(e);
			}
		}
		llVertices.add(vThis);
		
		notifyVertexCreated(v);
		return vThis;
	}
	
	public boolean containsVertex(Vertex v) {
		return vertices.get(v.getIndex()) != null;
	}
	
	/**
	 * Restores the edge of the supergraph.
	 * @param e
	 * @return
	 */
	private AdjListEdge createEdge(Edge e) {
		// check if present
		if (edges.get(e.getIndex()) != null) return edges.get(e.getIndex());
		
		AdjListVertex uThis = vertices.get(e.getFirstVertex().getIndex());
		AdjListVertex vThis = vertices.get(e.getSecondVertex().getIndex());
		AdjListEdge eThis = new AdjListEdge(uThis, vThis, e.getIndex());
		edges.set(e.getIndex(), eThis);
		llEdges.add(eThis);
		
		notifyEdgeCreated(eThis);
		return eThis;
	}


}
