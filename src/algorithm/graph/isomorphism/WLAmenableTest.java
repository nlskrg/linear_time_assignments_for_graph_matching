package algorithm.graph.isomorphism;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;

import algorithm.graph.isomorphism.labelrefinement.VertexLabelConverter;
import algorithm.graph.isomorphism.labelrefinement.WeisfeilerLehmanRefiner;
import graph.AdjListGraph;
import graph.ConnectivityTools;
import graph.Graph;
import graph.GraphTools;
import graph.Graph.Edge;
import graph.Graph.Vertex;
import graph.LGraph;
import graph.Subgraph;
import graph.properties.EdgeArray;
import graph.properties.VertexArray;

/**
 * Tests whether a graph G is amenable to color refinement, i.e., color 
 * refinement succeeds in distinguishing G from any non-isomorphic graph H.
 * 
 * Note: This implementation is not optimized for efficiency.
 * 
 * Vikraman Arvind, Johannes Köbler, Gaurav Rattan, Oleg Verbitsky:
 * Graph Isomorphism, Color Refinement, and Compactness. 
 * Computational Complexity 26(3): 627-685 (2017)
 * 
 * @author Nils Kriege
 *
 */
public class WLAmenableTest {
	
	public static ArrayList<ArrayList<Vertex>> createStablePartition(Graph G) {
		
		VertexArray<Integer> cols = new VertexArray<>(G);
		EdgeArray<String> ea = new EdgeArray<>(G);
		LGraph<Integer, String> lg = new LGraph<>(G, cols, ea);
		for (Vertex v : G.vertices()) {
			cols.set(v, 0); // initialize with uniform labels
		}
		for (Edge e : G.edges()) {
			ea.set(e, "");
		}

		WeisfeilerLehmanRefiner<String> wl = new WeisfeilerLehmanRefiner<>();
		int lastColorCount = 1; 
		int colorCount = 1;
		do {
			// new compression map from strings to 0..colors
			VertexLabelConverter<String> vlc = new VertexLabelConverter<>();
			lg = vlc.refineGraph(wl.refineGraph(lg)); // refinement and compression
			lastColorCount = colorCount;
			colorCount = vlc.getNextLabel();
		} while (colorCount != lastColorCount);
		
		ArrayList<ArrayList<Vertex>> cells = new ArrayList<>(colorCount);
		for (int i=0; i<colorCount; i++) cells.add(new ArrayList<>());
		for (Vertex v : G.vertices()) {
			int col = lg.getVertexLabel().get(v);
			cells.get(col).add(v);
		}
		return cells;
	}
	
	/**
	 * Tests a graph regarding amenability.
	 * @param G simple undirected 
	 * @return whether the graph G is amenable to color refinement.
	 */
	public static boolean testAmenable(Graph G) {
		
		// ---------------------------------
		// (1) obtain stable partition (this can be done more efficiently)
		// ---------------------------------
		ArrayList<ArrayList<Vertex>> cells = createStablePartition(G);

		// ---------------------------------
		// (2) test amenability
		// ---------------------------------

		// We build the cell graph for checking global properties while
		// testing conditions (A) and (B)
		int n = cells.size();
		AdjListGraph cG = new AdjListGraph(n, n*(n-1)/2);
		VertexArray<VertexType> vaCG = new VertexArray<>(cG, true);
		VertexArray<ArrayList<Vertex>> vaCGcell = new VertexArray<>(cG, true);
		
		// (A) type of the graphs induced by cells 
		for (ArrayList<Vertex> cell : cells) {
			Graph iG = GraphTools.createInducedSubgraph(G, cell);
			GraphType type = graphTypeConditionA(iG);
//			System.out.println(type);
			if (type == GraphType.OTHER) {
//				GraphFrame.createGraphFrame(iG);
				System.out.println("cell type");
				return false;
			}
			Vertex v = cG.createVertex();
			if (type == GraphType.COMPLETE || type == GraphType.EMPTY) {
				vaCG.set(v, VertexType.HOMOGENEOUS);
			} else {
				vaCG.set(v, VertexType.HETEROGENEOUS);
			}
			vaCGcell.set(v, cell);
		}
		// (B) type of bipartite graphs induced by two cells
		for (int i=0; i<cells.size(); i++) {
			ArrayList<Vertex> X = cells.get(i);
			for (int j=i+1; j<cells.size(); j++) {
				ArrayList<Vertex> Y = cells.get(j);
				Subgraph ibG = GraphTools.createInducedBipartiteSubgraph(G, X, Y);
				// bipartition of the subgraph
				ArrayList<Vertex> iX = new ArrayList<>(X.size());
				for (Vertex x : X) iX.add(ibG.getVertex(x.getIndex()));
				ArrayList<Vertex> iY = new ArrayList<>(Y.size());
				for (Vertex y : Y) iY.add(ibG.getVertex(y.getIndex()));
				GraphType type = graphTypeConditionB(ibG, iX, iY);
				if (type == GraphType.OTHER) {
					System.out.println("edge type");
					System.out.println(ibG);
					System.out.println(iX);
					System.out.println(iY);
					return false;
				}
				if (type != GraphType.BIPARTITE_COMPLETE && type != GraphType.EMPTY) {
					// anisotropic edge
					cG.createEdge(cG.getVertex(i), cG.getVertex(j));
				}
			}			
		}
		
//		GraphFrame.createGraphFrame(new LGraph<>(cG, vaCG, new EdgeArray<>(cG)));
		
		// (G) and (H) are both checked on the connected components, since
		// we have just created the anisotropic edges
		for (LinkedList<Vertex> cc : ConnectivityTools.connectedComponents(cG)) {
			
			// (H) check that only one heterogeneous vertex exists, which has minimum
			// cardinality.
			Vertex heterogeneaus = null;
			int minCardinality = Integer.MAX_VALUE;
			Vertex minCardinalityVertex = null;
			for (Vertex v : cc) {
				int vCardinality = vaCGcell.get(v).size();
				if (vCardinality < minCardinality) {
					minCardinality = vCardinality;
					minCardinalityVertex = v;
				}
				if (vaCG.get(v) == VertexType.HETEROGENEOUS) {
					// more than one heterogeneous vertex 
					if (heterogeneaus != null) {
						System.out.println("multiple heterogenous");
						return false; 
					} else {
						heterogeneaus = v;
					}
				}
			}
			// check cardinality
			if (heterogeneaus != null && vaCGcell.get(heterogeneaus).size() != minCardinality) {
				System.out.println("cardinality");
				return false;
			}
			
			// (G) check that all connected components are trees satisfying monotonicity
			// both tests are performed within one BFS
			VertexArray<Vertex> parent = new VertexArray<>(cG);
			
			LinkedList<Vertex> level = new LinkedList<>();
			LinkedList<Vertex> nextLevel = new LinkedList<>();
			level.add(minCardinalityVertex);
			// the root is its own parent; this allows to distinguish the root
			// from unvisited vertices
			parent.set(minCardinalityVertex, minCardinalityVertex);  
			while(!level.isEmpty()) {
				while(!level.isEmpty()) {
					Vertex v = level.pop();
					int vCard = vaCGcell.get(v).size();
					for (Vertex w : v.neighbors()) {
						if (parent.get(w) != null) {
							if (parent.get(v) == w) continue;
							System.out.println("no tree"); 
							return false; // multiple parents exists, not a tree
						}
						parent.set(w, v);

						int wCard = vaCGcell.get(w).size();
						if (wCard < vCard) {
							System.out.println("not monotonic: "+wCard+"(vertex "+w+") "+vCard +"(veretx "+v+")");
							return false;
						}
						nextLevel.add(w);
						
					}
				}
				level = nextLevel;
				nextLevel = new LinkedList<>();
			}
			
		}
		
		return true;
	}
	
	public static LGraph<GraphType, GraphType> createCellGraph(Graph G) {
		return createCellGraph(G, createStablePartition(G));
	}
	
	public static LGraph<GraphType, GraphType> createCellGraph(Graph G, ArrayList<ArrayList<Vertex>> cells) {
		int n = cells.size();
		AdjListGraph cG = new AdjListGraph(n, n*(n-1)/2);
		VertexArray<GraphType> vaCG = new VertexArray<>(cG, true);
		EdgeArray<GraphType> eaCG = new EdgeArray<>(cG, true);
		VertexArray<ArrayList<Vertex>> vaCGcell = new VertexArray<>(cG, true);
		
		for (ArrayList<Vertex> cell : cells) {
			Graph iG = GraphTools.createInducedSubgraph(G, cell);
			GraphType type = graphTypeConditionA(iG);
			Vertex v = cG.createVertex();
			vaCG.set(v, type);
			vaCGcell.set(v, cell);
//			if (cell.contains(G.getVertex(8)) && 
//					cell.contains(G.getVertex(61))) {
//				GraphFrame.createGraphFrame(iG,true);
//			}
		}
		for (int i=0; i<cells.size(); i++) {
			ArrayList<Vertex> X = cells.get(i);
			for (int j=i+1; j<cells.size(); j++) {
				ArrayList<Vertex> Y = cells.get(j);
				Subgraph ibG = GraphTools.createInducedBipartiteSubgraph(G, X, Y);
				// bipartition of the subgraph
				ArrayList<Vertex> iX = new ArrayList<>(X.size());
				for (Vertex x : X) iX.add(ibG.getVertex(x.getIndex()));
				ArrayList<Vertex> iY = new ArrayList<>(Y.size());
				for (Vertex y : Y) iY.add(ibG.getVertex(y.getIndex()));
				GraphType type = graphTypeConditionB(ibG, iX, iY);
				if (type != GraphType.BIPARTITE_COMPLETE && type != GraphType.EMPTY) {
					Edge e = cG.createEdge(cG.getVertex(i), cG.getVertex(j));
					eaCG.set(e, type);
//					System.out.println("edge: ");
//					GraphFrame.createGraphFrame(GraphTools.createInducedSubgraph(G, cells.get(i)), true);
//					GraphFrame.createGraphFrame(GraphTools.createInducedSubgraph(G, cells.get(j)), true);
//					GraphFrame.createGraphFrame(GraphTools.createInducedBipartiteSubgraph(G, cells.get(i), cells.get(j)), true);
				}
			}			
		}
		
		return new LGraph<>(cG, vaCG, eaCG);

	}
	
	public enum VertexType {
		HOMOGENEOUS,
		HETEROGENEOUS
	}
	
	public enum GraphType {
		EMPTY,
		COMPLETE,
		MATCHING,
		CO_MATCHING,
		CYCLE5,
		OTHER,
		BIPARTITE_COMPLETE,
		BIPARTITE_STARS,
		CO_BIPARTITE_STARS
	}
	
	public static GraphType graphTypeConditionA(Graph G) {
		
		if (GraphTools.isEmpty(G)) return GraphType.EMPTY;
		if (GraphTools.isComplete(G)) return GraphType.COMPLETE;
		if (GraphTools.isMatchingGraph(G)) return GraphType.MATCHING;
		if (GraphTools.isMatchingGraphComplement(G)) return GraphType.CO_MATCHING;
		if (GraphTools.is5Cycle(G)) return GraphType.CYCLE5;
		
		return GraphType.OTHER;
	}
	
	public static GraphType graphTypeConditionB(Graph G, ArrayList<Vertex> X, ArrayList<Vertex> Y) {
		
		if (GraphTools.isEmpty(G)) return GraphType.EMPTY;
		if (G.getEdgeCount() == X.size()*Y.size()) return GraphType.BIPARTITE_COMPLETE;
		if (isBipartiteStars(G, X, Y)) return GraphType.BIPARTITE_STARS;
		Graph comp = GraphTools.createBipartiteComplement(G, X, Y);
		X = (ArrayList<Vertex>) comp.getProperty("U");
		Y = (ArrayList<Vertex>) comp.getProperty("V");
		if (isBipartiteStars(G, X, Y)) return GraphType.CO_BIPARTITE_STARS;

		return GraphType.OTHER;
	}
	
	public static boolean isBipartiteStars(Graph G, ArrayList<Vertex> X, ArrayList<Vertex> Y) {
		// check for stars
		int degX = X.get(0).getDegree();
		int degY = Y.get(0).getDegree();
		for (int i=1; i<X.size(); i++) {
			if (degX != X.get(0).getDegree()) return false;
		}
		for (int i=1; i<Y.size(); i++) {
			if (degY != Y.get(0).getDegree()) return false;
		}
		if (degX==1 &&  X.size()==Y.size()*degY) return true;
		if (degY==1 &&  Y.size()==X.size()*degX) return true;
		
		return false;
	}
	
	
	public static void main(String[] args) throws IOException {

		AdjListGraph G = new AdjListGraph();
		GraphTools.addCycle(6, G);
		GraphTools.addCycle(3, G);
		boolean test = testAmenable(G);
		System.out.println(test);
		
	}

}
