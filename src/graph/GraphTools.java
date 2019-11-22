package graph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Random;
import java.util.Stack;

import graph.AdjListRootedTree.AdjListRTreeVertex;
import graph.Graph.Edge;
import graph.Graph.Vertex;
import graph.properties.VertexArray;


public class GraphTools {
	
	private static final long seed = 88686765;
	private static final Random rand = new Random(seed);
	
	/**
	 * 
	 * @param G undirected simple graph
	 * @return complement of G
	 */
	public static AdjListGraph complement(Graph G) {
		int n = G.getVertexCount();
		int m = (n*n-1)/2 - G.getEdgeCount();
		AdjListGraph H = new AdjListGraph(n,m);
		
		HashMap<Vertex, Vertex> targetToSourceMap = new HashMap<Vertex, Vertex>();
		for (Vertex sv : G.vertices()) {
			Vertex tv = H.createVertex();
			targetToSourceMap.put(tv, sv);
		}
		
		for (int i=0; i<H.getVertexCount(); i++) {
			Vertex tu = H.getVertex(i);
			Vertex su = targetToSourceMap.get(tu);
			for (int j=i+1; j<H.getVertexCount(); j++) {
				Vertex tv = H.getVertex(j);
				Vertex sv = targetToSourceMap.get(tv);
				if (!G.hasEdge(su, sv)) {
					H.createEdge(tu, tv);
				}
			}

		}
		
		return H;
	}
	
	public static void addCycle(int k, ExtendibleGraph g) {
		
		ArrayList<Vertex> l = new ArrayList<>(k);
		for (int i=0; i<k; i++) {
			l.add(g.createVertex());
		}
		for (int i=1; i<k; i++) {
			g.createEdge(l.get(i-1), l.get(i));
		}
		g.createEdge(l.get(k-1), l.get(0));
		
	}

	/**
	 * Copies all vertices and edges from targetGraph into sourceGraph.
	 * The implementation of both graphs may differ.
	 * @param sourceGraph
	 * @param targetGraph
	 * @return a mapping of vertices from source to target
	 */
	// TODO preserve edge oder for each vertex!?
	// TODO more efficient implementation using indices? targetGraph must be empty? or use offsets!?
	public static HashMap<Vertex, Vertex> copyGraph(Graph sourceGraph, ExtendibleGraph targetGraph, boolean shuffle, boolean removeSelfLoops) {
		HashMap<Vertex, Vertex> sourceToTargetMap = new HashMap<Vertex, Vertex>();

		Iterable<? extends Vertex> sV = sourceGraph.vertices();
		if (shuffle) {
			ArrayList<Vertex> copy = new ArrayList<>(sourceGraph.getVertexCount());
			for (Vertex v : sV) copy.add(v);
			Collections.shuffle(copy, rand);
			sV = copy;
		}
		for (Vertex sv : sV) {
			Vertex tv = targetGraph.createVertex();
			sourceToTargetMap.put(sv, tv);
		}
		
		for (Edge se : sourceGraph.edges()) {
			Vertex su = se.getFirstVertex();
			Vertex sv = se.getSecondVertex();
			if (removeSelfLoops && su == sv) continue;
			Vertex tu = sourceToTargetMap.get(su);
			Vertex tv = sourceToTargetMap.get(sv);
			targetGraph.createEdge(tu, tv);
		}
		
		return sourceToTargetMap;
	}
	
	public static HashMap<Vertex, Vertex> copyGraph(Graph sourceGraph, ExtendibleGraph targetGraph) {
		return copyGraph(sourceGraph, targetGraph, false, false);
	}
	
	public static AdjListGraph shuffleNodes(Graph graph) {
		AdjListGraph target = new AdjListGraph(graph.getVertexCount(), graph.getEdgeCount());
		copyGraph(graph, target, true, false);
		return target;
	}
	
	public static AdjListGraph removeSelfLoops(Graph graph) {
		AdjListGraph target = new AdjListGraph(graph.getVertexCount(), graph.getEdgeCount());
		copyGraph(graph, target, false, true);
		return target;
	}
	
	public static void addRandomGraph(int vertexCount, int edgeCount, ExtendibleGraph graph) {
		ArrayList<Vertex> vertices = new ArrayList<Vertex>(vertexCount);

		for (int i=0; i < vertexCount; i++) {
			Vertex v = graph.createVertex();
			vertices.add(v);
		}
		
		Vertex u, v;
		for (int i=0; i < edgeCount; i++) {
			do {
				u = vertices.get(rand.nextInt(vertexCount));
				v = vertices.get(rand.nextInt(vertexCount));
			} while (u == v || graph.hasEdge(u,v));

			graph.createEdge(u, v);
		}
	}
	
	/**
	 * Creates and adds an Erdos-Renyi random graph following the
	 * G(n, p) model.
	 * @param n number of vertices
	 * @param p edge probability
	 */
	public static void addErdosRenyiGraph(int n, double p, ExtendibleGraph graph) {
		ArrayList<Vertex> vertices = new ArrayList<Vertex>(n);

		for (int i=0; i < n; i++) {
			Vertex v = graph.createVertex();
			vertices.add(v);
		}

		Vertex u, v;
		for (int i=0; i<n; i++) {
			for (int j=i+1; j<n; j++) {
				u = vertices.get(i);
				v = vertices.get(j);
				if (rand.nextDouble() < p) {
					graph.createEdge(u, v);
				}
			}
		}
	}
	
	public static void addGridGraph(int n, int m, ExtendibleGraph graph) {
		Vertex[][] vertices= new Vertex[n][m];

		for (int i=0; i<n; i++) {
			for (int j=0; j<m; j++) {
				vertices[i][j] = graph.createVertex();
			}
		}
		
		for (int i=0; i<n; i++) {
			for (int j=0; j<m-1; j++) {
				Vertex u = vertices[i][j];
				Vertex v = vertices[i][j+1];
				graph.createEdge(u, v);
			}
		}
		
		for (int i=0; i<n-1; i++) {
			for (int j=0; j<m; j++) {
				Vertex u = vertices[i][j];
				Vertex v = vertices[i+1][j];
				graph.createEdge(u, v);
			}
		}
	}
	
	public static void addRandomBinaryTree(int vertexCount, ExtendibleGraph graph) {
		Stack<Vertex> vertices = new Stack<Vertex>();
		
		for (int i=0; i < vertexCount; i++) {
			Vertex v = graph.createVertex();
			vertices.push(v);
		}
		
		ArrayList<Vertex> selectableParants = new ArrayList<Vertex>(vertexCount);
		Vertex root = vertices.pop();
		selectableParants.add(root);
		while (!vertices.isEmpty()) {
			Vertex v = vertices.pop();
			Vertex u = selectableParants.get(rand.nextInt(selectableParants.size()));
			graph.createEdge(u, v);
			selectableParants.add(v);
			if ((u == root && u.getDegree() == 2) || u.getDegree() == 3) {
				selectableParants.remove(u);
			}
		}
	}
	
	/**
	 * Creates a binary tree that is hard to layout with a transformed RT algorithm.
	 * 
	 * Note: Requires graph to be empty!
	 * @param vertexCount
	 * @param graph
	 */
	public static void addDegeneratedBinaryTree(ExtendibleGraph graph) {
		int vertexCount = 20;
		
		Vertex root = graph.createVertex();
		for (int i=0; i<=vertexCount; i++) {
			graph.createEdge(root, graph.createVertex());
		}
		
		Stack<Vertex> branches = new Stack<Vertex>();
		Stack<Vertex> ends = new Stack<Vertex>();
		Random r = new Random(1);
		for (int i=0; i<vertexCount/4; i++) {
			branches.add(graph.getVertex(r.nextInt(vertexCount/2)+1));
		}
		for (Vertex b : branches) {
			int i=0;
			Vertex end = b;
			ends.push(end);
			while (r.nextDouble()>i*0.01d) {
				Vertex newEnd = graph.createVertex();
				graph.createEdge(end, newEnd);
				end = newEnd;
				ends.pop();
				ends.push(end);
				i++;
			}
		}
		for (Vertex end : ends) {
			for (int i=0; i<8; i++) {
				graph.createEdge(end, graph.createVertex());
			}
		}
	}
	
	/**
	 * Adds the graph depicted on page 301 of
	 * "Algorithms on Trees and Graphs", Gabriel Valiente
	 */
	public static void addValienteCliqueGraph(ExtendibleGraph g) {
		int offset = g.getVertexCount();
		
		for (int i=0; i<7; i++)
			g.createVertex();
		
		g.createEdge(g.getVertex(offset+0), g.getVertex(offset+1));
		g.createEdge(g.getVertex(offset+0), g.getVertex(offset+2));
		g.createEdge(g.getVertex(offset+0), g.getVertex(offset+3));
		g.createEdge(g.getVertex(offset+0), g.getVertex(offset+5));
		g.createEdge(g.getVertex(offset+0), g.getVertex(offset+6));
		
		g.createEdge(g.getVertex(offset+1), g.getVertex(offset+3));
		g.createEdge(g.getVertex(offset+1), g.getVertex(offset+4));
		g.createEdge(g.getVertex(offset+1), g.getVertex(offset+6));
		
		g.createEdge(g.getVertex(offset+2), g.getVertex(offset+3));
		g.createEdge(g.getVertex(offset+2), g.getVertex(offset+5));
		g.createEdge(g.getVertex(offset+2), g.getVertex(offset+6));
		
		g.createEdge(g.getVertex(offset+3), g.getVertex(offset+4));
		g.createEdge(g.getVertex(offset+3), g.getVertex(offset+5));
		g.createEdge(g.getVertex(offset+3), g.getVertex(offset+6));
		
		g.createEdge(g.getVertex(offset+4), g.getVertex(offset+6));

		g.createEdge(g.getVertex(offset+5), g.getVertex(offset+6));
	}
	
	public static void addCompleteGraph(int vertexCount, ExtendibleGraph g) {
		int offset = g.getNextVertexIndex();
		
		for (int i=0; i<vertexCount; i++) {
			g.createVertex();
		}
		for (int i=0; i<vertexCount; i++) {
			Vertex u = g.getVertex(offset+i);
			for (int j=i+1; j<vertexCount; j++) {
				Vertex v = g.getVertex(offset+j);
				g.createEdge(u, v);
			}
		}
	}
	
	public static Subgraph createInducedSubgraph(Graph g, Collection<Vertex> vertices) {
		Subgraph sg = new Subgraph(g);
		for (Vertex v : vertices) {
			sg.createVertex(v);
		}
		for (Edge e : g.edges()) {
			Vertex u = e.getFirstVertex();
			Vertex v = e.getSecondVertex();
			if (vertices.contains(u) && vertices.contains(v)) {
				sg.createEdge(e);
			}
		}
		return sg;
	}
	
	public static Subgraph createInducedBipartiteSubgraph(Graph g, Collection<Vertex> U, Collection<Vertex> V) {
		Subgraph sg = new Subgraph(g);
		for (Vertex v : U) {
			sg.createVertex(v);
		}
		for (Vertex v : V) {
			sg.createVertex(v);
		}
		for (Edge e : g.edges()) {
			Vertex u = e.getFirstVertex();
			Vertex v = e.getSecondVertex();
			if ((V.contains(u) && U.contains(v)) || (U.contains(u) && V.contains(v))) {
				sg.createEdge(e);
			}
		}
		return sg;
	}
	
	/**
	 * Creates a bipartite complement.
	 * The bipartition will be stored in the properties "U" and "V".
	 * @param G
	 * @param U
	 * @param V
	 * @return
	 */
	public static AdjListGraph createBipartiteComplement(Graph G, ArrayList<Vertex> U, ArrayList<Vertex> V) {
		int nU = U.size();
		int nV = V.size();
		int m = nU*nV - G.getEdgeCount();
		AdjListGraph H = new AdjListGraph(nU+nV,m);
		ArrayList<Vertex> UH = new ArrayList<>(U.size());
		ArrayList<Vertex> VH = new ArrayList<>(V.size());
		
		HashMap<Vertex, Vertex> sourceToTargetMap = new HashMap<Vertex, Vertex>();
		for (Vertex sv : U) {
			Vertex tv = H.createVertex();
			sourceToTargetMap.put(sv, tv);
			UH.add(tv);
		}
		H.setProperty("U", UH);
		for (Vertex sv : V) {
			Vertex tv = H.createVertex();
			sourceToTargetMap.put(sv, tv);
			VH.add(tv);
		}
		H.setProperty("V", VH);

		for (int i=0; i<U.size(); i++) {
			Vertex su = U.get(i);
			Vertex tu = sourceToTargetMap.get(su);
			for (int j=0; j<V.size(); j++) {
				Vertex sv = V.get(j);
				Vertex tv = sourceToTargetMap.get(sv);
				if (!G.hasEdge(su, sv)) {
					H.createEdge(tu, tv);
				}
			}
		}
		
		return H;
	}

	/**
	 * Creates a rooted tree (vertices have corresponding indices).
	 * @param g
	 * @param root
	 * @return
	 */
	public static RootedTree createInducedRootedTree(Graph g, Vertex root) {
		AdjListRootedTree rt = new AdjListRootedTree(g.getVertexCount());
		HashMap<Vertex,Vertex> gToRt = copyGraph(g, rt);
		rt.setRoot((AdjListRTreeVertex) gToRt.get(root));
		createTreeChildren(null, rt.getRoot(), rt);		
		
		return rt;
	}

	private static void createTreeChildren(AdjListRTreeVertex p, AdjListRTreeVertex v, AdjListRootedTree rt) {
		for (AdjListRTreeVertex c : v.neighbors()) {
			if (c != p) {
				if (c.getParent() != null) throw new IllegalArgumentException("Not a tree structure");
				c.setParent(v);
				createTreeChildren(v, c, rt);
			}
		}
	}
	
	public static double[][] getAdjacencyMatrix(Graph G) {
		int n = G.getVertexCount();
		double[][] A = new double[n][n];
		for (Edge e : G.edges()) {
			int i = e.getFirstVertex().getIndex();
			int j = e.getSecondVertex().getIndex();
			A[i][j] = A[j][i] = 1;
		}
		return A;
	}
	
	public static ArrayList<Vertex> getKHopVertices(Graph g, Vertex s, int k) {
		ArrayList<Vertex> r = new ArrayList<>();
		r.add(s);
		LinkedList<Vertex> level = new LinkedList<>();
		LinkedList<Vertex> nextLevel = new LinkedList<>();
		VertexArray<Boolean> visited = new VertexArray<>(g);
		for (Vertex v : g.vertices()) visited.set(v, false);
		level.add(s);
		visited.set(s, true);
		for (int distance = 0; distance < k; distance++) {
//			System.out.println(distance+" "+level);
			while (!level.isEmpty()) {
				Vertex w = level.pop();
				for (Vertex x : w.neighbors()) {
					if (!visited.get(x)) {
						nextLevel.add(x);
						visited.set(x, true);
					}
				}
			}
			// swap
			LinkedList<Vertex> tmp = level;
			level = nextLevel;
			nextLevel = tmp;
			r.addAll(level);
		}
		return r;
	}
	
	public static boolean isEmpty(Graph G) {
		return G.getEdgeCount() == 0;
	}
	
	/**
	 * Checks whether the graph is complete. 
	 * @param G directed or undirected graph without parallel edges or self-loops
	 * @return
	 */
	public static boolean isComplete(Graph G) {
		int n=G.getVertexCount();
		int m=G.getEdgeCount();
		if (G instanceof Digraph) {
			return n*(n-1) == m;
		} else {
			return n*(n-1) == 2*m;
		}
	}
	
	/**
	 * Checks whether the graph is matching graph, i.e., a
	 * union of non-incident edges.
	 * @param G undirected graph
	 * @return
	 */
	public static boolean isMatchingGraph(Graph G) {
		int n=G.getVertexCount();
		int m=G.getEdgeCount();

		if (n != 2*m) return false;
		for (Vertex v : G.vertices()) {
			if (v.getDegree() != 1) return false;
		}
		
		return true;
	}
	
	/**
	 * Checks whether the graph is the complement of a matching graph, i.e., 
	 * the complement of a graph consisting of a union of non-incident edges.
	 * @param G undirected graph
	 * @return
	 */
	public static boolean isMatchingGraphComplement(Graph G) {
		int n=G.getVertexCount();
		int m=G.getEdgeCount();

		if (n*(n-2) != 2*m) return false;
		for (Vertex v : G.vertices()) {
			if (v.getDegree() != n-2) return false;
		}
		
		return true;
	}
	
	/**
	 * Checks whether the graph is a 5-cyle.
	 * @param G simple undirected graph
	 * @return
	 */
	public static boolean is5Cycle(Graph G) {
		int n=G.getVertexCount();
		int m=G.getEdgeCount();

		if (n != 5  || m != 5) return false;
		for (Vertex v : G.vertices()) {
			if (v.getDegree() != 2) return false;
		}
		
		return true;
	}
}
