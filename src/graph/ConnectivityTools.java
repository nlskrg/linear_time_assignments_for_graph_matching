package graph;

import java.util.ArrayList;
import java.util.LinkedList;

import datastructure.NTuple;
import graph.Graph.Vertex;
import graph.properties.VertexArray;
import graph.properties.VertexProperty;

public class ConnectivityTools {
	
	/**
	 * Checks if a graph is connected.
	 * @param g a graph
	 * @return true iff g is connected
	 */
	public static boolean isConnected(Graph g) {
		if (g.getEdgeCount() < g.getVertexCount()-1)
			return false;
		
		if (g.getVertexCount() == 0)
			return true;
		
		VertexProperty<Integer> found = new VertexArray<Integer>(g);
		doDFS(g.vertices().iterator().next(), found, 1);
		for (Vertex v : g.vertices()) {
			if (found.get(v) == null) return false;
		}
		
		return true;
	}
	
	public static boolean isBiconnected(Graph g) {
		return isKConnected(g, 2);
	}
	
	public static boolean isKConnected(Graph g, int k) {
		return findKSeparators(g, k-1).isEmpty(); // TODO special case 0-connected?
	}

	
	public static ArrayList<LinkedList<Vertex>> connectedComponents(Graph g) {
		
		VertexProperty<Integer> ccId = new VertexArray<Integer>(g);

		int ccCount = 0;
		for (Vertex v : g.vertices()) {
			if (ccId.get(v) == null) { // not found yet
				doDFS(v, ccId, ccCount);
				ccCount++;
			}
		}
		
		ArrayList<LinkedList<Vertex>> cc = new ArrayList<>(ccCount);
		for (int i=0; i<ccCount; i++) {
			cc.add(new LinkedList<>());
		}
		for (Vertex v : g.vertices()) {
			cc.get(ccId.get(v)).add(v);
		}
		
		return cc;
	}
	
	/**
	 * Determines the number of connected components and returns a
	 * list of representative vertices, each of which exclusively 
	 * resides in a connected component.
	 * @param g
	 * @return vertex list
	 */
	public static LinkedList<Vertex> connectedComponentsRepresentatives(Graph g) {
		
		LinkedList<Vertex> cc = new LinkedList<Vertex>();
		VertexProperty<Integer> visited = new VertexArray<Integer>(g);
		
		for (Vertex v : g.vertices()) {
			if (visited.get(v) == null) { //start dfs
				cc.add(v);
				doDFS(v, visited, cc.size());
			}
		}
		
		return cc;
	}
	
	private static void doDFS(Vertex v, VertexProperty<Integer> found, int ccId) {
		found.set(v, ccId);
		for (Vertex w : v.neighbors()) {
			if (found.get(w)==null) {
				doDFS(w, found, ccId);
			}
		}
	}
	
	/**
	 * Finds cut vertices.
	 * This implementation is inefficient and deletes each vertex in turn
	 * and checks the connectivity.
	 * @param g
	 * @return the cutvertices
	 */
	public ArrayList<Vertex> findCutVertices(Graph g) {
		ArrayList<Vertex> r = new ArrayList<>();
		DynamicInducedSubgraph g2 = new DynamicInducedSubgraph(g);
		for (Vertex v : g.vertices()) {
			g2.deleteVertex(v);
			if (isConnected(g2)) {
				r.add(v);
			}
			g2.createVertex(v);
		}
		
		return r;
	}
	
	/**
	 * Finds all k-separators in the naive way by testing all size k subsets.
	 * @param g
	 * @param k
	 * @return the k separators
	 */
	public static ArrayList<NTuple<Vertex>> findKSeparators(Graph g, int k) {
		
		DynamicInducedSubgraph g2 = new DynamicInducedSubgraph(g);
		
		ArrayList<Vertex> V = new ArrayList<>(g.getVertexCount());
		for (Vertex v : g.vertices()) {
			V.add(v);
		}
		ArrayList<NTuple<Vertex>> r = new ArrayList<>();
	    NTuple<Vertex> kset = new NTuple<>(k);
	    processLargerSubsets(V, kset, 0, 0, r, g2);
	
		return r;
	}
	
	private static void processLargerSubsets(ArrayList<Vertex> set, NTuple<Vertex> subset, int subsetSize, int nextIndex, ArrayList<NTuple<Vertex>> r, DynamicInducedSubgraph g) {
	    if (subsetSize == subset.getN()) {
	        // deleted and test
	    	if (!isConnected(g)) {
	    		r.add(new NTuple<>(subset));
	    	}
	    } else {
	        for (int j = nextIndex; j < set.size(); j++) {
	            subset.set(subsetSize, set.get(j));
	            g.deleteVertex(set.get(j));
	            processLargerSubsets(set, subset, subsetSize + 1, j + 1, r, g);
	            g.createVertex(set.get(j));
	        }
	    }
	}
	

}
